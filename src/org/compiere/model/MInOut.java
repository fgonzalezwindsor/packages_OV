/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.apps.ADialog;
import org.compiere.apps.ConfirmPanel;
import org.compiere.print.ReportEngine;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.TimeUtil;
import org.ofb.model.OFBForward;
import org.ofb.process.ExportDTEMInOutCGProvectis;
import org.ofb.process.ExportDTEMInOutFOL;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  Shipment Model
 *
 *  @author Jorg Janke
 *  @version $Id: MInOut.java,v 1.4 2006/07/30 00:51:03 jjanke Exp $
 *
 *  Modifications: Added the RMA functionality (Ashley Ramdass)
 *  @author Karsten Thiemann, Schaeffer AG
 * 			<li>Bug [ 1759431 ] Problems with VCreateFrom
 *  @author victor.perez@e-evolution.com, e-Evolution http://www.e-evolution.com
 * 			<li>FR [ 1948157  ]  Is necessary the reference for document reverse
 * 			<li> FR [ 2520591 ] Support multiples calendar for Org
 *			@see http://sourceforge.net/tracker2/?func=detail&atid=879335&aid=2520591&group_id=176962
 *  @author Armen Rizal, Goodwill Consulting
 * 			<li>BF [ 1745154 ] Cost in Reversing Material Related Docs
 *  @see http://sourceforge.net/tracker/?func=detail&atid=879335&aid=1948157&group_id=176962
 *  @author Teo Sarca, teo.sarca@gmail.com
 * 			<li>BF [ 2993853 ] Voiding/Reversing Receipt should void confirmations
 * 				https://sourceforge.net/tracker/?func=detail&atid=879332&aid=2993853&group_id=176962
 */
public class MInOut extends X_M_InOut implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -239302197968535277L;

	/**
	 * 	Create Shipment From Order
	 *	@param order order
	 *	@param movementDate optional movement date
	 *	@param forceDelivery ignore order delivery rule
	 *	@param allAttributeInstances if true, all attribute set instances
	 *	@param minGuaranteeDate optional minimum guarantee date if all attribute instances
	 *	@param complete complete document (Process if false, Complete if true)
	 *	@param trxName transaction
	 *	@return Shipment or null
	 */
	public static MInOut createFrom (MOrder order, Timestamp movementDate,
		boolean forceDelivery, boolean allAttributeInstances, Timestamp minGuaranteeDate,
		boolean complete, String trxName)
	{
		if (order == null)
			throw new IllegalArgumentException("No Order");
		//
		if (!forceDelivery && DELIVERYRULE_CompleteLine.equals(order.getDeliveryRule()))
		{
			return null;
		}

		//	Create Header
		MInOut retValue = new MInOut (order, 0, movementDate);
		retValue.setDocAction(complete ? DOCACTION_Complete : DOCACTION_Prepare);

		//	Check if we can create the lines
		MOrderLine[] oLines = order.getLines(true, "M_Product_ID");
		for (int i = 0; i < oLines.length; i++)
		{
			BigDecimal qty = oLines[i].getQtyOrdered().subtract(oLines[i].getQtyDelivered());
			//	Nothing to deliver
			if (qty.signum() == 0)
				continue;
			//	Stock Info
			MStorage[] storages = null;
			MProduct product = oLines[i].getProduct();
			if (product != null && product.get_ID() != 0 && product.isStocked())
			{
				String MMPolicy = product.getMMPolicy();
				storages = MStorage.getWarehouse (order.getCtx(), order.getM_Warehouse_ID(),
					oLines[i].getM_Product_ID(), oLines[i].getM_AttributeSetInstance_ID(),
					minGuaranteeDate, MClient.MMPOLICY_FiFo.equals(MMPolicy), true, 0, trxName);
			} else {
				continue;
			}

			if (!forceDelivery)
			{
				BigDecimal maxQty = Env.ZERO;
				for (int ll = 0; ll < storages.length; ll++)
					maxQty = maxQty.add(storages[ll].getQtyOnHand());
				if (DELIVERYRULE_Availability.equals(order.getDeliveryRule()))
				{
					if (maxQty.compareTo(qty) < 0)
						qty = maxQty;
				}
				else if (DELIVERYRULE_CompleteLine.equals(order.getDeliveryRule()))
				{
					if (maxQty.compareTo(qty) < 0)
						continue;
				}
			}
			//	Create Line
			if (retValue.get_ID() == 0)	//	not saved yet
				retValue.save(trxName);
			//	Create a line until qty is reached
			for (int ll = 0; ll < storages.length; ll++)
			{
				BigDecimal lineQty = storages[ll].getQtyOnHand();
				if (lineQty.compareTo(qty) > 0)
					lineQty = qty;
				MInOutLine line = new MInOutLine (retValue);
				line.setOrderLine(oLines[i], storages[ll].getM_Locator_ID(),
					order.isSOTrx() ? lineQty : Env.ZERO);
				line.setQty(lineQty);	//	Correct UOM for QtyEntered
				if (oLines[i].getQtyEntered().compareTo(oLines[i].getQtyOrdered()) != 0)
					line.setQtyEntered(lineQty
						.multiply(oLines[i].getQtyEntered())
						.divide(oLines[i].getQtyOrdered(), 12, BigDecimal.ROUND_HALF_UP));
				line.setC_Project_ID(oLines[i].getC_Project_ID());
				line.save(trxName);
				//	Delivered everything ?
				qty = qty.subtract(lineQty);
			//	storage[ll].changeQtyOnHand(lineQty, !order.isSOTrx());	// Credit Memo not considered
			//	storage[ll].save(get_TrxName());
				if (qty.signum() == 0)
					break;
			}
		}	//	for all order lines

		//	No Lines saved
		if (retValue.get_ID() == 0)
			return null;

		return retValue;
	}	//	createFrom

	/**
	 * 	Create new Shipment by copying
	 * 	@param from shipment
	 * 	@param dateDoc date of the document date
	 * 	@param C_DocType_ID doc type
	 * 	@param isSOTrx sales order
	 * 	@param counter create counter links
	 * 	@param trxName trx
	 * 	@param setOrder set the order link
	 *	@return Shipment
	 */
	public static MInOut copyFrom (MInOut from, Timestamp dateDoc, Timestamp dateAcct,
		int C_DocType_ID, boolean isSOTrx, boolean counter, String trxName, boolean setOrder)
	{
		MInOut to = new MInOut (from.getCtx(), 0, null);
		to.set_TrxName(trxName);
		copyValues(from, to, from.getAD_Client_ID(), from.getAD_Org_ID());
		to.set_ValueNoCheck ("M_InOut_ID", I_ZERO);
		to.set_ValueNoCheck ("DocumentNo", null);
		//
		to.setDocStatus (DOCSTATUS_Drafted);		//	Draft
		to.setDocAction(DOCACTION_Complete);
		//
		to.setC_DocType_ID (C_DocType_ID);
		to.setIsSOTrx(isSOTrx);
		if (counter)
		{
			MDocType docType = MDocType.get(from.getCtx(), C_DocType_ID);
			if (MDocType.DOCBASETYPE_MaterialDelivery.equals(docType.getDocBaseType()))
			{
				to.setMovementType (isSOTrx ? MOVEMENTTYPE_CustomerShipment : MOVEMENTTYPE_VendorReturns);
			}
			else if (MDocType.DOCBASETYPE_MaterialReceipt.equals(docType.getDocBaseType()))
			{
				to.setMovementType (isSOTrx ? MOVEMENTTYPE_CustomerReturns : MOVEMENTTYPE_VendorReceipts);
			}
		}

		//
		to.setDateOrdered (dateDoc);
		to.setDateAcct (dateAcct);
		to.setMovementDate(dateDoc);
		to.setDatePrinted(null);
		to.setIsPrinted (false);
		to.setDateReceived(null);
		to.setNoPackages(0);
		to.setShipDate(null);
		to.setPickDate(null);
		to.setIsInTransit(false);
		//
		to.setIsApproved (false);
		to.setC_Invoice_ID(0);
		to.setTrackingNo(null);
		to.setIsInDispute(false);
		//
		to.setPosted (false);
		to.setProcessed (false);
		//[ 1633721 ] Reverse Documents- Processing=Y
		to.setProcessing(false);
		to.setC_Order_ID(0);	//	Overwritten by setOrder
		to.setM_RMA_ID(0);      //  Overwritten by setOrder
		if (counter)
		{
			to.setC_Order_ID(0);
			to.setRef_InOut_ID(from.getM_InOut_ID());
			//	Try to find Order/Invoice link
			if (from.getC_Order_ID() != 0)
			{
				MOrder peer = new MOrder (from.getCtx(), from.getC_Order_ID(), from.get_TrxName());
				if (peer.getRef_Order_ID() != 0)
					to.setC_Order_ID(peer.getRef_Order_ID());
			}
			if (from.getC_Invoice_ID() != 0)
			{
				MInvoice peer = new MInvoice (from.getCtx(), from.getC_Invoice_ID(), from.get_TrxName());
				if (peer.getRef_Invoice_ID() != 0)
					to.setC_Invoice_ID(peer.getRef_Invoice_ID());
			}
			//find RMA link
			if (from.getM_RMA_ID() != 0)
			{
				MRMA peer = new MRMA (from.getCtx(), from.getM_RMA_ID(), from.get_TrxName());
				if (peer.getRef_RMA_ID() > 0)
					to.setM_RMA_ID(peer.getRef_RMA_ID());
			}
		}
		else
		{
			to.setRef_InOut_ID(0);
			if (setOrder)
			{
				to.setC_Order_ID(from.getC_Order_ID());
				to.setM_RMA_ID(from.getM_RMA_ID()); // Copy also RMA
			}
		}
		//
		if (!to.save(trxName))
			throw new IllegalStateException("Could not create Shipment");
		if (counter)
			from.setRef_InOut_ID(to.getM_InOut_ID());

		if (to.copyLinesFrom(from, counter, setOrder) == 0)
			throw new IllegalStateException("Could not create Shipment Lines");

		return to;
	}	//	copyFrom

	/**
	 *  @deprecated
	 * 	Create new Shipment by copying
	 * 	@param from shipment
	 * 	@param dateDoc date of the document date
	 * 	@param C_DocType_ID doc type
	 * 	@param isSOTrx sales order
	 * 	@param counter create counter links
	 * 	@param trxName trx
	 * 	@param setOrder set the order link
	 *	@return Shipment
	 */
	public static MInOut copyFrom (MInOut from, Timestamp dateDoc,
		int C_DocType_ID, boolean isSOTrx, boolean counter, String trxName, boolean setOrder)
	{
		MInOut to = copyFrom ( from, dateDoc, dateDoc,
				C_DocType_ID, isSOTrx, counter,
				trxName, setOrder);
		return to;

	}

	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_InOut_ID
	 *	@param trxName rx name
	 */
	public MInOut (Properties ctx, int M_InOut_ID, String trxName)
	{
		super (ctx, M_InOut_ID, trxName);
		if (M_InOut_ID == 0)
		{
		//	setDocumentNo (null);
		//	setC_BPartner_ID (0);
		//	setC_BPartner_Location_ID (0);
		//	setM_Warehouse_ID (0);
		//	setC_DocType_ID (0);
			setIsSOTrx (false);
			setMovementDate (new Timestamp (System.currentTimeMillis ()));
			setDateAcct (getMovementDate());
		//	setMovementType (MOVEMENTTYPE_CustomerShipment);
			setDeliveryRule (DELIVERYRULE_Availability);
			setDeliveryViaRule (DELIVERYVIARULE_Pickup);
			setFreightCostRule (FREIGHTCOSTRULE_FreightIncluded);
			setDocStatus (DOCSTATUS_Drafted);
			setDocAction (DOCACTION_Complete);
			setPriorityRule (PRIORITYRULE_Medium);
			setNoPackages(0);
			setIsInTransit(false);
			setIsPrinted (false);
			setSendEMail (false);
			setIsInDispute(false);
			//
			setIsApproved(false);
			super.setProcessed (false);
			setProcessing(false);
			setPosted(false);
		}
	}	//	MInOut

	/**
	 *  Load Constructor
	 *  @param ctx context
	 *  @param rs result set record
	 *	@param trxName transaction
	 */
	public MInOut (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MInOut

	/**
	 * 	Order Constructor - create header only
	 *	@param order order
	 *	@param movementDate optional movement date (default today)
	 *	@param C_DocTypeShipment_ID document type or 0
	 */
	public MInOut (MOrder order, int C_DocTypeShipment_ID, Timestamp movementDate)
	{
		this (order.getCtx(), 0, order.get_TrxName());
		setClientOrg(order);
		setC_BPartner_ID (order.getC_BPartner_ID());
		setC_BPartner_Location_ID (order.getC_BPartner_Location_ID());	//	shipment address
		setAD_User_ID(order.getAD_User_ID());
		//
		setM_Warehouse_ID (order.getM_Warehouse_ID());
		setIsSOTrx (order.isSOTrx());
		if (C_DocTypeShipment_ID == 0)
			C_DocTypeShipment_ID = DB.getSQLValue(null,
				"SELECT C_DocTypeShipment_ID FROM C_DocType WHERE C_DocType_ID=?",
				order.getC_DocType_ID());
		setC_DocType_ID (C_DocTypeShipment_ID);
		
		//ininoles aviso para problemas documentno en 0
		/*log.config("ininoles MInOut despues de setear C_DocType_ID= C_DocTypeShipment_ID: "+C_DocTypeShipment_ID+ " sql: SELECT C_DocTypeShipment_ID FROM C_DocType WHERE C_DocType_ID=?"+
				order.getC_DocType_ID());
		log.log(Level.SEVERE, "ininoles MInOut despues de setear C_DocType_ID= C_DocTypeShipment_ID: "+C_DocTypeShipment_ID+ " sql: SELECT C_DocTypeShipment_ID FROM C_DocType WHERE C_DocType_ID=?"+
				order.getC_DocType_ID());*/
		

		// patch suggested by Armen
		// setMovementType (order.isSOTrx() ? MOVEMENTTYPE_CustomerShipment : MOVEMENTTYPE_VendorReceipts);
		String movementTypeShipment = null;
		MDocType dtShipment = new MDocType(order.getCtx(), C_DocTypeShipment_ID, order.get_TrxName()); 
		if (dtShipment.getDocBaseType().equals(MDocType.DOCBASETYPE_MaterialDelivery)) 
			movementTypeShipment = dtShipment.isSOTrx() ? MOVEMENTTYPE_CustomerShipment : MOVEMENTTYPE_VendorReturns; 
		else if (dtShipment.getDocBaseType().equals(MDocType.DOCBASETYPE_MaterialReceipt)) 
			movementTypeShipment = dtShipment.isSOTrx() ? MOVEMENTTYPE_CustomerReturns : MOVEMENTTYPE_VendorReceipts;  
		setMovementType (movementTypeShipment); 
		
		//	Default - Today
		if (movementDate != null)
			setMovementDate (movementDate);
		setDateAcct (getMovementDate());

		//	Copy from Order
		setC_Order_ID(order.getC_Order_ID());
		setDeliveryRule (order.getDeliveryRule());
		setDeliveryViaRule (order.getDeliveryViaRule());
		setM_Shipper_ID(order.getM_Shipper_ID());
		setFreightCostRule (order.getFreightCostRule());
		setFreightAmt(order.getFreightAmt());
		setSalesRep_ID(order.getSalesRep_ID());
		//
		setC_Activity_ID(order.getC_Activity_ID());
		setC_Campaign_ID(order.getC_Campaign_ID());
		setC_Charge_ID(order.getC_Charge_ID());
		setChargeAmt(order.getChargeAmt());
		//
		setC_Project_ID(order.getC_Project_ID());
		setDateOrdered(order.getDateOrdered());
		setDescription(order.getDescription());
		setPOReference(order.getPOReference());
		setSalesRep_ID(order.getSalesRep_ID());
		setAD_OrgTrx_ID(order.getAD_OrgTrx_ID());
		setUser1_ID(order.getUser1_ID());
		setUser2_ID(order.getUser2_ID());
		setPriorityRule(order.getPriorityRule());
		// Drop shipment
		setIsDropShip(order.isDropShip());
		setDropShip_BPartner_ID(order.getDropShip_BPartner_ID());
		setDropShip_Location_ID(order.getDropShip_Location_ID());
		setDropShip_User_ID(order.getDropShip_User_ID());
	}	//	MInOut

	/**
	 * 	Invoice Constructor - create header only
	 *	@param invoice invoice
	 *	@param C_DocTypeShipment_ID document type or 0
	 *	@param movementDate optional movement date (default today)
	 *	@param M_Warehouse_ID warehouse
	 */
	public MInOut (MInvoice invoice, int C_DocTypeShipment_ID, Timestamp movementDate, int M_Warehouse_ID)
	{
		this (invoice.getCtx(), 0, invoice.get_TrxName());
		setClientOrg(invoice);
		setC_BPartner_ID (invoice.getC_BPartner_ID());
		setC_BPartner_Location_ID (invoice.getC_BPartner_Location_ID());	//	shipment address
		setAD_User_ID(invoice.getAD_User_ID());
		//
		setM_Warehouse_ID (M_Warehouse_ID);
		setIsSOTrx (invoice.isSOTrx());
		setMovementType (invoice.isSOTrx() ? MOVEMENTTYPE_CustomerShipment : MOVEMENTTYPE_VendorReceipts);
		MOrder order = null;
		if (invoice.getC_Order_ID() != 0)
			order = new MOrder (invoice.getCtx(), invoice.getC_Order_ID(), invoice.get_TrxName());
		if (C_DocTypeShipment_ID == 0 && order != null)
			C_DocTypeShipment_ID = DB.getSQLValue(null,
				"SELECT C_DocTypeShipment_ID FROM C_DocType WHERE C_DocType_ID=?",
				order.getC_DocType_ID());
		if (C_DocTypeShipment_ID != 0)
			setC_DocType_ID (C_DocTypeShipment_ID);
		else
			setC_DocType_ID();

		//	Default - Today
		if (movementDate != null)
			setMovementDate (movementDate);
		setDateAcct (getMovementDate());

		//	Copy from Invoice
		setC_Order_ID(invoice.getC_Order_ID());
		setSalesRep_ID(invoice.getSalesRep_ID());
		//
		setC_Activity_ID(invoice.getC_Activity_ID());
		setC_Campaign_ID(invoice.getC_Campaign_ID());
		setC_Charge_ID(invoice.getC_Charge_ID());
		setChargeAmt(invoice.getChargeAmt());
		//
		setC_Project_ID(invoice.getC_Project_ID());
		setDateOrdered(invoice.getDateOrdered());
		setDescription(invoice.getDescription());
		setPOReference(invoice.getPOReference());
		setAD_OrgTrx_ID(invoice.getAD_OrgTrx_ID());
		setUser1_ID(invoice.getUser1_ID());
		setUser2_ID(invoice.getUser2_ID());

		if (order != null)
		{
			setDeliveryRule (order.getDeliveryRule());
			setDeliveryViaRule (order.getDeliveryViaRule());
			setM_Shipper_ID(order.getM_Shipper_ID());
			setFreightCostRule (order.getFreightCostRule());
			setFreightAmt(order.getFreightAmt());

			// Drop Shipment
			setIsDropShip(order.isDropShip());
			setDropShip_BPartner_ID(order.getDropShip_BPartner_ID());
			setDropShip_Location_ID(order.getDropShip_Location_ID());
			setDropShip_User_ID(order.getDropShip_User_ID());
		}
	}	//	MInOut

	/**
	 * 	Copy Constructor - create header only
	 *	@param original original
	 *	@param movementDate optional movement date (default today)
	 *	@param C_DocTypeShipment_ID document type or 0
	 */
	public MInOut (MInOut original, int C_DocTypeShipment_ID, Timestamp movementDate)
	{
		this (original.getCtx(), 0, original.get_TrxName());
		setClientOrg(original);
		setC_BPartner_ID (original.getC_BPartner_ID());
		setC_BPartner_Location_ID (original.getC_BPartner_Location_ID());	//	shipment address
		setAD_User_ID(original.getAD_User_ID());
		//
		setM_Warehouse_ID (original.getM_Warehouse_ID());
		setIsSOTrx (original.isSOTrx());
		setMovementType (original.getMovementType());
		if (C_DocTypeShipment_ID == 0)
			setC_DocType_ID(original.getC_DocType_ID());
		else
			setC_DocType_ID (C_DocTypeShipment_ID);

		//	Default - Today
		if (movementDate != null)
			setMovementDate (movementDate);
		setDateAcct (getMovementDate());

		//	Copy from Order
		setC_Order_ID(original.getC_Order_ID());
		setDeliveryRule (original.getDeliveryRule());
		setDeliveryViaRule (original.getDeliveryViaRule());
		setM_Shipper_ID(original.getM_Shipper_ID());
		setFreightCostRule (original.getFreightCostRule());
		setFreightAmt(original.getFreightAmt());
		setSalesRep_ID(original.getSalesRep_ID());
		//
		setC_Activity_ID(original.getC_Activity_ID());
		setC_Campaign_ID(original.getC_Campaign_ID());
		setC_Charge_ID(original.getC_Charge_ID());
		setChargeAmt(original.getChargeAmt());
		//
		setC_Project_ID(original.getC_Project_ID());
		setDateOrdered(original.getDateOrdered());
		setDescription(original.getDescription());
		setPOReference(original.getPOReference());
		setSalesRep_ID(original.getSalesRep_ID());
		setAD_OrgTrx_ID(original.getAD_OrgTrx_ID());
		setUser1_ID(original.getUser1_ID());
		setUser2_ID(original.getUser2_ID());

		// DropShipment
		setIsDropShip(original.isDropShip());
		setDropShip_BPartner_ID(original.getDropShip_BPartner_ID());
		setDropShip_Location_ID(original.getDropShip_Location_ID());
		setDropShip_User_ID(original.getDropShip_User_ID());

	}	//	MInOut


	/**	Lines					*/
	private MInOutLine[]	m_lines = null;
	/** Confirmations			*/
	private MInOutConfirm[]	m_confirms = null;
	/** BPartner				*/
	private MBPartner		m_partner = null;


	/**
	 * 	Get Document Status
	 *	@return Document Status Clear Text
	 */
	public String getDocStatusName()
	{
		return MRefList.getListName(getCtx(), 131, getDocStatus());
	}	//	getDocStatusName

	/**
	 * 	Add to Description
	 *	@param description text
	 */
	public void addDescription (String description)
	{
		String desc = getDescription();
		if (desc == null)
			setDescription(description);
		else
			setDescription(desc + " | " + description);
	}	//	addDescription

	/**
	 *	String representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MInOut[")
			.append (get_ID()).append("-").append(getDocumentNo())
			.append(",DocStatus=").append(getDocStatus())
			.append ("]");
		return sb.toString ();
	}	//	toString

	/**
	 * 	Get Document Info
	 *	@return document info (untranslated)
	 */
	public String getDocumentInfo()
	{
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		return dt.getName() + " " + getDocumentNo();
	}	//	getDocumentInfo

	/**
	 * 	Create PDF
	 *	@return File or null
	 */
	public File createPDF ()
	{
		try
		{
			File temp = File.createTempFile(get_TableName()+get_ID()+"_", ".pdf");
			return createPDF (temp);
		}
		catch (Exception e)
		{
			log.severe("Could not create PDF - " + e.getMessage());
		}
		return null;
	}	//	getPDF

	/**
	 * 	Create PDF file
	 *	@param file output file
	 *	@return file if success
	 */
	public File createPDF (File file)
	{
		ReportEngine re = ReportEngine.get (getCtx(), ReportEngine.SHIPMENT, getM_InOut_ID(), get_TrxName());
		if (re == null)
			return null;
		return re.getPDF(file);
	}	//	createPDF

	/**
	 * 	Get Lines of Shipment
	 * 	@param requery refresh from db
	 * 	@return lines
	 */
	public MInOutLine[] getLines (boolean requery)
	{
		if (m_lines != null && !requery) {
			set_TrxName(m_lines, get_TrxName());
			return m_lines;
		}
		List<MInOutLine> list = new Query(getCtx(), I_M_InOutLine.Table_Name, "M_InOut_ID=?", get_TrxName())
		.setParameters(getM_InOut_ID())
		.setOrderBy(MInOutLine.COLUMNNAME_Line)
		.list();
		//
		m_lines = new MInOutLine[list.size()];
		list.toArray(m_lines);
		return m_lines;
	}	//	getMInOutLines

	/**
	 * 	Get Lines of Shipment
	 * 	@return lines
	 */
	public MInOutLine[] getLines()
	{
		return getLines(false);
	}	//	getLines


	/**
	 * 	Get Confirmations
	 * 	@param requery requery
	 *	@return array of Confirmations
	 */
	public MInOutConfirm[] getConfirmations(boolean requery)
	{
		if (m_confirms != null && !requery)
		{
			set_TrxName(m_confirms, get_TrxName());
			return m_confirms;
		}
		List<MInOutConfirm> list = new Query(getCtx(), I_M_InOutConfirm.Table_Name, "M_InOut_ID=?", get_TrxName())
		.setParameters(getM_InOut_ID())
		.list();
		m_confirms = new MInOutConfirm[list.size ()];
		list.toArray (m_confirms);
		return m_confirms;
	}	//	getConfirmations


	/**
	 * 	Copy Lines From other Shipment
	 *	@param otherShipment shipment
	 *	@param counter set counter info
	 *	@param setOrder set order link
	 *	@return number of lines copied
	 */
	public int copyLinesFrom (MInOut otherShipment, boolean counter, boolean setOrder)
	{
		if (isProcessed() || isPosted() || otherShipment == null)
			return 0;
		MInOutLine[] fromLines = otherShipment.getLines(false);
		int count = 0;
		for (int i = 0; i < fromLines.length; i++)
		{
			MInOutLine line = new MInOutLine (this);
			MInOutLine fromLine = fromLines[i];
			line.set_TrxName(get_TrxName());
			if (counter)	//	header
				PO.copyValues(fromLine, line, getAD_Client_ID(), getAD_Org_ID());
			else
				PO.copyValues(fromLine, line, fromLine.getAD_Client_ID(), fromLine.getAD_Org_ID());
			line.setM_InOut_ID(getM_InOut_ID());
			line.set_ValueNoCheck ("M_InOutLine_ID", I_ZERO);	//	new
			//	Reset
			if (!setOrder)
			{
				line.setC_OrderLine_ID(0);
				line.setM_RMALine_ID(0);  // Reset RMA Line
			}
			if (!counter)
				line.setM_AttributeSetInstance_ID(0);
			line.setM_AttributeSetInstance_ID(fromLine.getM_AttributeSetInstance_ID() );//faaguilar OFB fix . original 0
		//	line.setS_ResourceAssignment_ID(0);
			line.setRef_InOutLine_ID(0);
			line.setIsInvoiced(false);
			//
			line.setConfirmedQty(Env.ZERO);
			line.setPickedQty(Env.ZERO);
			line.setScrappedQty(Env.ZERO);
			line.setTargetQty(Env.ZERO);
			//	Set Locator based on header Warehouse
			if (getM_Warehouse_ID() != otherShipment.getM_Warehouse_ID())
			{
				line.setM_Locator_ID(0);
				line.setM_Locator_ID(Env.ZERO);
			}
			//
			if (counter)
			{
				line.setRef_InOutLine_ID(fromLine.getM_InOutLine_ID());
				if (fromLine.getC_OrderLine_ID() != 0)
				{
					MOrderLine peer = new MOrderLine (getCtx(), fromLine.getC_OrderLine_ID(), get_TrxName());
					if (peer.getRef_OrderLine_ID() != 0)
						line.setC_OrderLine_ID(peer.getRef_OrderLine_ID());
				}
				//RMALine link
				if (fromLine.getM_RMALine_ID() != 0)
				{
					MRMALine peer = new MRMALine (getCtx(), fromLine.getM_RMALine_ID(), get_TrxName());
					if (peer.getRef_RMALine_ID() > 0)
						line.setM_RMALine_ID(peer.getRef_RMALine_ID());
				}
			}
			//
			line.setProcessed(false);
			if (line.save(get_TrxName()))
				count++;
			//	Cross Link
			if (counter)
			{
				fromLine.setRef_InOutLine_ID(line.getM_InOutLine_ID());
				fromLine.save(get_TrxName());
			}
		}
		if (fromLines.length != count)
			log.log(Level.SEVERE, "Line difference - From=" + fromLines.length + " <> Saved=" + count);
		return count;
	}	//	copyLinesFrom

	/** Reversal Flag		*/
	private boolean m_reversal = false;

	/**
	 * 	Set Reversal
	 *	@param reversal reversal
	 */
	private void setReversal(boolean reversal)
	{
		m_reversal = reversal;
	}	//	setReversal
	/**
	 * 	Is Reversal
	 *	@return reversal
	 */
	public boolean isReversal()
	{
		return m_reversal;
	}	//	isReversal

	/**
	 * 	Set Processed.
	 * 	Propagate to Lines/Taxes
	 *	@param processed processed
	 */
	public void setProcessed (boolean processed)
	{
		super.setProcessed (processed);
		if (get_ID() == 0)
			return;
		String sql = "UPDATE M_InOutLine SET Processed='"
			+ (processed ? "Y" : "N")
			+ "' WHERE M_InOut_ID=" + getM_InOut_ID();
		int noLine = DB.executeUpdate(sql, get_TrxName());
		m_lines = null;
		log.fine(processed + " - Lines=" + noLine);
	}	//	setProcessed

	/**
	 * 	Get BPartner
	 *	@return partner
	 */
	public MBPartner getBPartner()
	{
		if (m_partner == null)
			m_partner = new MBPartner (getCtx(), getC_BPartner_ID(), get_TrxName());
		return m_partner;
	}	//	getPartner

	/**
	 * 	Set Document Type
	 * 	@param DocBaseType doc type MDocType.DOCBASETYPE_
	 */
	public void setC_DocType_ID (String DocBaseType)
	{
		String sql = "SELECT C_DocType_ID FROM C_DocType "
			+ "WHERE AD_Client_ID=? AND DocBaseType=?"
			+ " AND IsActive='Y'"
			+ " AND IsSOTrx='" + (isSOTrx() ? "Y" : "N") + "' "
			+ "ORDER BY IsDefault DESC";
		int C_DocType_ID = DB.getSQLValue(null, sql, getAD_Client_ID(), DocBaseType);
		if (C_DocType_ID <= 0)
			log.log(Level.SEVERE, "Not found for AC_Client_ID="
				+ getAD_Client_ID() + " - " + DocBaseType);
		else
		{
			log.fine("DocBaseType=" + DocBaseType + " - C_DocType_ID=" + C_DocType_ID);
			setC_DocType_ID (C_DocType_ID);
			boolean isSOTrx = MDocType.DOCBASETYPE_MaterialDelivery.equals(DocBaseType);
			setIsSOTrx (isSOTrx);
			
			/**
			 * faaguilar OFB
			 * seteo correcto de venta o compra*/
			//faaaguilar OFB iSSOTrx begin
			setIsSOTrx( new MDocType(getCtx(),C_DocType_ID,get_TrxName() ).isSOTrx() );
			//faaaguilar OFB iSSOTrx end
		}
	}	//	setC_DocType_ID

	/**
	 * 	Set Default C_DocType_ID.
	 * 	Based on SO flag
	 */
	public void setC_DocType_ID()
	{
		if (isSOTrx())
			setC_DocType_ID(MDocType.DOCBASETYPE_MaterialDelivery);
		else
			setC_DocType_ID(MDocType.DOCBASETYPE_MaterialReceipt);
	}	//	setC_DocType_ID

	/**
	 * 	Set Business Partner Defaults & Details
	 * 	@param bp business partner
	 */
	public void setBPartner (MBPartner bp)
	{
		if (bp == null)
			return;

		setC_BPartner_ID(bp.getC_BPartner_ID());

		//	Set Locations
		MBPartnerLocation[] locs = bp.getLocations(false);
		if (locs != null)
		{
			for (int i = 0; i < locs.length; i++)
			{
				if (locs[i].isShipTo())
					setC_BPartner_Location_ID(locs[i].getC_BPartner_Location_ID());
			}
			//	set to first if not set
			if (getC_BPartner_Location_ID() == 0 && locs.length > 0)
				setC_BPartner_Location_ID(locs[0].getC_BPartner_Location_ID());
		}
		if (getC_BPartner_Location_ID() == 0)
			log.log(Level.SEVERE, "Has no To Address: " + bp);

		//	Set Contact
		MUser[] contacts = bp.getContacts(false);
		if (contacts != null && contacts.length > 0)	//	get first User
			setAD_User_ID(contacts[0].getAD_User_ID());
	}	//	setBPartner

	/**
	 * 	Create the missing next Confirmation
	 */
	public void createConfirmation()
	{
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		boolean pick = dt.isPickQAConfirm();
		boolean ship = dt.isShipConfirm();
		//	Nothing to do
		if (!pick && !ship)
		{
			log.fine("No need");
			return;
		}

		//	Create Both .. after each other
		if (pick && ship)
		{
			boolean havePick = false;
			boolean haveShip = false;
			MInOutConfirm[] confirmations = getConfirmations(false);
			for (int i = 0; i < confirmations.length; i++)
			{
				MInOutConfirm confirm = confirmations[i];
				if (MInOutConfirm.CONFIRMTYPE_PickQAConfirm.equals(confirm.getConfirmType()))
				{
					if (!confirm.isProcessed())		//	wait intil done
					{
						log.fine("Unprocessed: " + confirm);
						return;
					}
					havePick = true;
				}
				else if (MInOutConfirm.CONFIRMTYPE_ShipReceiptConfirm.equals(confirm.getConfirmType()))
					haveShip = true;
			}
			//	Create Pick
			if (!havePick)
			{
				MInOutConfirm.create (this, MInOutConfirm.CONFIRMTYPE_PickQAConfirm, false);
				return;
			}
			//	Create Ship
			if (!haveShip)
			{
				MInOutConfirm.create (this, MInOutConfirm.CONFIRMTYPE_ShipReceiptConfirm, false);
				return;
			}
			return;
		}
		//	Create just one
		if (pick)
			MInOutConfirm.create (this, MInOutConfirm.CONFIRMTYPE_PickQAConfirm, true);
		else if (ship)
			MInOutConfirm.create (this, MInOutConfirm.CONFIRMTYPE_ShipReceiptConfirm, true);
	}	//	createConfirmation
	
	private void voidConfirmations()
	{
		for(MInOutConfirm confirm : getConfirmations(true))
		{
			if (!confirm.isProcessed())
			{
				if (!confirm.processIt(MInOutConfirm.DOCACTION_Void))
					throw new AdempiereException(confirm.getProcessMsg());
				confirm.saveEx();
			}
		}
	}


	/**
	 * 	Set Warehouse and check/set Organization
	 *	@param M_Warehouse_ID id
	 */
	public void setM_Warehouse_ID (int M_Warehouse_ID)
	{
		if (M_Warehouse_ID == 0)
		{
			log.severe("Ignored - Cannot set AD_Warehouse_ID to 0");
			return;
		}
		super.setM_Warehouse_ID (M_Warehouse_ID);
		//
		/*faaguilar OFB codigo comentado
		 * para que no oblique al warehouse pertenecer a la org del movimiento
		 * **/
		/*MWarehouse wh = MWarehouse.get(getCtx(), getM_Warehouse_ID());
		if (wh.getAD_Org_ID() != getAD_Org_ID())
		{
			log.warning("M_Warehouse_ID=" + M_Warehouse_ID
				+ ", Overwritten AD_Org_ID=" + getAD_Org_ID() + "->" + wh.getAD_Org_ID());
			setAD_Org_ID(wh.getAD_Org_ID());
		}*/
	}	//	setM_Warehouse_ID


	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true or false
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		//	Warehouse Org
		/*faaguilar OFB codigo comentado sacada validacion de org
		if (newRecord)
		{
			MWarehouse wh = MWarehouse.get(getCtx(), getM_Warehouse_ID());
			if (wh.getAD_Org_ID() != getAD_Org_ID())
			{
				log.saveError("WarehouseOrgConflict", "");
				return false;
			}
		}
		*/
		/**
		 * faaguilar OFB
		 * chequea que el tipo de movimiento
		 * segun las caracteristicas del movimiento sea el correcto*/
		//Begin faaguilar OFB Ship & Returns begin
		
		boolean IsSOTrx =isSOTrx();
		if(getDocBase().equals("MMS"))
		{
			if (IsSOTrx)
				setMovementType("C-");
			else
				setMovementType("V-");
		}
		else if (getDocBase().equals("MMR"))
		{
			if (IsSOTrx)
				setMovementType("C+");
			else
				setMovementType("V+");
		}
		//End Faaguilar OFB Ship & Returns end

        // Shipment/Receipt can have either Order/RMA (For Movement type)
        if (getC_Order_ID() != 0 && getM_RMA_ID() != 0)
        {
            log.saveError("OrderOrRMA", "");
            return false;
        }

		//	Shipment - Needs Order/RMA
        if(!OFBForward.NoValidateOrderShipment())
        {
			if (!getMovementType().contentEquals(MInOut.MOVEMENTTYPE_CustomerReturns) && isSOTrx() && getC_Order_ID() == 0 && getM_RMA_ID() == 0)
			{
				log.saveError("FillMandatory", Msg.translate(getCtx(), "C_Order_ID"));
				return false;
			}
        }

        if (isSOTrx() && getM_RMA_ID() != 0)
        {
            // Set Document and Movement type for this Receipt
            MRMA rma = new MRMA(getCtx(), getM_RMA_ID(), get_TrxName());
            MDocType docType = MDocType.get(getCtx(), rma.getC_DocType_ID());
            setC_DocType_ID(docType.getC_DocTypeShipment_ID());
        }
        
        //faaguilar OFB advertencia warehouse begin
        if(this.getC_Order_ID()>0){
        	int originalW_ID=this.getC_Order().getM_Warehouse_ID();
        	if(originalW_ID!=this.getM_Warehouse_ID())
        		log.saveWarning("Advertencia", "");
        	
        }
      //faaguilar OFB advertencia warehouse end

		return true;
	}	//	beforeSave

	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return success
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (!success || newRecord)
			return success;

		if (is_ValueChanged("AD_Org_ID"))
		{
			String sql = "UPDATE M_InOutLine ol"
				+ " SET AD_Org_ID ="
					+ "(SELECT AD_Org_ID"
					+ " FROM M_InOut o WHERE ol.M_InOut_ID=o.M_InOut_ID) "
				+ "WHERE M_InOut_ID=" + getC_Order_ID();
			int no = DB.executeUpdate(sql, get_TrxName());
			log.fine("Lines -> #" + no);
		}
		return true;
	}	//	afterSave


	/**************************************************************************
	 * 	Process document
	 *	@param processAction document action
	 *	@return true if performed
	 */
	public boolean processIt (String processAction)
	{
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
		return engine.processIt (processAction, getDocAction());
	}	//	process

	/**	Process Message 			*/
	private String		m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean		m_justPrepared = false;

	/**
	 * 	Unlock Document.
	 * 	@return true if success
	 */
	public boolean unlockIt()
	{
		log.info(toString());
		setProcessing(false);
		return true;
	}	//	unlockIt

	/**
	 * 	Invalidate Document
	 * 	@return true if success
	 */
	public boolean invalidateIt()
	{
		log.info(toString());
		setDocAction(DOCACTION_Prepare);
		return true;
	}	//	invalidateIt

	/**
	 *	Prepare Document
	 * 	@return new status (In Progress or Invalid)
	 */
	public String prepareIt()
	{
		log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;

		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());

		//  Order OR RMA can be processed on a shipment/receipt
		if (getC_Order_ID() != 0 && getM_RMA_ID() != 0)
		{
		    m_processMsg = "@OrderOrRMA@";
		    return DocAction.STATUS_Invalid;
		}
		//	Std Period open?
		if (!MPeriod.isOpen(getCtx(), getDateAcct(), dt.getDocBaseType(), getAD_Org_ID()))
		{
			m_processMsg = "@PeriodClosed@";
			return DocAction.STATUS_Invalid;
		}

		//	Credit Check
		if (isSOTrx() && !isReversal())
		{
			I_C_Order order = getC_Order();
			if (order != null && MDocType.DOCSUBTYPESO_PrepayOrder.equals(order.getC_DocType().getDocSubTypeSO())
					&& !MSysConfig.getBooleanValue("CHECK_CREDIT_ON_PREPAY_ORDER", true, getAD_Client_ID(), getAD_Org_ID())) {
				// ignore -- don't validate Prepay Orders depending on sysconfig parameter
			} else {
				MBPartner bp = new MBPartner (getCtx(), getC_BPartner_ID(), get_TrxName());
				if (MBPartner.SOCREDITSTATUS_CreditStop.equals(bp.getSOCreditStatus()))
				{
					m_processMsg = "@BPartnerCreditStop@ - @TotalOpenBalance@="
						+ bp.getTotalOpenBalance()
						+ ", @SO_CreditLimit@=" + bp.getSO_CreditLimit();
					return DocAction.STATUS_Invalid;
				}
				if (MBPartner.SOCREDITSTATUS_CreditHold.equals(bp.getSOCreditStatus()))
				{
					m_processMsg = "@BPartnerCreditHold@ - @TotalOpenBalance@="
						+ bp.getTotalOpenBalance()
						+ ", @SO_CreditLimit@=" + bp.getSO_CreditLimit();
					return DocAction.STATUS_Invalid;
				}
				
				/***faaguilar OFB original code commented
				 BigDecimal notInvoicedAmt = MBPartner.getNotInvoicedAmt(getC_BPartner_ID());
				if (MBPartner.SOCREDITSTATUS_CreditHold.equals(bp.getSOCreditStatus(notInvoicedAmt)))
				{
					m_processMsg = "@BPartnerOverSCreditHold@ - @TotalOpenBalance@="
						+ bp.getTotalOpenBalance() + ", @NotInvoicedAmt@=" + notInvoicedAmt
						+ ", @SO_CreditLimit@=" + bp.getSO_CreditLimit();
					return DocAction.STATUS_Invalid;
				}*/
			}
		}

		//	Lines
		MInOutLine[] lines = getLines(true);
		if (lines == null || lines.length == 0)
		{
			m_processMsg = "@NoLines@";
			return DocAction.STATUS_Invalid;
		}
		
		BigDecimal Volume = Env.ZERO;
		BigDecimal Weight = Env.ZERO;

		//	Mandatory Attributes
		for (int i = 0; i < lines.length; i++)
		{
			MInOutLine line = lines[i];
			MProduct product = line.getProduct();
			if (product != null)
			{
				Volume = Volume.add(product.getVolume().multiply(line.getMovementQty()));
				Weight = Weight.add(product.getWeight().multiply(line.getMovementQty()));
			}
			//
			if (line.getM_AttributeSetInstance_ID() != 0)
				continue;
			if (product != null && product.isASIMandatory(isSOTrx()))
			{
				m_processMsg = "@M_AttributeSet_ID@ @IsMandatory@ (@Line@ #" + lines[i].getLine() +
								", @M_Product_ID@=" + product.getValue() + ")";
				return DocAction.STATUS_Invalid;
			}
			
		}
		
		//faaguilar OFB chequeo de rangos limite precio cantidad begin
		if(!isReversal()){
			m_processMsg=rangos();
			if(m_processMsg!=null && m_processMsg.length()>1){
				return DocAction.STATUS_Invalid;
			}
		}
		//faaguilar OFB chequeo de rangos limite precio cantidad end
		
		//faaguilar OFB begin validacion stock y material policys lote begin
		if(!getDocStatus().equals(MInOut.DOCSTATUS_InProgress))
			for (int i = 0; i < lines.length; i++)
			{
				MInOutLine line = lines[i];
					if (line.getM_Product() != null	&& line.getM_Product().isStocked() )
							if(!isReversal())
							{
								checkMaterialPolicy(line);
							}
			}
		
		m_processMsg = checkStock();
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		//faaguilar OFB begin validacion lote end
		
		setVolume(Volume);
		setWeight(Weight);

		if (!isReversal())	//	don't change reversal
		{
			createConfirmation();
		}

		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;

		m_justPrepared = true;
		if (!DOCACTION_Complete.equals(getDocAction()))
			setDocAction(DOCACTION_Complete);
		return DocAction.STATUS_InProgress;
	}	//	prepareIt

	/**
	 * 	Approve Document
	 * 	@return true if success
	 */
	public boolean  approveIt()
	{
		log.info(toString());
		setIsApproved(true);
		return true;
	}	//	approveIt

	/**
	 * 	Reject Approval
	 * 	@return true if success
	 */
	public boolean rejectIt()
	{
		log.info(toString());
		setIsApproved(false);
		return true;
	}	//	rejectIt

	/**
	 * 	Complete Document
	 * 	@return new status (Complete, In Progress, Invalid, Waiting ..)
	 */
	public String completeIt()
	{
		//	Re-Check
		if (!m_justPrepared)
		{
			String status = prepareIt();
			if (!DocAction.STATUS_InProgress.equals(status))
				return status;
		}

		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		m_processMsg = checkPolicy();//faaguilar OFB
		if (m_processMsg != null) //faaguilar OFB
			return DocAction.STATUS_Invalid; //faaguilar OFB
		
		//faaguilar OFB crea orden que representa carpeta de importacion begin
		if(!createOrder())
		{
			m_processMsg = "No existe una Orden de Compra de Importacion definida";
			return DocAction.STATUS_Invalid;
		}
		//faaguilar OFB crea orden que representa carpeta de importacion End
		
		//	Outstanding (not processed) Incoming Confirmations ?
		MInOutConfirm[] confirmations = getConfirmations(true);
		for (int i = 0; i < confirmations.length; i++)
		{
			MInOutConfirm confirm = confirmations[i];
			if (!confirm.isProcessed())
			{
				if (MInOutConfirm.CONFIRMTYPE_CustomerConfirmation.equals(confirm.getConfirmType()))
					continue;
				//
				m_processMsg = "Open @M_InOutConfirm_ID@: " +
					confirm.getConfirmTypeName() + " - " + confirm.getDocumentNo();
				return DocAction.STATUS_InProgress;
			}
		}


		//	Implicit Approval
		if (!isApproved())
			approveIt();
		log.info(toString());
		StringBuffer info = new StringBuffer();
		
		createAsset();//faaguilar OFB 

		//	For all lines
		MInOutLine[] lines = getLines(false);
		for (int lineIndex = 0; lineIndex < lines.length; lineIndex++)
		{
			MInOutLine sLine = lines[lineIndex];
			MProduct product = sLine.getProduct();

			//	Qty & Type
			String MovementType = getMovementType();
			BigDecimal Qty = sLine.getMovementQty();
			if (MovementType.charAt(1) == '-')	//	C- Customer Shipment - V- Vendor Return
				Qty = Qty.negate();
			BigDecimal QtySO = Env.ZERO;
			BigDecimal QtyPO = Env.ZERO;

			//	Update Order Line
			MOrderLine oLine = null;
			if (sLine.getC_OrderLine_ID() != 0)
			{
				oLine = new MOrderLine (getCtx(), sLine.getC_OrderLine_ID(), get_TrxName());
				log.fine("OrderLine - Reserved=" + oLine.getQtyReserved()
					+ ", Delivered=" + oLine.getQtyDelivered());
				if (isSOTrx())
					QtySO = sLine.getMovementQty();
				else
					QtyPO = sLine.getMovementQty();
			}


            // Load RMA Line
            MRMALine rmaLine = null;

            if (sLine.getM_RMALine_ID() != 0)
            {
                rmaLine = new MRMALine(getCtx(), sLine.getM_RMALine_ID(), get_TrxName());
            }

			log.info("Line=" + sLine.getLine() + " - Qty=" + sLine.getMovementQty());

			//	Stock Movement - Counterpart MOrder.reserveStock
			if (product != null
				&& product.isStocked() )
			{
				//Ignore the Material Policy when is Reverse Correction
				if(!isReversal())
				{
					checkMaterialPolicy(sLine);
				}

				log.fine("Material Transaction");
				MTransaction mtrx = null;
				//same warehouse in order and receipt?
				boolean sameWarehouse = true;
				//	Reservation ASI - assume none
				int reservationAttributeSetInstance_ID = 0; // sLine.getM_AttributeSetInstance_ID();
				if (oLine != null) {
					reservationAttributeSetInstance_ID = oLine.getM_AttributeSetInstance_ID();
					sameWarehouse = oLine.getM_Warehouse_ID()==getM_Warehouse_ID();
				}
				//
				if (sLine.getM_AttributeSetInstance_ID() == 0)
				{
					MInOutLineMA mas[] = MInOutLineMA.get(getCtx(),
						sLine.getM_InOutLine_ID(), get_TrxName());
					for (int j = 0; j < mas.length; j++)
					{
						MInOutLineMA ma = mas[j];
						BigDecimal QtyMA = ma.getMovementQty();
						if (MovementType.charAt(1) == '-')	//	C- Customer Shipment - V- Vendor Return
							QtyMA = QtyMA.negate();
						BigDecimal reservedDiff = Env.ZERO;
						BigDecimal orderedDiff = Env.ZERO;
						if (sLine.getC_OrderLine_ID() != 0)
						{
							if (isSOTrx())
								reservedDiff = ma.getMovementQty().negate();
							else
								orderedDiff = ma.getMovementQty().negate();
						}
						//faaguilar OFB fix retorno desde clientes begin
						if(getMovementType().equals(MInOut.MOVEMENTTYPE_CustomerReturns))
							reservedDiff=Env.ZERO;
						
						if(!isSOTrx())
							if(QtyPO.compareTo(orderedDiff)<0)//recibido mayor que comprado
									orderedDiff = QtyPO;
						//faaguilar OFB fix retorno desde clientes end

						if(existReservationTable() && oLine != null)//faaguilar OFB							
							OFBReservation(oLine.getM_Warehouse_ID(),sLine.getM_Product_ID(),orderedDiff,reservedDiff);//faaguilar OFB
						
						//	Update Storage - see also VMatch.createMatchRecord
						if (!MStorage.add(getCtx(), getM_Warehouse_ID(),
								sLine.getM_Locator_ID(),
								sLine.getM_Product_ID(),
								ma.getM_AttributeSetInstance_ID(), reservationAttributeSetInstance_ID,
								QtyMA,
								sameWarehouse ? reservedDiff : Env.ZERO,
								sameWarehouse ? orderedDiff : Env.ZERO,
								get_TrxName()))
						{
							m_processMsg = "Cannot correct Inventory (MA)";
							return DocAction.STATUS_Invalid;
						}
						if (!sameWarehouse) {
							//correct qtyOrdered in warehouse of order
							MWarehouse wh = MWarehouse.get(getCtx(), oLine.getM_Warehouse_ID());
							if (!MStorage.add(getCtx(), oLine.getM_Warehouse_ID(),
									wh.getDefaultLocator().getM_Locator_ID(),
									sLine.getM_Product_ID(),
									ma.getM_AttributeSetInstance_ID(), reservationAttributeSetInstance_ID,
									Env.ZERO, reservedDiff, orderedDiff, get_TrxName()))
								{
									m_processMsg = "Cannot correct Inventory (MA) in order warehouse";
									return DocAction.STATUS_Invalid;
								}
						}
						
						
						//	Create Transaction
						mtrx = new MTransaction (getCtx(), sLine.getAD_Org_ID(),
							MovementType, sLine.getM_Locator_ID(),
							sLine.getM_Product_ID(), ma.getM_AttributeSetInstance_ID(),
							QtyMA, getMovementDate(), get_TrxName());
						mtrx.setM_InOutLine_ID(sLine.getM_InOutLine_ID());
						if (!mtrx.save())
						{
							m_processMsg = "Could not create Material Transaction (MA)";
							return DocAction.STATUS_Invalid;
						}
					}
				}
				//	sLine.getM_AttributeSetInstance_ID() != 0
				if (mtrx == null)
				{
					BigDecimal reservedDiff = sameWarehouse ? QtySO.negate() : Env.ZERO;
					BigDecimal orderedDiff = sameWarehouse ? QtyPO.negate(): Env.ZERO;

					if(existReservationTable() && oLine != null)//faaguilar OFB
						OFBReservation(oLine.getM_Warehouse_ID(),sLine.getM_Product_ID(),orderedDiff,reservedDiff);//faaguilar OFB
					
					//	Fallback: Update Storage - see also VMatch.createMatchRecord
					if (!MStorage.add(getCtx(), getM_Warehouse_ID(),
						sLine.getM_Locator_ID(),
						sLine.getM_Product_ID(),
						sLine.getM_AttributeSetInstance_ID(), reservationAttributeSetInstance_ID,
						Qty, reservedDiff, orderedDiff, get_TrxName()))
					{
						m_processMsg = "Cannot correct Inventory";
						return DocAction.STATUS_Invalid;
					}
					if (!sameWarehouse) {
						//correct qtyOrdered in warehouse of order
						MWarehouse wh = MWarehouse.get(getCtx(), oLine.getM_Warehouse_ID());
						if (!MStorage.add(getCtx(), oLine.getM_Warehouse_ID(),
								wh.getDefaultLocator().getM_Locator_ID(),
								sLine.getM_Product_ID(),
								sLine.getM_AttributeSetInstance_ID(), reservationAttributeSetInstance_ID,
								Env.ZERO, QtySO.negate(), QtyPO.negate(), get_TrxName()))
							{
								m_processMsg = "Cannot correct Inventory";
								return DocAction.STATUS_Invalid;
							}
					}
					//	FallBack: Create Transaction
					mtrx = new MTransaction (getCtx(), sLine.getAD_Org_ID(),
						MovementType, sLine.getM_Locator_ID(),
						sLine.getM_Product_ID(), sLine.getM_AttributeSetInstance_ID(),
						Qty, getMovementDate(), get_TrxName());
					mtrx.setM_InOutLine_ID(sLine.getM_InOutLine_ID());
					if (!mtrx.save())
					{
						m_processMsg = CLogger.retrieveErrorString("Could not create Material Transaction");
						return DocAction.STATUS_Invalid;
					}
				}
			}	//	stock movement

			//	Correct Order Line
			if (product != null && oLine != null)		//	other in VMatch.createMatchRecord
				if(oLine.getQtyReserved().compareTo(sLine.getMovementQty())>=0) //faaguilar OFB
					oLine.setQtyReserved(oLine.getQtyReserved().subtract(sLine.getMovementQty()));
				else
					oLine.setQtyReserved(Env.ZERO);//faaguilar OFB

			//	Update Sales Order Line
			if (oLine != null)
			{
				if (isSOTrx()							//	PO is done by Matching
					|| sLine.getM_Product_ID() == 0)	//	PO Charges, empty lines
				{
					if (isSOTrx())//despacho o devolucion de cliente
					{
						if(Qty.signum()<0)//faaguilar OFB entrega
							oLine.setQtyDelivered(oLine.getQtyDelivered().subtract(Qty));
						else //devolucion de cliente o anulacion de despacho
						{
							if(oLine.getQtyOrdered().compareTo(oLine.getQtyDelivered().add(Qty))>=0)
								//ininoles se ocupa cantidad de la linea y no calculada para que no sume la cantidad de los documentos anulados
								oLine.setQtyDelivered(oLine.getQtyDelivered().add(sLine.getMovementQty()));//faaguilar OFB devolucion 
							else
								oLine.setQtyDelivered(oLine.getQtyDelivered().subtract(Qty));
						}
					}
					else// recibo o devolucion a proveedor
					{
						if(Qty.signum()>0)//recibo de proveedor
							oLine.setQtyDelivered(oLine.getQtyDelivered().add(Qty));
						else//anulacion de recibo
							oLine.setQtyDelivered(oLine.getQtyDelivered().add(Qty));
						
					}
					oLine.setDateDelivered(getMovementDate());	//	overwrite=last
					
				}
				if (!oLine.save())
				{
					m_processMsg = "Could not update Order Line";
					return DocAction.STATUS_Invalid;
				}
				else
					log.fine("OrderLine -> Reserved=" + oLine.getQtyReserved()
						+ ", Delivered=" + oLine.getQtyReserved());
			}
            //  Update RMA Line Qty Delivered
            else if (rmaLine != null)
            {
                if (isSOTrx())
                {
                    rmaLine.setQtyDelivered(rmaLine.getQtyDelivered().add(Qty));
                }
                else
                {
                    rmaLine.setQtyDelivered(rmaLine.getQtyDelivered().subtract(Qty));
                }
                if (!rmaLine.save())
                {
                    m_processMsg = "Could not update RMA Line";
                    return DocAction.STATUS_Invalid;
                }
            }

			//	Create Asset for SO
			if (product != null
				&& isSOTrx()
				&& product.isCreateAsset()
				&& sLine.getMovementQty().signum() > 0
				&& !isReversal())
			{
				log.fine("Asset");
				info.append("@A_Asset_ID@: ");
				int noAssets = sLine.getMovementQty().intValue();
				if (!product.isOneAssetPerUOM())
					noAssets = 1;
				for (int i = 0; i < noAssets; i++)
				{
					if (i > 0)
						info.append(" - ");
					int deliveryCount = i+1;
					if (!product.isOneAssetPerUOM())
						deliveryCount = 0;
					MAsset asset = new MAsset (this, sLine, deliveryCount);
					if (!asset.save(get_TrxName()))
					{
						m_processMsg = "Could not create Asset";
						return DocAction.STATUS_Invalid;
					}
					info.append(asset.getValue());
				}
			}	//	Asset


			//	Matching
			if (!isSOTrx()
				&& sLine.getM_Product_ID() != 0
				&& !isReversal())
			{
				BigDecimal matchQty = sLine.getMovementQty();
				//	Invoice - Receipt Match (requires Product)
				MInvoiceLine iLine = MInvoiceLine.getOfInOutLine (sLine);
				if (iLine != null && iLine.getM_Product_ID() != 0)
				{
					if (matchQty.compareTo(iLine.getQtyInvoiced())>0)
						matchQty = iLine.getQtyInvoiced();

					MMatchInv[] matches = MMatchInv.get(getCtx(),
						sLine.getM_InOutLine_ID(), iLine.getC_InvoiceLine_ID(), get_TrxName());
					if (matches == null || matches.length == 0)
					{
						MMatchInv inv = new MMatchInv (iLine, getMovementDate(), matchQty);
						if (sLine.getM_AttributeSetInstance_ID() != iLine.getM_AttributeSetInstance_ID())
						{
							iLine.setM_AttributeSetInstance_ID(sLine.getM_AttributeSetInstance_ID());
							iLine.save();	//	update matched invoice with ASI
							inv.setM_AttributeSetInstance_ID(sLine.getM_AttributeSetInstance_ID());
						}
						boolean isNewMatchInv = false;
						if (inv.get_ID() == 0)
							isNewMatchInv = true;
						if (!inv.save(get_TrxName()))
						{
							m_processMsg = CLogger.retrieveErrorString("Could not create Inv Matching");
							return DocAction.STATUS_Invalid;
						}
						if (isNewMatchInv)
							addDocsPostProcess(inv);
					}
				}

				//	Link to Order
				if (sLine.getC_OrderLine_ID() != 0)
				{
					log.fine("PO Matching");
					//	Ship - PO
					MMatchPO po = MMatchPO.create (null, sLine, getMovementDate(), matchQty);
					boolean isNewMatchPO = false;
					if (po.get_ID() == 0)
						isNewMatchPO = true;
					if (!po.save(get_TrxName()))
					{
						m_processMsg = "Could not create PO Matching";
						return DocAction.STATUS_Invalid;
					}
					if (isNewMatchPO)
						addDocsPostProcess(po);
					//	Update PO with ASI
					if (   oLine != null && oLine.getM_AttributeSetInstance_ID() == 0
						&& sLine.getMovementQty().compareTo(oLine.getQtyOrdered()) == 0) //  just if full match [ 1876965 ]
					{
						oLine.setM_AttributeSetInstance_ID(sLine.getM_AttributeSetInstance_ID());
						oLine.save(get_TrxName());
					}
				}
				else	//	No Order - Try finding links via Invoice
				{
					//	Invoice has an Order Link
					if (iLine != null && iLine.getC_OrderLine_ID() != 0)
					{
						//	Invoice is created before  Shipment
						log.fine("PO(Inv) Matching");
						//	Ship - Invoice
						MMatchPO po = MMatchPO.create (iLine, sLine,
							getMovementDate(), matchQty);
						boolean isNewMatchPO = false;
						if (po.get_ID() == 0)
							isNewMatchPO = true;
						if (!po.save(get_TrxName()))
						{
							m_processMsg = "Could not create PO(Inv) Matching";
							return DocAction.STATUS_Invalid;
						}
						if (isNewMatchPO)
							addDocsPostProcess(po);
						//	Update PO with ASI
						oLine = new MOrderLine (getCtx(), po.getC_OrderLine_ID(), get_TrxName());
						if (   oLine != null && oLine.getM_AttributeSetInstance_ID() == 0
							&& sLine.getMovementQty().compareTo(oLine.getQtyOrdered()) == 0) //  just if full match [ 1876965 ]
						{
							oLine.setM_AttributeSetInstance_ID(sLine.getM_AttributeSetInstance_ID());
							oLine.save(get_TrxName());
						}
					}
				}	//	No Order
			}	//	PO Matching

		}	//	for all lines

		//	Counter Documents
		MInOut counter = createCounterDoc();
		if (counter != null)
			info.append(" - @CounterDoc@: @M_InOut_ID@=").append(counter.getDocumentNo());

		//  Drop Shipments
		MInOut dropShipment = createDropShipment();
		if (dropShipment != null)
			info.append(" - @DropShipment@: @M_InOut_ID@=").append(dropShipment.getDocumentNo());
		//	User Validation
		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocAction.STATUS_Invalid;
		}

		// Set the definite document number after completed (if needed)
		setDefiniteDocumentNo();
		
		//modificacion para genenacion de xml al completar ininoles
		try
		{	
			if (OFBForward.GenerateXMLMinOut())
			{
				CreateXML();
			}else if (OFBForward.GenerateXMLMinOutCGProvectis())
			{
				ExportDTEMInOutCGProvectis cg = new ExportDTEMInOutCGProvectis();				
				cg.CreateXMLCG(this);
			}else if (OFBForward.GenerateXMLMinOutFel())
			{
				ExportDTEMInOutFOL cg = new ExportDTEMInOutFOL();				
				cg.CreateXMLCG(this);
			}
		}catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		//end ininoles xml
		
		// si se completa una recepcion asociada a una OC con preventa, se envia aviso
//		StringBuffer sqlDoctosPreventa = new StringBuffer("SELECT Count(*) FROM OV_PRERESERVA WHERE DocStatus = 'CO' AND C_Order_ID = " + getC_Order_ID());
//		int doctosPreventa = DB.getSQLValue(get_TrxName(), sqlDoctosPreventa.toString());
		List<String> listaClientes = new ArrayList<String>();
		PreparedStatement pstPre = DB.prepareStatement("SELECT OV_Prereserva_ID FROM OV_Prereserva WHERE DocStatus = 'CO' AND C_Order_ID = " + getC_Order_ID(), get_TrxName());
		ResultSet res;
		List<String> correosAviso = new ArrayList<String>();
		try {
			res = pstPre.executeQuery();
			while (res.next()) {
				MPrereserva prereserva = new MPrereserva(getCtx(), res.getInt(1), get_TrxName());
				if (prereserva.getC_BPartner_ID() != 0) {
					MBPartner bpartner = MBPartner.get(getCtx(), prereserva.getC_BPartner_ID());
					listaClientes.add(bpartner.getValue() + " - " + bpartner.getName());
				} else {
					listaClientes.add("Multirut Nro " + prereserva.getDocumentNo());
				}
				correosAviso.add(prereserva.getSalesRep().getName()+"@comercialwindsor.cl");
			}
		} catch (SQLException e) {
			log.info("Error al consultar preventas de OC: " + e.toString());
		}
		
//		log.info("doctosPreventa: " + doctosPreventa);
		if (getC_Order().getC_DocType_ID() == 1000047 && listaClientes.size() > 0) { //1000047: Orden de Compra Internacional
			// Enviar notificacion que se completo un recibo de una orden de compra internacional
			StringBuffer cuerpoMensaje = new StringBuffer();
			cuerpoMensaje.append("Se ha recibido OC Internacional N° " + getC_Order().getDocumentNo() + ". Como dicha orden tiene preventas comprometidas para <br />");
			for(String cliente : listaClientes) {
				cuerpoMensaje.append(cliente + "<br />");
			}
			cuerpoMensaje.append(", se recomienda procesar dicha OC a la brevedad posible.");
			MClient M_Client = new MClient(getCtx(),get_TrxName());
			EMail email = M_Client.createEMail("crodriguez@comercialwindsor.cl","Recepción de OC Internacional con preventa de transito " + new Timestamp(System.currentTimeMillis()), cuerpoMensaje.toString(), true);
			EMail.SENT_OK.equals(email.send());
			
			EMail email2 = M_Client.createEMail("agalemiri@comercialwindsor.cl","Recepción de OC Internacional con preventa de transito " + new Timestamp(System.currentTimeMillis()), cuerpoMensaje.toString(), true);
			EMail.SENT_OK.equals(email2.send());
			
			EMail email3 = M_Client.createEMail("aparra@comercialwindsor.cl","Recepción de OC Internacional con preventa de transito " + new Timestamp(System.currentTimeMillis()), cuerpoMensaje.toString(), true);
			EMail.SENT_OK.equals(email3.send());
			
			Set<String> hashSet = new HashSet<String>(correosAviso);
			correosAviso.clear();
			correosAviso.addAll(hashSet);
			for (String correo : correosAviso) {
				EMail email4 = M_Client.createEMail(correo,"Recepción de OC Internacional con preventa de transito " + new Timestamp(System.currentTimeMillis()), cuerpoMensaje.toString(), true);
				EMail.SENT_OK.equals(email4.send());
			}
		}
		
		m_processMsg = info.toString();
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}	//	completeIt

	/* Save array of documents to process AFTER completing this one */
	ArrayList<PO> docsPostProcess = new ArrayList<PO>();

	private void addDocsPostProcess(PO doc) {
		docsPostProcess.add(doc);
	}

	public ArrayList<PO> getDocsPostProcess() {
		return docsPostProcess;
	}

	/**
	 * Automatically creates a customer shipment for any
	 * drop shipment material receipt
	 * Based on createCounterDoc() by JJ
	 * @return shipment if created else null
	 */
	private MInOut createDropShipment() {

		if ( isSOTrx() || !isDropShip() || getC_Order_ID() == 0 )
			return null;

		//	Document Type
		int C_DocTypeTarget_ID = 0;
		MDocType[] shipmentTypes = MDocType.getOfDocBaseType(getCtx(), MDocType.DOCBASETYPE_MaterialDelivery);

		for (int i = 0; i < shipmentTypes.length; i++ )
		{
			if (shipmentTypes[i].isSOTrx() && ( C_DocTypeTarget_ID == 0 || shipmentTypes[i].isDefault() ) )
				C_DocTypeTarget_ID = shipmentTypes[i].getC_DocType_ID();
		}

		//	Deep Copy
		MInOut dropShipment = copyFrom(this, getMovementDate(), getDateAcct(),
			C_DocTypeTarget_ID, !isSOTrx(), false, get_TrxName(), true);

		int linkedOrderID = new MOrder (getCtx(), getC_Order_ID(), get_TrxName()).getLink_Order_ID();
		if (linkedOrderID != 0)
		{
			dropShipment.setC_Order_ID(linkedOrderID);

			// get invoice id from linked order
			int invID = new MOrder (getCtx(), linkedOrderID, get_TrxName()).getC_Invoice_ID();
			if ( invID != 0 )
				dropShipment.setC_Invoice_ID(invID);
		}
		else
			return null;

		dropShipment.setC_BPartner_ID(getDropShip_BPartner_ID());
		dropShipment.setC_BPartner_Location_ID(getDropShip_Location_ID());
		dropShipment.setAD_User_ID(getDropShip_User_ID());
		dropShipment.setIsDropShip(false);
		dropShipment.setDropShip_BPartner_ID(0);
		dropShipment.setDropShip_Location_ID(0);
		dropShipment.setDropShip_User_ID(0);
		dropShipment.setMovementType(MOVEMENTTYPE_CustomerShipment);

		//	References (Should not be required
		dropShipment.setSalesRep_ID(getSalesRep_ID());
		dropShipment.save(get_TrxName());

		//		Update line order references to linked sales order lines
		MInOutLine[] lines = dropShipment.getLines(true);
		for (int i = 0; i < lines.length; i++)
		{
			MInOutLine dropLine = lines[i];
			MOrderLine ol = new MOrderLine(getCtx(), dropLine.getC_OrderLine_ID(), null);
			if ( ol.getC_OrderLine_ID() != 0 ) {
				dropLine.setC_OrderLine_ID(ol.getLink_OrderLine_ID());
				dropLine.save();
			}
		}

		log.fine(dropShipment.toString());

		dropShipment.setDocAction(DocAction.ACTION_Complete);
		dropShipment.processIt(DocAction.ACTION_Complete);
		dropShipment.save();

		return dropShipment;
	}

	/**
	 * 	Set the definite document number after completed
	 */
	private void setDefiniteDocumentNo() {
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		if (dt.isOverwriteDateOnComplete()) {
			setMovementDate(new Timestamp (System.currentTimeMillis()));
		}
		if (dt.isOverwriteSeqOnComplete()) {
			String value = DB.getDocumentNo(getC_DocType_ID(), get_TrxName(), true, this);
			if (value != null)
				setDocumentNo(value);
		}
	}

	/**
	 * 	Check Material Policy
	 * 	Sets line ASI
	 */
	private void checkMaterialPolicy(MInOutLine line)
	{
		
		//faaguilar OFB begin
		BigDecimal qtyFound = DB.getSQLValueBD(get_TrxName(), "Select SUM(MovementQty) from M_InOutLineMA where M_InOutLine_ID=?" , line.getM_InOutLine_ID());
		if(qtyFound !=null && qtyFound.compareTo(line.getMovementQty())==0)
			return;
		
		//faaguilar OFB end
		
		int no = MInOutLineMA.deleteInOutLineMA(line.getM_InOutLine_ID(), get_TrxName());
		if (no > 0)
			log.config("Delete old #" + no);

		//	Incoming Trx
		String MovementType = getMovementType();
		boolean inTrx = MovementType.charAt(1) == '+';	//	V+ Vendor Receipt


		boolean needSave = false;

		MProduct product = line.getProduct();

		//	Need to have Location
		if (product != null
				&& line.getM_Locator_ID() == 0)
		{
			//MWarehouse w = MWarehouse.get(getCtx(), getM_Warehouse_ID());
			line.setM_Warehouse_ID(getM_Warehouse_ID());
			line.setM_Locator_ID(inTrx ? Env.ZERO : line.getMovementQty());	//	default Locator
			needSave = true;
		}

		//	Attribute Set Instance
		//  Create an  Attribute Set Instance to any receipt FIFO/LIFO
		if (product != null && line.getM_AttributeSetInstance_ID() == 0)
		{
			//Validate Transaction
			if (getMovementType().compareTo(MInOut.MOVEMENTTYPE_CustomerReturns) == 0 
					|| getMovementType().compareTo(MInOut.MOVEMENTTYPE_VendorReceipts) == 0 )
			{
				MAttributeSetInstance asi = null;
				//auto balance negative on hand
				MStorage[] storages = MStorage.getWarehouse(getCtx(), getM_Warehouse_ID(), line.getM_Product_ID(), 0,
						null, MClient.MMPOLICY_FiFo.equals(product.getMMPolicy()), false, line.getM_Locator_ID(), get_TrxName());
				for (MStorage storage : storages)
				{
					if (storage.getQtyOnHand().signum() < 0)
					{
						asi = new MAttributeSetInstance(getCtx(), storage.getM_AttributeSetInstance_ID(), get_TrxName());
						break;
					}
				}
				//always create asi so fifo/lifo work.
				if (asi == null)
				{
					asi = MAttributeSetInstance.create(getCtx(), product, get_TrxName());
				}
				line.setM_AttributeSetInstance_ID(asi.getM_AttributeSetInstance_ID());
				log.config("New ASI=" + line);
				needSave = true;
			}
			// Create consume the Attribute Set Instance using policy FIFO/LIFO
			else if(getMovementType().compareTo(MInOut.MOVEMENTTYPE_VendorReturns) == 0 || getMovementType().compareTo(MInOut.MOVEMENTTYPE_CustomerShipment) == 0)
			{
				String MMPolicy = product.getMMPolicy();
				Timestamp minGuaranteeDate = getMovementDate();
				MStorage[] storages = MStorage.getWarehouse(getCtx(), getM_Warehouse_ID(), line.getM_Product_ID(), line.getM_AttributeSetInstance_ID(),
						minGuaranteeDate, MClient.MMPOLICY_FiFo.equals(MMPolicy), true, line.getM_Locator_ID(), get_TrxName());
				BigDecimal qtyToDeliver = line.getMovementQty();
				for (MStorage storage: storages)
				{
					if (storage.getQtyOnHand().compareTo(qtyToDeliver) >= 0)
					{
						MInOutLineMA ma = new MInOutLineMA (line,
								storage.getM_AttributeSetInstance_ID(),
								qtyToDeliver);
						ma.saveEx();
						qtyToDeliver = Env.ZERO;
					}
					else
					{
						MInOutLineMA ma = new MInOutLineMA (line,
								storage.getM_AttributeSetInstance_ID(),
								storage.getQtyOnHand());
						ma.saveEx();
						qtyToDeliver = qtyToDeliver.subtract(storage.getQtyOnHand());
						log.fine( ma + ", QtyToDeliver=" + qtyToDeliver);
					}

					if (qtyToDeliver.signum() == 0)
						break;
				}

				if (qtyToDeliver.signum() != 0)
				{
					//deliver using new asi
					MAttributeSetInstance asi = MAttributeSetInstance.create(getCtx(), product, get_TrxName());
					int M_AttributeSetInstance_ID = asi.getM_AttributeSetInstance_ID();
					MInOutLineMA ma = new MInOutLineMA (line, M_AttributeSetInstance_ID, qtyToDeliver);
					ma.saveEx();
					log.fine("##: " + ma);
				}
			}	//	outgoing Trx
		}	//	attributeSetInstance

		if (needSave)
		{
			line.saveEx();
		}
	}	//	checkMaterialPolicy


	/**************************************************************************
	 * 	Create Counter Document
	 * 	@return InOut
	 */
	private MInOut createCounterDoc()
	{
		//	Is this a counter doc ?
		if (getRef_InOut_ID() != 0)
			return null;

		//	Org Must be linked to BPartner
		MOrg org = MOrg.get(getCtx(), getAD_Org_ID());
		int counterC_BPartner_ID = org.getLinkedC_BPartner_ID(get_TrxName());
		if (counterC_BPartner_ID == 0)
			return null;
		//	Business Partner needs to be linked to Org
		MBPartner bp = new MBPartner (getCtx(), getC_BPartner_ID(), get_TrxName());
		int counterAD_Org_ID = bp.getAD_OrgBP_ID_Int();
		if (counterAD_Org_ID == 0)
			return null;

		MBPartner counterBP = new MBPartner (getCtx(), counterC_BPartner_ID, null);
		MOrgInfo counterOrgInfo = MOrgInfo.get(getCtx(), counterAD_Org_ID, get_TrxName());
		log.info("Counter BP=" + counterBP.getName());

		//	Document Type
		int C_DocTypeTarget_ID = 0;
		MDocTypeCounter counterDT = MDocTypeCounter.getCounterDocType(getCtx(), getC_DocType_ID());
		if (counterDT != null)
		{
			log.fine(counterDT.toString());
			if (!counterDT.isCreateCounter() || !counterDT.isValid())
				return null;
			C_DocTypeTarget_ID = counterDT.getCounter_C_DocType_ID();
		}
		else	//	indirect
		{
			C_DocTypeTarget_ID = MDocTypeCounter.getCounterDocType_ID(getCtx(), getC_DocType_ID());
			log.fine("Indirect C_DocTypeTarget_ID=" + C_DocTypeTarget_ID);
			if (C_DocTypeTarget_ID <= 0)
				return null;
		}

		//	Deep Copy
		MInOut counter = copyFrom(this, getMovementDate(), getDateAcct(),
			C_DocTypeTarget_ID, !isSOTrx(), true, get_TrxName(), true);

		//
		counter.setAD_Org_ID(counterAD_Org_ID);
		counter.setM_Warehouse_ID(counterOrgInfo.getM_Warehouse_ID());
		//
		counter.setBPartner(counterBP);

		if ( isDropShip() )
		{
			counter.setIsDropShip(true );
			counter.setDropShip_BPartner_ID(getDropShip_BPartner_ID());
			counter.setDropShip_Location_ID(getDropShip_Location_ID());
			counter.setDropShip_User_ID(getDropShip_User_ID());
		}

		//	Refernces (Should not be required
		counter.setSalesRep_ID(getSalesRep_ID());
		counter.save(get_TrxName());

		String MovementType = counter.getMovementType();
		boolean inTrx = MovementType.charAt(1) == '+';	//	V+ Vendor Receipt

		//	Update copied lines
		MInOutLine[] counterLines = counter.getLines(true);
		for (int i = 0; i < counterLines.length; i++)
		{
			MInOutLine counterLine = counterLines[i];
			counterLine.setClientOrg(counter);
			counterLine.setM_Warehouse_ID(counter.getM_Warehouse_ID());
			counterLine.setM_Locator_ID(0);
			counterLine.setM_Locator_ID(inTrx ? Env.ZERO : counterLine.getMovementQty());
			//
			counterLine.save(get_TrxName());
		}

		log.fine(counter.toString());

		//	Document Action
		if (counterDT != null)
		{
			if (counterDT.getDocAction() != null)
			{
				counter.setDocAction(counterDT.getDocAction());
				counter.processIt(counterDT.getDocAction());
				counter.save(get_TrxName());
			}
		}
		return counter;
	}	//	createCounterDoc

	/**
	 * 	Void Document.
	 * 	@return true if success
	 */
	public boolean voidIt()
	{
		log.info(toString());
		// Before Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_VOID);
		if (m_processMsg != null)
			return false;

		if (DOCSTATUS_Closed.equals(getDocStatus())
			|| DOCSTATUS_Reversed.equals(getDocStatus())
			|| DOCSTATUS_Voided.equals(getDocStatus()))
		{
			m_processMsg = "Document Closed: " + getDocStatus();
			return false;
		}

		//	Not Processed
		if (DOCSTATUS_Drafted.equals(getDocStatus())
			|| DOCSTATUS_Invalid.equals(getDocStatus())
			|| DOCSTATUS_InProgress.equals(getDocStatus())
			|| DOCSTATUS_Approved.equals(getDocStatus())
			|| DOCSTATUS_NotApproved.equals(getDocStatus()) )
		{
			//	Set lines to 0
			MInOutLine[] lines = getLines(false);
			for (int i = 0; i < lines.length; i++)
			{
				MInOutLine line = lines[i];
				BigDecimal old = line.getMovementQty();
				if (old.signum() != 0)
				{
					line.setQty(Env.ZERO);
					line.addDescription("Void (" + old + ")");
					line.save(get_TrxName());
				}
			}
			//
			// Void Confirmations
			setDocStatus(DOCSTATUS_Voided); // need to set & save docstatus to be able to check it in MInOutConfirm.voidIt()
			saveEx();
			voidConfirmations();
		}
		else
		{	//faaguilar OFB validacion antes de anular begin
			String docno=findInvoice();
			if(docno!=null){
				m_processMsg = "Este Documento posee una Factura Valida Relacionada, no puede ser Anulado. :"+docno;
				return false;
			}
			//faaguilar OFB validacion antes de anular end
			return voidOFB();//faaguilar OFB -forma personalizada de Anular
		}

		// After Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_VOID);
		if (m_processMsg != null)
			return false;

		setProcessed(true);
		setDocAction(DOCACTION_None);
		return true;
	}	//	voidIt

	/**
	 * 	Close Document.
	 * 	@return true if success
	 */
	public boolean closeIt()
	{
		log.info(toString());
		// Before Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
		if (m_processMsg != null)
			return false;

		setProcessed(true);
		setDocAction(DOCACTION_None);

		// After Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
		if (m_processMsg != null)
			return false;
		return true;
	}	//	closeIt

	/**
	 * 	Reverse Correction - same date
	 * 	@return true if success
	 */
	public boolean reverseCorrectIt()
	{
		log.info(toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		if (!MPeriod.isOpen(getCtx(), getDateAcct(), dt.getDocBaseType(), getAD_Org_ID()))
		{
			m_processMsg = "@PeriodClosed@";
			return false;
		}

		//faaguilar OFB validacion antes de anular begin
		String docno=findInvoice();
		if(docno!=null){
			m_processMsg = "Este Documento posee una Factura Valida Relacionada, no puede ser Anulado. :"+docno;
			return false;
		}
		//faaguilar OFB validacion antes de anular end
		return voidOFB();//faaguilar OFB -forma personalizada de Anular
		
	}	//	reverseCorrectionIt

	public boolean reverseCorrectItOFB()
	{
		log.info(toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		if (!MPeriod.isOpen(getCtx(), getDateAcct(), dt.getDocBaseType(), getAD_Org_ID()))
		{
			m_processMsg = "@PeriodClosed@";
			return false;
		}

		//	Reverse/Delete Matching
		if (!isSOTrx())
		{
			MMatchInv[] mInv = MMatchInv.getInOut(getCtx(), getM_InOut_ID(), get_TrxName());
			for (int i = 0; i < mInv.length; i++)
				mInv[i].deleteEx(true);
			MMatchPO[] mPO = MMatchPO.getInOut(getCtx(), getM_InOut_ID(), get_TrxName());
			for (int i = 0; i < mPO.length; i++)
			{
				if (mPO[i].getC_InvoiceLine_ID() == 0)
					mPO[i].deleteEx(true);
				else
				{
					mPO[i].setM_InOutLine_ID(0);
					mPO[i].saveEx();

				}
			}
		}

		//	Deep Copy
		MInOut reversal = copyFrom (this, getMovementDate(), getDateAcct(),
			getC_DocType_ID(), isSOTrx(), false, get_TrxName(), true);
		if (reversal == null)
		{
			m_processMsg = "Could not create Ship Reversal";
			return false;
		}
		reversal.setReversal(true);

		//	Reverse Line Qty
		MInOutLine[] sLines = getLines(false);
		MInOutLine[] rLines = reversal.getLines(false);
		for (int i = 0; i < rLines.length; i++)
		{
			MInOutLine rLine = rLines[i];
			/**
			 * faaguilar OFB
			 * relacion inoutline con orderline para restarle lo factura al reversado*/
			//faaguilar OFB reverso begin
			MOrderLine oline=null;
			try {
				 oline= (MOrderLine) sLines[i].getC_OrderLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(oline!=null){
				rLine.setQtyEntered(oline.getQtyOrdered().subtract(oline.getQtyInvoiced()).negate());
				rLine.setMovementQty(oline.getQtyOrdered().subtract(oline.getQtyInvoiced()).negate());
			}else {
				rLine.setQtyEntered(rLine.getQtyEntered().negate());
				rLine.setMovementQty(rLine.getMovementQty().negate());
			}
			rLine.addDescription("Reversado No Anulado");
			
			//faaguilar OFB reverso end
			
			rLine.setM_AttributeSetInstance_ID(sLines[i].getM_AttributeSetInstance_ID());
			// Goodwill: store original (voided/reversed) document line
			rLine.setReversalLine_ID(sLines[i].getM_InOutLine_ID());
			if (!rLine.save(get_TrxName()))
			{
				m_processMsg = "Could not correct Ship Reversal Line";
				return false;
			}
			//	We need to copy MA
			if (rLine.getM_AttributeSetInstance_ID() == 0)
			{
				MInOutLineMA mas[] = MInOutLineMA.get(getCtx(),
					sLines[i].getM_InOutLine_ID(), get_TrxName());
				for (int j = 0; j < mas.length; j++)
				{
					MInOutLineMA ma = new MInOutLineMA (rLine,
						mas[j].getM_AttributeSetInstance_ID(),
						mas[j].getMovementQty().negate());
					ma.saveEx();
				}
			}
			//	De-Activate Asset
			MAsset asset = MAsset.getFromShipment(getCtx(), sLines[i].getM_InOutLine_ID(), get_TrxName());
			if (asset != null)
			{
				asset.setIsActive(false);
				asset.addDescription("(" + reversal.getDocumentNo() + " #" + rLine.getLine() + "<-)");
				asset.save();
			}
		}
		reversal.setC_Order_ID(getC_Order_ID());
		// Set M_RMA_ID
		reversal.setM_RMA_ID(getM_RMA_ID());
		reversal.addDescription("{->" + getDocumentNo() + ")");
		reversal.addDescription("Reversado No Anulado");
		//FR1948157
		reversal.setReversal_ID(getM_InOut_ID());
		reversal.saveEx(get_TrxName());
		//
		if (!reversal.processIt(DocAction.ACTION_Complete)
			|| !reversal.getDocStatus().equals(DocAction.STATUS_Completed))
		{
			m_processMsg = "Reversal ERROR: " + reversal.getProcessMsg();
			return false;
		}
		reversal.closeIt();
		reversal.setProcessing (false);
		reversal.setDocStatus(DOCSTATUS_Reversed);
		reversal.setDocAction(DOCACTION_None);
		reversal.saveEx(get_TrxName());
		//
		addDescription("(" + reversal.getDocumentNo() + "<-)");
		addDescription("Reversado No Anulado");
		
		//
		// Void Confirmations
		setDocStatus(DOCSTATUS_Reversed); // need to set & save docstatus to be able to check it in MInOutConfirm.voidIt()
		saveEx();
		voidConfirmations();

		// After reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		m_processMsg = reversal.getDocumentNo();
		//FR1948157
		this.setReversal_ID(reversal.getM_InOut_ID());
		setProcessed(true);
		setDocStatus(DOCSTATUS_Reversed);		//	 may come from void
		setDocAction(DOCACTION_None);
		return true;
	}	//	reverseCorrectionIt
	/**
	 * 	Reverse Accrual - none
	 * 	@return false
	 */
	public boolean reverseAccrualIt()
	{
		log.info(toString());
		// Before reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;

		// After reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;

		return false;
	}	//	reverseAccrualIt

	/**
	 * 	Re-activate
	 * 	@return false
	 */
	public boolean reActivateIt()
	{
		log.info(toString());
		// Before reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REACTIVATE);
		if (m_processMsg != null)
			return false;

		// After reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REACTIVATE);
		if (m_processMsg != null)
			return false;

		return false;
	}	//	reActivateIt


	/*************************************************************************
	 * 	Get Summary
	 *	@return Summary of Document
	 */
	public String getSummary()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getDocumentNo());
		//	: Total Lines = 123.00 (#1)
		sb.append(":")
		//	.append(Msg.translate(getCtx(),"TotalLines")).append("=").append(getTotalLines())
			.append(" (#").append(getLines(false).length).append(")");
		//	 - Description
		if (getDescription() != null && getDescription().length() > 0)
			sb.append(" - ").append(getDescription());
		return sb.toString();
	}	//	getSummary

	/**
	 * 	Get Process Message
	 *	@return clear text error message
	 */
	public String getProcessMsg()
	{
		return m_processMsg;
	}	//	getProcessMsg

	/**
	 * 	Get Document Owner (Responsible)
	 *	@return AD_User_ID
	 */
	public int getDoc_User_ID()
	{
		return getSalesRep_ID();
	}	//	getDoc_User_ID

	/**
	 * 	Get Document Approval Amount
	 *	@return amount
	 */
	public BigDecimal getApprovalAmt()
	{
		return Env.ZERO;
	}	//	getApprovalAmt

	/**
	 * 	Get C_Currency_ID
	 *	@return Accounting Currency
	 */
	public int getC_Currency_ID ()
	{
		return Env.getContextAsInt(getCtx(),"$C_Currency_ID");
	}	//	getC_Currency_ID

	/**
	 * 	Document Status is Complete or Closed
	 *	@return true if CO, CL or RE
	 */
	public boolean isComplete()
	{
		String ds = getDocStatus();
		return DOCSTATUS_Completed.equals(ds)
			|| DOCSTATUS_Closed.equals(ds)
			|| DOCSTATUS_Reversed.equals(ds);
	}	//	isComplete

	/**
	 * faaguilar OFB
	 * crea una orden que representa una carpeta de importacion
	 * COMPRAS
	 * */
	public boolean createOrder()
	{
		Boolean isforeign ="Y".equals(DB.getSQLValueString(get_TrxName(), "select isforeign from c_doctype where c_doctype_id="+getC_DocType_ID()));
		int orderpo=DB.getSQLValue(get_TrxName(),"select c_doctype_id from c_doctype where isforeign='Y' and docbasetype='POO' and ad_client_id="+getAD_Client_ID());
		
		if(orderpo<=0 && isforeign)
			return false;
		
		if(isforeign)
		{
			MOrder order=new MOrder(getCtx(),0,get_TrxName());
			order.setBPartner(getBPartner());
			order.setDescription("Creada desde la recepcion :"+getDocumentNo());
			order.setIsSOTrx(false);
			order.setC_DocType_ID(orderpo);
			order.setC_DocTypeTarget_ID(orderpo);
			order.save();
			
			MOrderLine line = new MOrderLine(order);
			line.setIsDescription(true);
			line.setDescription("Esta Orden representa una carpeta de Importaciï¿½n");
			line.setC_UOM_ID(100);
			line.save();
			
			order.setDocAction("--");
			order.setDocStatus("CO");
			order.setProcessed(true);
			order.save();
			
			setC_Order_ID(order.getC_Order_ID());
			
		}
		return true;
	}
	
	/**
	 * faaguilar OFB
	 * busca facturas relacionadas con las lineas del InOut
	 * retorna la 1ï¿½ encontrada
	 * se usa para validar antes de anular el InOut*/
	public String findInvoice(){
		
		String mysql="select count(1) from C_InvoiceLine ivl "+
		"inner join C_Invoice iv on (ivl.C_Invoice_ID=iv.C_Invoice_ID) "+
		"where ivl.M_InOutLine_ID IN (select M_InOutLine_ID from M_InOutLine where M_InOut_ID="+getM_InOut_ID()+") "+
		"and iv.DocStatus in ('CO', 'CL')";
		
		int count=DB.getSQLValue(get_TrxName(),mysql);
		if(count>0)			
		{
			mysql="select f.documentno from c_invoice f " +
			" inner join c_invoiceline fl on (f.c_invoice_id=fl.c_invoice_id)" +
			" where fl.M_InOutLine_ID IN (select M_InOutLine_ID from M_InOutLine where M_InOut_ID="+getM_InOut_ID()+") "+
			"and f.DocStatus IN ('CO','CL')";
			
			String DocNo=DB.getSQLValueString(get_TrxName(),mysql);
			return DocNo;
		}
		
	   return null;
	}
	
	/**
	 * faaguilar OFB
	 * 	anula el documento completo considerando todas sus lineas y cantidades
	 * 	@return true if success
	 */
	public boolean voidOFB()
	{
		log.info(toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		if (!MPeriod.isOpen(getCtx(), getDateAcct(), dt.getDocBaseType(), getAD_Org_ID()))
		{
			m_processMsg = "@PeriodClosed@";
			return false;
		}

		//	Reverse/Delete Matching
		if (!isSOTrx())
		{
			MMatchInv[] mInv = MMatchInv.getInOut(getCtx(), getM_InOut_ID(), get_TrxName());
			for (int i = 0; i < mInv.length; i++)
				mInv[i].deleteEx(true);
			MMatchPO[] mPO = MMatchPO.getInOut(getCtx(), getM_InOut_ID(), get_TrxName());
			for (int i = 0; i < mPO.length; i++)
			{
				if (mPO[i].getC_InvoiceLine_ID() == 0)
					mPO[i].deleteEx(true);
				else
				{
					mPO[i].setM_InOutLine_ID(0);
					mPO[i].saveEx();

				}
			}
		}

		//	Deep Copy
		MInOut reversal = copyToReversal (this, getMovementDate(), getDateAcct(),
			getC_DocType_ID(), isSOTrx(), false, get_TrxName(), true);
		if (reversal == null)
		{
			m_processMsg = "Could not create Ship Reversal";
			return false;
		}
		reversal.setReversal(true);
		//	Reverse Line Qty
		MInOutLine[] sLines = getLines(false);
		MInOutLine[] rLines = reversal.getLines(false);
		for (int i = 0; i < rLines.length; i++)
		{
			MInOutLine rLine = rLines[i];						
			rLine.setQtyEntered(rLine.getQtyEntered().negate());
			rLine.setMovementQty(rLine.getMovementQty().negate());
			rLine.setM_AttributeSetInstance_ID(sLines[i].getM_AttributeSetInstance_ID());
			// Goodwill: store original (voided/reversed) document line
			rLine.setReversalLine_ID(sLines[i].getM_InOutLine_ID());
			/**
			 * faaguilar OFB
			 * si existe un locator en las bodegas definido como patio
			 * entonces todos los movimientos que se anulen de venta
			 * */
			//faaguilar OFB locator patio begin
			if(MLocator.getPatio(reversal.getM_Warehouse_ID())>0 && getMovementType().equals(MOVEMENTTYPE_CustomerShipment))
				rLine.setM_Locator_ID(MLocator.getPatio(reversal.getM_Warehouse_ID()));
			//faaguilar OFB locator patio end
			
			if (!rLine.save(get_TrxName()))
			{
				m_processMsg = "Could not correct Ship Reversal Line";
				return false;
			}
			//	We need to copy MA
			if (rLine.getM_AttributeSetInstance_ID() == 0)
			{
				MInOutLineMA mas[] = MInOutLineMA.get(getCtx(),
					sLines[i].getM_InOutLine_ID(), get_TrxName());
				for (int j = 0; j < mas.length; j++)
				{
					MInOutLineMA ma = new MInOutLineMA (rLine,
						mas[j].getM_AttributeSetInstance_ID(),
						mas[j].getMovementQty().negate());
					ma.saveEx();
				}
			}
			//	De-Activate Asset
			MAsset asset = MAsset.getFromShipment(getCtx(), sLines[i].getM_InOutLine_ID(), get_TrxName());
			if (asset != null)
			{
				asset.setIsActive(false);
				asset.addDescription("(" + reversal.getDocumentNo() + " #" + rLine.getLine() + "<-)");
				asset.save();
			}
		}
		reversal.setC_Order_ID(getC_Order_ID());
		// Set M_RMA_ID
		reversal.setM_RMA_ID(getM_RMA_ID());
		reversal.addDescription("{->" + getDocumentNo() + ")");
		//FR1948157
		reversal.setReversal_ID(getM_InOut_ID());
		reversal.saveEx(get_TrxName());
		//
		if (!reversal.processIt(DocAction.ACTION_Complete)
			|| !reversal.getDocStatus().equals(DocAction.STATUS_Completed))
		{
			m_processMsg = "Reversal ERROR: " + reversal.getProcessMsg();
			return false;
		}
		reversal.closeIt();
		reversal.setProcessing (false);
		reversal.setDocStatus(DOCSTATUS_Reversed);
		reversal.setDocAction(DOCACTION_None);
		reversal.saveEx(get_TrxName());
		//
		addDescription("(" + reversal.getDocumentNo() + "<-)");
		
		//
		// Void Confirmations
		setDocStatus(DOCSTATUS_Reversed); // need to set & save docstatus to be able to check it in MInOutConfirm.voidIt()
		saveEx();
		voidConfirmations();

		// After reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		m_processMsg = reversal.getDocumentNo();
		//FR1948157
		this.setReversal_ID(reversal.getM_InOut_ID());
		setProcessed(true);
		setDocStatus(DOCSTATUS_Reversed);		//	 may come from void
		setDocAction(DOCACTION_None);
		return true;
	}	//	reverseCorrectionIt
	
	/**
	 * faaguilar OFB
	 * indica el docbasetype del documento actual*/
	public String getDocBase()
	{
		
		String base;
		
		base=DB.getSQLValueString(get_TrxName(),"select docbasetype from c_doctype where c_doctype_id=?",getC_DocType_ID());
		
		return base;
	}
	
	/**
	 * faaguilar OFB
	 * verifica en base al rol los rangos de tolerancia de precio y cantidad para ordenes de compra*/
	public String rangos(){
		
		if(isSOTrx() || this.getMovementType().equals(this.MOVEMENTTYPE_CustomerShipment))
			return null;
		String processMsg="";
		
		MRole role= new MRole( getCtx(), Env.getAD_Role_ID(getCtx()),get_TrxName());
		BigDecimal rtq=(BigDecimal)role.get_Value("P_RTQ");
		BigDecimal rtp=(BigDecimal)role.get_Value("P_RTP");
		
		if(rtq.signum()==0 && rtp.signum()==0)
			return null;
		
		rtq=rtq.divide(Env.ONEHUNDRED, 2,BigDecimal.ROUND_HALF_EVEN);
		rtp=rtp.divide(Env.ONEHUNDRED, 2,BigDecimal.ROUND_HALF_EVEN);
		
		MInOutLine[] lines = getLines(true);
		for (int i = 0; i < lines.length; i++)
		{
			MInOutLine line = lines[i];
			if(line.getC_OrderLine_ID()>0){
				
				MOrderLine oline= new MOrderLine (getCtx(), line.getC_OrderLine_ID() , get_TrxName());
				
				//validacion de cantidad
				BigDecimal maxQty=oline.getQtyEntered().add(oline.getQtyEntered().multiply(rtq));
				BigDecimal minQty=oline.getQtyEntered().subtract(oline.getQtyEntered().multiply(rtq));
				if(line.getQtyEntered().floatValue()> maxQty.floatValue() )
					processMsg="limite de cantidad - linea " + line.getLine();
				
				//validacion de precio 
				BigDecimal maxP=oline.getPriceActual().add(oline.getPriceActual().multiply(rtp));
				BigDecimal minP=oline.getPriceActual().subtract(oline.getPriceActual().multiply(rtp));
				BigDecimal priceActual=(BigDecimal) line.get_Value("PriceActual");
				
				if(priceActual.floatValue()>maxP.floatValue() || priceActual.floatValue()<minP.floatValue())
					processMsg=" limite de precio - linea ;" + line.getLine();
				
				if(processMsg.length()>10)
					break;
			}
			
		}
		
		return processMsg;
		
	}
	
	/** faaguilar OFB
	 * getOrderLineLocator
	 * trae el mismo locator en el cual se reservo al hacer la orden*/
	public int getOrderLocator(MOrderLine line){
		int M_Locator_ID=0;
		
		try{
			if(line.get_Value("M_Locator_ID")!=null && ((Integer)line.get_Value("M_Locator_ID")).intValue()>0 )
			{
				M_Locator_ID=(Integer)line.get_Value("M_Locator_ID");
				return M_Locator_ID;
			}
			
		}
		catch(Exception e){
			log.severe("OrderLine - no field M_Locator_ID");
		}
		
		MProduct product = line.getProduct();
		if (product != null) 
		{
			if (product.isStocked())
			{
				boolean isSOTrx = isSOTrx();
				BigDecimal target = line.getQtyOrdered();
				BigDecimal difference = target
				.subtract(line.getQtyReserved())
				.subtract(line.getQtyDelivered()); 
				BigDecimal ordered = isSOTrx ? Env.ZERO : difference;
				
				if (line.getM_AttributeSetInstance_ID() != 0)	//	Get existing Location
					M_Locator_ID = MStorage.getM_Locator_ID (line.getM_Warehouse_ID(), 
						line.getM_Product_ID(), line.getM_AttributeSetInstance_ID(), 
						ordered, get_TrxName());
				//	Get default Location
				if (M_Locator_ID == 0)
				{
					// try to take default locator for product first
					// if it is from the selected warehouse
					MWarehouse wh = MWarehouse.get(getCtx(), line.getM_Warehouse_ID());
					M_Locator_ID = product.getM_Locator_ID();
					if (M_Locator_ID!=0) {
						MLocator locator = new MLocator(getCtx(), product.getM_Locator_ID(), get_TrxName());
						//product has default locator defined but is not from the order warehouse
						if(locator.getM_Warehouse_ID()!=wh.get_ID()) {
							M_Locator_ID = wh.getDefaultLocator().getM_Locator_ID();
						}
					} else {
						M_Locator_ID = wh.getDefaultLocator().getM_Locator_ID();
					}
				}
			}
		}
		return M_Locator_ID;
	}
	
	/**
	 * faaguilar OFB
	 * create asset for charge TCAF*/
	public void createAsset(){
		
		
		MInOutLine[] lines = getLines(false);
		int group_ID = 0;
		for (int lineIndex = 0; lineIndex < lines.length; lineIndex++)
		{
			MInOutLine sLine = lines[lineIndex];
			if(isAsset(sLine))
				if(sLine.get_ValueAsString("A_CapvsExp").equals(X_C_InvoiceLine.A_CAPVSEXP_Capital)  )//crear activo
				{				
					group_ID = sLine.get_ValueAsInt("A_Asset_Group_ID");
					
					if(group_ID <= 0){
						group_ID = DB.getSQLValue(get_TrxName(), "select a_asset_group_id from a_asset_group where isdefault='Y'");
					    if(group_ID <= 0)
					    	throw new IllegalStateException("no default asset group");
					}
					//ininoles generar varios activos.
					BigDecimal qtyAsset = sLine.getQtyEntered();
					/*int unitAsset = 1;					
					try
					{
						if(sLine.get_ValueAsBoolean("AssetUnit"))
						{
							unitAsset = qtyAsset.intValue();
							qtyAsset = Env.ONE;
						}
					}catch (Exception e)
					{
						log.log(Level.SEVERE, e.getMessage(), e);
					}*/

					/*for (int a = 1; a <= unitAsset; a++)
					 {*/
						MAsset asset = new MAsset(getCtx(),  0 ,get_TrxName());
						//asset.setName(sLine.getDescription()+"-"+a);
						asset.setName(sLine.getDescription());
						asset.setAD_Org_ID(sLine.getAD_Org_ID());
						asset.setHelp(sLine.get_ValueAsString("Help"));
						asset.setAssetServiceDate(getMovementDate());
						asset.setQty(qtyAsset); //se ocupa variable manejable
						asset.setA_Asset_Group_ID(group_ID);
						if(sLine.get_ValueAsInt("AD_user_ID")>0)
							asset.setAD_User_ID(sLine.get_ValueAsInt("AD_user_ID"));
						asset.saveEx();
						//ininoles nuevo campo de grupo de activo padre
						try
						{
							if(sLine.get_ValueAsInt("A_Asset_Group_Ref_ID")>0)
							{
								asset.set_CustomColumn("A_Asset_Group_Ref_ID", sLine.get_ValueAsInt("A_Asset_Group_Ref_ID"));
								asset.save();
							}
						}catch (Exception e)
						{
							log.log(Level.SEVERE, e.getMessage(), e);
						}
						
						sLine.set_CustomColumn("A_Asset_ID", asset.getA_Asset_ID());
						sLine.save();
						
						if(sLine.getC_OrderLine_ID()>0)
						{
							MOrderLine oLine = new MOrderLine (getCtx() ,sLine.getC_OrderLine_ID(),get_TrxName());
							oLine.set_CustomColumn("A_Asset_ID", asset.getA_Asset_ID());
							oLine.save();
						}
						
						
						if(sLine.get_ValueAsInt("AD_user_ID")>0)
						{
							X_A_Asset_User assetuser = new X_A_Asset_User(getCtx(),  0 ,get_TrxName());
							assetuser.setA_Asset_ID(asset.getA_Asset_ID());
							assetuser.setAD_User_ID(sLine.get_ValueAsInt("AD_user_ID"));
							assetuser.setDateFrom(getMovementDate());
							assetuser.save();
							
							X_A_Asset_Use  assetuse = new X_A_Asset_Use(getCtx(),  0 ,get_TrxName());
							assetuse.setA_Asset_ID(asset.getA_Asset_ID());
							assetuse.setUseDate(getMovementDate());
							assetuse.setUseUnits(1);
							assetuse.set_CustomColumn("AD_user_ID", sLine.get_ValueAsInt("AD_user_ID"));
							assetuse.save();
							//ininoles nuevo campo para dpp dentro de try para que no se caiga si no existe
							try
							{
								if(sLine.get_ValueAsInt("S_Resource_ID")>0)
								{
									assetuse.set_CustomColumn("S_Resource_ID", sLine.get_ValueAsInt("S_Resource_ID"));
									assetuse.set_CustomColumn("Org3_ID", sLine.get_ValueAsInt("Org2_ID"));
								}
							}catch (Exception e)
							{
								log.log(Level.SEVERE, e.getMessage(), e);
							}
							assetuse.save();
						}else
						{
							X_A_Asset_Use  assetuse = new X_A_Asset_Use(getCtx(),  0 ,get_TrxName());
							assetuse.setA_Asset_ID(asset.getA_Asset_ID());
							assetuse.setUseDate(getMovementDate());
							assetuse.setUseUnits(1);
							//ininoles nuevo campo para dpp dentro de try para que no se caiga si no existe
							try
							{
								if(sLine.get_ValueAsInt("S_Resource_ID")>0)
								{
									assetuse.set_CustomColumn("S_Resource_ID", sLine.get_ValueAsInt("S_Resource_ID"));
									assetuse.set_CustomColumn("Org3_ID", sLine.get_ValueAsInt("Org2_ID"));
									//ininoles se agrega seteo de nuevo campo 
									int User2 = DB.getSQLValue(get_TrxName(), "SELECT AD_User_ID FROM S_Resource WHERE S_Resource_ID = "+sLine.get_ValueAsInt("S_Resource_ID"));
									assetuse.set_CustomColumn("AD_User2_ID",User2);									
								}
							}catch (Exception e)
							{
								log.log(Level.SEVERE, e.getMessage(), e);
							}
							assetuse.save();
						}
					//}
				}
		}
	}//endcreateAsset()
	
	/**
	 *  faaguilar OFB
	 * 	Create new Shipment by copying
	 * 	@param from shipment
	 * 	@param dateDoc date of the document date
	 * 	@param C_DocType_ID doc type
	 * 	@param isSOTrx sales order
	 * 	@param counter create counter links
	 * 	@param trxName trx
	 * 	@param setOrder set the order link
	 *	@return Shipment
	 */
	public static MInOut copyToReversal (MInOut from, Timestamp dateDoc, Timestamp dateAcct,
		int C_DocType_ID, boolean isSOTrx, boolean counter, String trxName, boolean setOrder)
	{
		MInOut to = new MInOut (from.getCtx(), 0, null);
		to.set_TrxName(trxName);
		copyValues(from, to, from.getAD_Client_ID(), from.getAD_Org_ID());
		to.set_ValueNoCheck ("M_InOut_ID", I_ZERO);
		to.set_ValueNoCheck ("DocumentNo", null);
		//
		to.setDocStatus (DOCSTATUS_Drafted);		//	Draft
		to.setDocAction(DOCACTION_Complete);
		//
		to.setC_DocType_ID (C_DocType_ID);
		to.setIsSOTrx(isSOTrx);
		if (counter)
		{
			MDocType docType = MDocType.get(from.getCtx(), C_DocType_ID);
			if (MDocType.DOCBASETYPE_MaterialDelivery.equals(docType.getDocBaseType()))
			{
				to.setMovementType (isSOTrx ? MOVEMENTTYPE_CustomerShipment : MOVEMENTTYPE_VendorReturns);
			}
			else if (MDocType.DOCBASETYPE_MaterialReceipt.equals(docType.getDocBaseType()))
			{
				to.setMovementType (isSOTrx ? MOVEMENTTYPE_CustomerReturns : MOVEMENTTYPE_VendorReceipts);
			}
		}

		//
		to.setDateOrdered (dateDoc);
		to.setDateAcct (dateAcct);
		to.setMovementDate(dateDoc);
		to.setDatePrinted(null);
		to.setIsPrinted (false);
		to.setDateReceived(null);
		to.setNoPackages(0);
		to.setShipDate(null);
		to.setPickDate(null);
		to.setIsInTransit(false);
		//
		to.setIsApproved (false);
		to.setC_Invoice_ID(0);
		to.setTrackingNo(null);
		to.setIsInDispute(false);
		//
		to.setPosted (false);
		to.setProcessed (false);
		//[ 1633721 ] Reverse Documents- Processing=Y
		to.setProcessing(false);
		to.setC_Order_ID(0);	//	Overwritten by setOrder
		to.setM_RMA_ID(0);      //  Overwritten by setOrder
		
		to.setReversal_ID(from.getM_InOut_ID());//faaguilar OFB important
		
		if (counter)
		{
			to.setC_Order_ID(0);
			to.setRef_InOut_ID(from.getM_InOut_ID());
			//	Try to find Order/Invoice link
			if (from.getC_Order_ID() != 0)
			{
				MOrder peer = new MOrder (from.getCtx(), from.getC_Order_ID(), from.get_TrxName());
				if (peer.getRef_Order_ID() != 0)
					to.setC_Order_ID(peer.getRef_Order_ID());
			}
			if (from.getC_Invoice_ID() != 0)
			{
				MInvoice peer = new MInvoice (from.getCtx(), from.getC_Invoice_ID(), from.get_TrxName());
				if (peer.getRef_Invoice_ID() != 0)
					to.setC_Invoice_ID(peer.getRef_Invoice_ID());
			}
			//find RMA link
			if (from.getM_RMA_ID() != 0)
			{
				MRMA peer = new MRMA (from.getCtx(), from.getM_RMA_ID(), from.get_TrxName());
				if (peer.getRef_RMA_ID() > 0)
					to.setM_RMA_ID(peer.getRef_RMA_ID());
			}
		}
		else
		{
			to.setRef_InOut_ID(0);
			if (setOrder)
			{
				to.setC_Order_ID(from.getC_Order_ID());
				to.setM_RMA_ID(from.getM_RMA_ID()); // Copy also RMA
			}
		}
		//
		if (!to.save(trxName))
			throw new IllegalStateException("Could not create Shipment");
		if (counter)
			from.setRef_InOut_ID(to.getM_InOut_ID());

		if (to.copyLinesToReversal(from, counter, setOrder) == 0)
			throw new IllegalStateException("Could not create Shipment Lines");

		return to;
	}	//	copytoReversal
	
	/**
	 *  faaguilar OFB
	 * 	Copy Lines From other Shipment
	 *	@param otherShipment shipment
	 *	@param counter set counter info
	 *	@param setOrder set order link
	 *	@return number of lines copied
	 */
	public int copyLinesToReversal (MInOut otherShipment, boolean counter, boolean setOrder)
	{
		if (isProcessed() || isPosted() || otherShipment == null)
			return 0;
		MInOutLine[] fromLines = otherShipment.getLines(false);
		int count = 0;
		for (int i = 0; i < fromLines.length; i++)
		{
			MInOutLine line = new MInOutLine (this);
			MInOutLine fromLine = fromLines[i];
			line.set_TrxName(get_TrxName());
			if (counter)	//	header
				PO.copyValues(fromLine, line, getAD_Client_ID(), getAD_Org_ID());
			else
				PO.copyValues(fromLine, line, fromLine.getAD_Client_ID(), fromLine.getAD_Org_ID());
			line.setM_InOut_ID(getM_InOut_ID());
			line.set_ValueNoCheck ("M_InOutLine_ID", I_ZERO);	//	new
			//	Reset
			if (!setOrder)
			{
				line.setC_OrderLine_ID(0);
				line.setM_RMALine_ID(0);  // Reset RMA Line
			}
			if (!counter)
				line.setM_AttributeSetInstance_ID(0);
			line.setM_AttributeSetInstance_ID(fromLine.getM_AttributeSetInstance_ID() );//faaguilar OFB fix . original 0
		//	line.setS_ResourceAssignment_ID(0);
			line.setRef_InOutLine_ID(0);
			line.setIsInvoiced(false);
			//
			line.setConfirmedQty(Env.ZERO);
			line.setPickedQty(Env.ZERO);
			line.setScrappedQty(Env.ZERO);
			line.setTargetQty(Env.ZERO);
			//	Set Locator based on header Warehouse
			if (getM_Warehouse_ID() != otherShipment.getM_Warehouse_ID())
			{
				line.setM_Locator_ID(0);
				line.setM_Locator_ID(Env.ZERO);
			}
			//
			if (counter)
			{
				line.setRef_InOutLine_ID(fromLine.getM_InOutLine_ID());
				if (fromLine.getC_OrderLine_ID() != 0)
				{
					MOrderLine peer = new MOrderLine (getCtx(), fromLine.getC_OrderLine_ID(), get_TrxName());
					if (peer.getRef_OrderLine_ID() != 0)
						line.setC_OrderLine_ID(peer.getRef_OrderLine_ID());
				}
				//RMALine link
				if (fromLine.getM_RMALine_ID() != 0)
				{
					MRMALine peer = new MRMALine (getCtx(), fromLine.getM_RMALine_ID(), get_TrxName());
					if (peer.getRef_RMALine_ID() > 0)
						line.setM_RMALine_ID(peer.getRef_RMALine_ID());
				}
			}
			//
			
			line.setReversalLine_ID(fromLine.getM_InOutLine_ID());
			
			line.setProcessed(false);
			if (line.save(get_TrxName()))
				count++;
			//	Cross Link
			if (counter)
			{
				fromLine.setRef_InOutLine_ID(line.getM_InOutLine_ID());
				fromLine.save(get_TrxName());
			}
		}
		if (fromLines.length != count)
			log.log(Level.SEVERE, "Line difference - From=" + fromLines.length + " <> Saved=" + count);
		return count;
	}	//	copyLinesFrom
	
	
	/**faaguilar OFB*/
	private int getLocatorTo(int Warehouse_ID){
		
		String sql ="select M_locator_ID from M_Locator where upper(value) = 'ABASTECIMIENTO' and M_Warehouse_ID=" + Warehouse_ID;
		
		int LocatorTO_ID = DB.getSQLValue(get_TrxName(), sql);
		if(LocatorTO_ID<0)
			LocatorTO_ID=0;
		
		return LocatorTO_ID;

	}
	
	/**faaguilar OFB*/
	private String checkStock(){
		
		if(getC_Order_ID()>0)
			if(getC_Order().getDeliveryRule().equals(MOrder.DELIVERYRULE_Force))
				return null;
		
		
		MInOutLine[] lines = getLines(true);
		for(MInOutLine line:lines)
		{
			if(line.getReversalLine_ID()>0)
				continue;
			
			if(line.getM_Product_ID()<=0)
				continue;
			
		    if(!line.getM_Product().isStocked())
		    	continue;
			if(!OFBForward.NoValidateStockShipment())
			{
				if(line.getM_AttributeSetInstance_ID()!=0 && (getMovementType().equals("C-") || getMovementType().equals("V-")) )
				{
						BigDecimal qtyHand = DB.getSQLValueBD(get_TrxName(), "Select SUM(QtyonHand) from M_Storage where M_Product_ID=?" +
									"and M_Locator_ID=? and M_AttributeSetInstance_ID=?",line.getM_Product_ID(),line.getM_Locator_ID(),line.getM_AttributeSetInstance_ID());
							
						if(qtyHand == null || qtyHand.compareTo(line.getMovementQty())<0)
						{
							return "Stock Insuficiente :"+(qtyHand==null?Env.ZERO:qtyHand) + " linea "+line.getLine();
					       
					    }
				}
				else if(line.getM_AttributeSetInstance_ID()==0 && (getMovementType().equals("C-") || getMovementType().equals("V-")) )
				{
					MInOutLineMA mas[] = MInOutLineMA.get(getCtx(),
							line.getM_InOutLine_ID(), get_TrxName());
						for (int j = 0; j < mas.length; j++)
						{
							MInOutLineMA ma = mas[j];
							BigDecimal qtyHand = DB.getSQLValueBD(get_TrxName(), "Select SUM(QtyonHand) from M_Storage where M_Product_ID=?" +
									"and M_Locator_ID=? and M_AttributeSetInstance_ID=?",line.getM_Product_ID(),line.getM_Locator_ID(),ma.getM_AttributeSetInstance_ID());
							
							if(qtyHand == null || qtyHand.compareTo(ma.getMovementQty())<0)
							{
								return "Stock Insuficiente :"+(qtyHand==null?Env.ZERO:qtyHand) + " linea "+line.getLine();
						        
						    }
						}
				}
			}
			
			if(getMovementType().equals("C-"))
			{
				int abastLoc_ID = getLocatorTo(line.getM_Warehouse_ID());
				if(line.getM_Locator_ID() == abastLoc_ID)
				{
					return  "No Valid Locator line " + line.getLine();
					
				}
			}
		}
		return null;
	}
	
	/**faaguilar OFB*/
	private String checkPolicy(){
		
		MInOutLine[] lines = getLines(true);
		for(MInOutLine line:lines)
		{
			if(line.getReversalLine_ID()>0 || line.getM_Product_ID()<=0 || !line.getM_Product().isStocked())
				continue;
			
			BigDecimal qtyFound = DB.getSQLValueBD(get_TrxName(), "Select SUM(MovementQty) from M_InOutLineMA where M_InOutLine_ID=?" , line.getM_InOutLine_ID());
			if(qtyFound !=null && qtyFound.compareTo(line.getMovementQty())==0)
				return null;
			
			if(qtyFound !=null && qtyFound.compareTo(line.getMovementQty())!=0)
				if(!ADialog.ask(2, new ConfirmPanel(true), "La politica de material de la linea "+ line.getLine() + " se volvera a calcular") )
					return " Cancelado";
		    
		}
		
		return null;
	}
	
	/**
	 * faaguilar OFB
	 * reservations new table*/
	public void OFBReservation(int M_WareHouse_ID, int M_Product_ID, BigDecimal ordered, BigDecimal reserved)
	{
		PreparedStatement pstmt = null;
		
		String mysql="SELECT * from M_StorageReservation where M_WareHouse_ID = ? and M_Product_ID = ?";
		try
		{
			pstmt = DB.prepareStatement(mysql, get_TrxName());
			pstmt.setInt(1, M_WareHouse_ID);
			pstmt.setInt(2, M_Product_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				X_M_StorageReservation storage = new X_M_StorageReservation (getCtx(),rs,get_TrxName() );
					storage.setQtyReserved(storage.getQtyReserved().add(reserved));
					storage.setQtyOrdered(storage.getQtyOrdered().add(ordered));
					
					if(storage.getQtyOrdered().signum()<0)
						storage.setQtyOrdered(Env.ZERO);
					if(storage.getQtyReserved().signum()<0)
						storage.setQtyReserved(Env.ZERO);
						
					storage.save();
			}
				
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		
		
	}
	
	/**faaguilar OFB*/
	public boolean existReservationTable()
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean val = false;
		
		String mysql="SELECT count(1) from M_StorageReservation";
		
		if(!DB.isOracle())
			mysql = "select count(1) from AD_Table where tablename='M_StorageReservation'";
		try
		{
			pstmt = DB.prepareStatement(mysql, get_TrxName());
			rs = pstmt.executeQuery();
			if (rs.next())
				if(rs.getInt(1)>0)
					val = true;
			
		}
		catch (Exception e)
		{
			
			val = false;
		}
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		return val;
	}
	//ininoles nuevo metodo que permite generar archivos xml de guias de despacho
	public String CreateXML()
    {
        MDocType doc = new MDocType(getCtx(), getC_DocType_ID(), get_TrxName());
        if(doc.get_Value("CreateXML") == null)
            return "";
        if(!((Boolean)doc.get_Value("CreateXML")).booleanValue())
            return "";
        int typeDoc = Integer.parseInt((String)doc.get_Value("DocumentNo"));
        if(typeDoc == 0)
            return "";
        String mylog = new String();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation implementation = builder.getDOMImplementation();
            Document document = implementation.createDocument(null, "DTE", null);
            document.setXmlVersion("1.0");
            Element Documento = document.createElement("Documento");
            document.getDocumentElement().appendChild(Documento);
            Documento.setAttribute("ID", (new StringBuilder()).append("DTE-").append(getDocumentNo()).toString());
            Element Encabezado = document.createElement("Encabezado");
            Documento.appendChild(Encabezado);
            Element IdDoc = document.createElement("IdDoc");
            Encabezado.appendChild(IdDoc);
            mylog = "IdDoc";
            Element TipoDTE = document.createElement("TipoDTE");
            org.w3c.dom.Text text = document.createTextNode(Integer.toString(typeDoc));
            TipoDTE.appendChild(text);
            IdDoc.appendChild(TipoDTE);
            Element Folio = document.createElement("Folio");
            org.w3c.dom.Text fo = document.createTextNode(getDocumentNo());
            Folio.appendChild(fo);
            IdDoc.appendChild(Folio);
            Element FchEmis = document.createElement("FchEmis");
            org.w3c.dom.Text emis = document.createTextNode(getMovementDate().toString().substring(0, 10));
            FchEmis.appendChild(emis);
            IdDoc.appendChild(FchEmis);
            
            //ininoles end
            
            //tipo de despacho ininoles            
            /*String DVRule = "select rlt.name from AD_Ref_List rl left join AD_Ref_List_Trl rlt on (rl.AD_Ref_List_ID = rlt.AD_Ref_List_ID )"+
            	"where AD_Reference_ID = 152 and rl.value = '"+getDeliveryViaRule() +"' and ad_language like '"+Env.getAD_Language(getCtx())+"' ";
            String DVRuleName = DB.getSQLValueString(get_TrxName(), DVRule);*/
            
            Element TipoDespacho = document.createElement("TipoDespacho");
            org.w3c.dom.Text TDespacho = document.createTextNode("2");
            TipoDespacho.appendChild(TDespacho);
            IdDoc.appendChild(TipoDespacho);
            //ininoles end
            
            
          //ininoles indicaciones traslado
            String IndTrl = null;
            try {
            	IndTrl = get_ValueAsString("IndTraslado");
            }
            catch (Exception e) {
            	IndTrl = null;
			}
            if ( IndTrl != null && IndTrl.length()>0)
            {
            	Element IndTraslado = document.createElement("IndTraslado");
                org.w3c.dom.Text iTraslado = document.createTextNode(IndTrl);
                IndTraslado.appendChild(iTraslado);
                IdDoc.appendChild(IndTraslado);         	
            }
            
            //ininoles nuevo campo termino de pago
            if (getC_Order_ID() > 0)
            {
            	MOrder order = new MOrder(getCtx(), getC_Order_ID(), get_TrxName());            	
            	MPaymentTerm pterm = new MPaymentTerm(getCtx(), order.getC_PaymentTerm_ID(), get_TrxName());
                Element PayTerm = document.createElement("TermPagoGlosa");
                org.w3c.dom.Text term = document.createTextNode(pterm.getName());
                PayTerm.appendChild(term);
                IdDoc.appendChild(PayTerm);            	
            }
                        
            //ininoles nuevo campo vendedor       
            
            //indicacion
            
            //ininoles nuevo campo descripcion cabecera                        
            /*Element HDescription = document.createElement("HeaderDescription");
            org.w3c.dom.Text Hdesc = document.createTextNode(getDescription()==null?" ":getDescription());
            HDescription.appendChild(Hdesc);
            IdDoc.appendChild(HDescription);*/
            //end ininoles
            
            //ininoles nuevo campo orden de venta            
            /*if (getC_Order_ID() > 0)
            {
            	Element SalesOrder = document.createElement("SalesOrder");
            	org.w3c.dom.Text SOrder = document.createTextNode(getC_Order().getDocumentNo());
            	SalesOrder.appendChild(SOrder);
            	IdDoc.appendChild(SalesOrder);
            }*/
            //end ininoles
                        
            Element Emisor = document.createElement("Emisor");
            Encabezado.appendChild(Emisor);
            mylog = "Emisor";
            MOrg company = MOrg.get(getCtx(), getAD_Org_ID());
            Element Rut = document.createElement("RUTEmisor");
            org.w3c.dom.Text rut = document.createTextNode((String)company.get_Value("Rut"));
            Rut.appendChild(rut);
            Emisor.appendChild(Rut);
            //ininoles validacion nuevo nombre razon social
            String nameRzn = company.getDescription();
            if (nameRzn == null)
            {
            	nameRzn = " ";
            }
            nameRzn = nameRzn.trim();
            if (nameRzn.length() < 2)
            	nameRzn = company.getName();
            //ininoles end            
            Element RznSoc = document.createElement("RznSoc");
            org.w3c.dom.Text rzn = document.createTextNode(nameRzn);
            RznSoc.appendChild(rzn);
            Emisor.appendChild(RznSoc);
            Element GiroEmis = document.createElement("GiroEmis");
            org.w3c.dom.Text gi = document.createTextNode((String)company.get_Value("Giro"));
            GiroEmis.appendChild(gi);
            Emisor.appendChild(GiroEmis);
            Element Acteco = document.createElement("Acteco");
            org.w3c.dom.Text teco = document.createTextNode((String)company.get_Value("Acteco"));
            Acteco.appendChild(teco);
            Emisor.appendChild(Acteco);
            Element DirOrigen = document.createElement("DirOrigen");
            org.w3c.dom.Text dir = document.createTextNode((String)company.get_Value("Address1"));
            DirOrigen.appendChild(dir);
            Emisor.appendChild(DirOrigen);
            
            Element CmnaOrigen = document.createElement("CmnaOrigen");
            org.w3c.dom.Text com = document.createTextNode((String)company.get_Value("Comuna"));
            CmnaOrigen.appendChild(com);
            Emisor.appendChild(CmnaOrigen);
            Element CiudadOrigen = document.createElement("CiudadOrigen");
            org.w3c.dom.Text city = document.createTextNode((String)company.get_Value("City"));
            CiudadOrigen.appendChild(city);
            Emisor.appendChild(CiudadOrigen);
            mylog = "receptor";
            MBPartner BP = new MBPartner(getCtx(), getC_BPartner_ID(), get_TrxName());
            MBPartnerLocation bloc = new MBPartnerLocation(getCtx(), getC_BPartner_Location_ID(), get_TrxName());
            Element Receptor = document.createElement("Receptor");
            Encabezado.appendChild(Receptor);
            Element RUTRecep = document.createElement("RUTRecep");
            org.w3c.dom.Text rutc = document.createTextNode((new StringBuilder()).append(BP.getValue()).append("-").append(BP.get_ValueAsString("Digito")).toString());
            RUTRecep.appendChild(rutc);
            Receptor.appendChild(RUTRecep);
            Element RznSocRecep = document.createElement("RznSocRecep");
            org.w3c.dom.Text RznSocR = document.createTextNode(BP.getName());
            RznSocRecep.appendChild(RznSocR);
            Receptor.appendChild(RznSocRecep);
            Element GiroRecep = document.createElement("GiroRecep");
            org.w3c.dom.Text giro = document.createTextNode((String)BP.get_Value("Giro"));
            GiroRecep.appendChild(giro);
            Receptor.appendChild(GiroRecep);
            
            Element ContactoRecep = document.createElement("Contacto");
            org.w3c.dom.Text contacto = document.createTextNode(getAD_User_ID()>0?this.getAD_User().getName():" "); //nombre completo contacto
            ContactoRecep.appendChild(contacto);
            Receptor.appendChild(ContactoRecep);
            
            Element CorreoRecep = document.createElement("CorreoRecep");
            org.w3c.dom.Text corrRecep = document.createTextNode(this.getAD_User().getEMail()==null?" ":this.getAD_User().getEMail()); //mail del contacto
            CorreoRecep.appendChild(corrRecep);
            Receptor.appendChild(CorreoRecep);
            
            
            Element DirRecep = document.createElement("DirRecep");
            org.w3c.dom.Text dirr = document.createTextNode(bloc.getLocation(true).getAddress1());
            DirRecep.appendChild(dirr);
            Receptor.appendChild(DirRecep);
            
            /*if(bloc.getLocation(true).getAddress2()!=null && bloc.getLocation(true).getAddress2().length()>0 ){
	            Element CmnaRecep = document.createElement("CmnaRecep");
	            org.w3c.dom.Text Cmna = document.createTextNode(bloc.getLocation(true).getAddress2());
	            CmnaRecep.appendChild(Cmna);
	            Receptor.appendChild(CmnaRecep);
            }*/
            Element CmnaRecep = document.createElement("CmnaRecep");
	        org.w3c.dom.Text Cmna = document.createTextNode(bloc.getLocation(true).getAddress3()==null?" ":bloc.getLocation(true).getAddress3());
	        CmnaRecep.appendChild(Cmna);
	        Receptor.appendChild(CmnaRecep);
            
            Element CiudadRecep = document.createElement("CiudadRecep");
            org.w3c.dom.Text reg = document.createTextNode(bloc.getLocation(true).getC_City_ID()>0?MCity.get(getCtx(), bloc.getLocation(true).getC_City_ID()).getName():"Santiago");
            CiudadRecep.appendChild(reg);
            Receptor.appendChild(CiudadRecep);
            
            //ininoles nuevos campos pedidos por hernani
            Element transporte = document.createElement("Transporte");
            Encabezado.appendChild(transporte);
            
            Element DirDest = document.createElement("DirDest");
            org.w3c.dom.Text dirdest = document.createTextNode(bloc.getLocation(true).getAddress1());
            DirDest.appendChild(dirdest);
            transporte.appendChild(DirDest);
            
            Element CmnaDest = document.createElement("CmnaDest");
	        org.w3c.dom.Text CmnaDestTxt = document.createTextNode(bloc.getLocation(true).getAddress3()==null?" ":bloc.getLocation(true).getAddress3());
	        CmnaDest.appendChild(CmnaDestTxt);
	        transporte.appendChild(CmnaDest);
            
            Element CiudadDest = document.createElement("CiudadDest");
            org.w3c.dom.Text regDest = document.createTextNode(bloc.getLocation(true).getC_City_ID()>0?MCity.get(getCtx(), bloc.getLocation(true).getC_City_ID()).getName():"Santiago");
            CiudadDest.appendChild(regDest);
            transporte.appendChild(CiudadDest);
            
            mylog = "Totales";
            Element Totales = document.createElement("Totales");
            Encabezado.appendChild(Totales);
            
            BigDecimal amountGrandT = Env.ZERO;            
            BigDecimal priceT = Env.ZERO;
            BigDecimal taxAmt = Env.ZERO;
            if (getC_Order_ID() > 0)
            {
            	//calculo de monto de la guia
            	MInOutLine iLines2[] = getLines();
            	for(int a = 0; a < iLines2.length; a++)
                {
            		priceT = Env.ZERO;
            		taxAmt = Env.ZERO;
            		MInOutLine iLine = iLines2[a];
            		if(iLine.getC_OrderLine_ID() > 0)
                    {      
            			priceT = iLine.getC_OrderLine().getPriceEntered();   
                    }            		
            		priceT = priceT.multiply(iLine.getQtyEntered());
            		if(iLine.getC_OrderLine().getC_Tax_ID() > 0)
            		{
            			if (iLine.getC_OrderLine().getC_Tax().getRate().compareTo(Env.ZERO) > 0)
            			{
            				taxAmt = priceT.multiply(iLine.getC_OrderLine().getC_Tax().getRate());
            				taxAmt = taxAmt.divide(Env.ONEHUNDRED);
            			}
            		}
            		priceT = priceT.add(taxAmt);
            		amountGrandT = amountGrandT.add(priceT);
                }
            }
                       
            Element TasaIVA = document.createElement("TasaIVA");
	        org.w3c.dom.Text tiva = document.createTextNode("19");
	        TasaIVA.appendChild(tiva);
	        Totales.appendChild(TasaIVA);	        
	        
            Element MntTotal = document.createElement("MntTotal");
            org.w3c.dom.Text total = document.createTextNode(amountGrandT.setScale(0, 4).toString());
            MntTotal.appendChild(total);
            Totales.appendChild(MntTotal);
           
            mylog = "detalle";
            MInOutLine iLines[] = getLines();
            for(int i = 0; i < iLines.length; i++)
            {
            	MInOutLine iLine = iLines[i];
            	if(iLine.getM_Product_ID()==0 && iLine.getC_Charge_ID()==0)
            		continue;
            	
                Element Detalle = document.createElement("Detalle");
                Documento.appendChild(Detalle);
              
                Element NroLinDet = document.createElement("NroLinDet");
                org.w3c.dom.Text line = document.createTextNode(Integer.toString(iLine.getLine() / 10));
                NroLinDet.appendChild(line);
                Detalle.appendChild(NroLinDet);
                Element NmbItem = document.createElement("NmbItem");
                String pname="";
                if(iLine.getProduct()!=null )
                	pname=iLine.getProduct().getName();
                else
                	pname=iLine.getC_Charge().getName();
                org.w3c.dom.Text Item = document.createTextNode(pname);
                NmbItem.appendChild(Item);
                Detalle.appendChild(NmbItem);
                
                /*Element DscItem = document.createElement("DscItem");
                org.w3c.dom.Text desc = document.createTextNode(iLine.getDescription()==null?" ":iLine.getDescription());
                DscItem.appendChild(desc);
                Detalle.appendChild(DscItem);*/
                
                /*Element QtyRef = document.createElement("QtyRef");
                org.w3c.dom.Text qty = document.createTextNode(iLine.getQtyEntered().toString());
                QtyRef.appendChild(qty);
                Detalle.appendChild(QtyRef);*/                
                
                //ininoles unidad de medida
                /*if (iLine.getM_Product_ID() > 0)
                {	
                	Element UomRef = document.createElement("UnmdItem");
                    org.w3c.dom.Text uom = document.createTextNode(iLine.getProduct().getC_UOM().getName()==null?" ":iLine.getProduct().getC_UOM().getName());
                    UomRef.appendChild(uom);
                    Detalle.appendChild(UomRef);                	
                }*/
                
                //ininoles end
                //ininoles seteo de monto
                BigDecimal mtoItem = Env.ZERO;
                BigDecimal prcRefMnt = Env.ZERO;
                if(iLine.getC_OrderLine_ID() > 0)
                {                	   
                	prcRefMnt = iLine.getC_OrderLine().getPriceEntered();
                	mtoItem = prcRefMnt.multiply(iLine.getQtyEntered());
                }
                
                Element PrcRef = document.createElement("PrcRef");
                org.w3c.dom.Text PrcRefTxt = document.createTextNode(prcRefMnt.setScale(0, 4).toString());
                PrcRef.appendChild(PrcRefTxt);
                Detalle.appendChild(PrcRef);
                                
                Element QtyItem = document.createElement("QtyItem");
                org.w3c.dom.Text qt = document.createTextNode(iLine.getQtyEntered().toString());
                QtyItem.appendChild(qt);
                Detalle.appendChild(QtyItem);
                
                Element MtoItem = document.createElement("MontoItem");
                org.w3c.dom.Text MtoTxt = document.createTextNode(mtoItem.setScale(0, 4).toString());
                MtoItem.appendChild(MtoTxt);
                Detalle.appendChild(MtoItem);                
            }

            mylog = "referencia";
            String tiporeferencia = new String();
            String folioreferencia  = new String();
            String fechareferencia = new String();
            int tipo_Ref =0;
            
            if(getPOReference() != null && getPOReference().length() > 0)//referencia orden
            {
            	 mylog = "referencia:order";
            	 //MOrder refdoc = new MOrder(getCtx(), ((Integer)get_Value("C_RefOrder_ID")).intValue(), get_TrxName()); 
            	 tiporeferencia = "801";
                 folioreferencia = getPOReference();
                 fechareferencia = this.getDateOrdered().toString().substring(0, 10);
            	 tipo_Ref = 2; //Orden
            }
            
            if(tipo_Ref>0){
                Element Referencia = document.createElement("Referencia");
                Documento.appendChild(Referencia);
                Element NroLinRef = document.createElement("NroLinRef");
                org.w3c.dom.Text Nro = document.createTextNode("1");
                NroLinRef.appendChild(Nro);
                Referencia.appendChild(NroLinRef);
                Element TpoDocRef = document.createElement("TpoDocRef");
                org.w3c.dom.Text tpo = document.createTextNode(tiporeferencia);
                TpoDocRef.appendChild(tpo);
                Referencia.appendChild(TpoDocRef);
                Element FolioRef = document.createElement("FolioRef");
                org.w3c.dom.Text ref = document.createTextNode(folioreferencia);
                FolioRef.appendChild(ref);
                Referencia.appendChild(FolioRef);
                Element FchRef = document.createElement("FchRef");
                org.w3c.dom.Text fchref = document.createTextNode(fechareferencia);
                FchRef.appendChild(fchref);
                Referencia.appendChild(FchRef);
                String CodRefTxt = null;
                try {
                	CodRefTxt = get_ValueAsString("CodRef");
                }
                catch (Exception e) {
                	CodRefTxt = null;
				}
                if ( CodRefTxt != null && CodRefTxt.length()>0)
                {
                	Element CodRef = document.createElement("CodRef");
                    org.w3c.dom.Text codref = document.createTextNode(get_ValueAsString("CodRef")==null?"0":get_ValueAsString("CodRef"));                
                    CodRef.appendChild(codref);
                    Referencia.appendChild(CodRef);                	
                }                
                
            }
            //fin referencia
            mylog = "firma";
            Element Firma = document.createElement("TmstFirma");
            Timestamp today = new Timestamp(TimeUtil.getToday().getTimeInMillis());
            org.w3c.dom.Text Ftext = document.createTextNode((new StringBuilder()).append(today.toString().substring(0, 10)).append("T").append(today.toString().substring(11, 19)).toString());
            Firma.appendChild(Ftext);
            Documento.appendChild(Firma);
            mylog = "archivo";
            String ExportDir = (String)company.get_Value("ExportDir");
            ExportDir = ExportDir.replace("\\", "/");
            javax.xml.transform.Source source = new DOMSource(document);
            javax.xml.transform.Result result = new StreamResult(new File(ExportDir, (new StringBuilder()).append(getDocumentNo()).append(".xml").toString()));
            javax.xml.transform.Result console = new StreamResult(System.out);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("encoding", "ISO-8859-1");
            transformer.transform(source, result);
            transformer.transform(source, console);
        }
        catch(Exception e)
        {
            log.severe((new StringBuilder()).append("CreateXML: ").append(mylog).append("--").append(e.getMessage()).toString());
            return (new StringBuilder()).append("CreateXML: ").append(mylog).append("--").append(e.getMessage()).toString();
        }
        return "XML Generated";
    }
	
	/**
	 * faaguilar is la linea tiene relacion con activos*/
	private boolean isAsset(MInOutLine sLine)
	{
		if(sLine.getC_Charge_ID()>0)
			  if(sLine.getC_Charge().getC_ChargeType_ID()>0)
				if( (sLine.getC_Charge().getC_ChargeType().getValue().equals("TCAF")) )
						return true;
		if(sLine.getM_Product_ID()>0)
			if(sLine.getM_Product().getM_Product_Category().getA_Asset_Group_ID()>0 )
				return true;
		
		return false;
	}
	
}	//	MInOut

