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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/**
 *	Llegada Model
 *	
 *  @author Isaac Castro
 *
 */
public class MLlegada extends X_OV_Llegada implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 898606565778668659L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param OV_Llegada_ID id
	 */
	public MLlegada (Properties ctx, int OV_Llegada_ID, String trxName)
	{
		super (ctx, OV_Llegada_ID, trxName);
		if (OV_Llegada_ID == 0)
		{
		//	setDocumentNo (null);
		//	setAD_User_ID (0);
//			setM_PriceList_ID (0);
		//	setM_Warehouse_ID(0);
			setDateDoc(new Timestamp(System.currentTimeMillis()));
			setFecha_Llegada(new Timestamp(System.currentTimeMillis()));
			setDocAction (DocAction.ACTION_Complete);	// CO
			setDocStatus (DocAction.STATUS_Drafted);		// DR
			setIsApproved (false);
			setPosted (false);
			setProcessed (false);
		}
	}	//	OVLlegada

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MLlegada (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MLlegada
	
	/** Lines						*/
	private MLlegadaLine[]		m_lines = null;
	
	private MLlegada[]	m_llegadas = null;
	
	/**
	 * 	Get Lines
	 *	@return array of lines
	 */
	public MLlegadaLine[] getLines()
	{
		if (m_lines != null) {
			set_TrxName(m_lines, get_TrxName());
			return m_lines;
		}
		
		//red1 - FR: [ 2214883 ] Remove SQL code and Replace for Query  
 	 	final String whereClause = I_OV_LlegadaLine.COLUMNNAME_OV_Llegada_ID+"=?";
	 	List <MLlegadaLine> list = new Query(getCtx(), I_OV_LlegadaLine.Table_Name, whereClause, get_TrxName())
			.setParameters(get_ID())
			.setOrderBy(I_OV_LlegadaLine.COLUMNNAME_Line)
			.list();
	 	//  red1 - end -

		m_lines = new MLlegadaLine[list.size ()];
		list.toArray (m_lines);
		return m_lines;
	}	//	getLines
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("OVLlegada[");
		sb.append(get_ID()).append("-").append(getDocumentNo())
			.append(",Status=").append(getDocStatus()).append(",Action=").append(getDocAction())
			.append ("]");
		return sb.toString ();
	}	//	toString
	
	/**
	 * 	Get Document Info
	 *	@return document info
	 */
	public String getDocumentInfo()
	{
		return Msg.getElement(getCtx(), "OV_Llegada_ID") + " " + getDocumentNo();
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
	//	ReportEngine re = ReportEngine.get (getCtx(), ReportEngine.INVOICE, getC_Invoice_ID());
	//	if (re == null)
			return null;
	//	return re.getPDF(file);
	}	//	createPDF

	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		// Set DocumentNo
		MOrder order = new MOrder(getCtx(), getC_Order_ID(), null);
		if (order.get_Value("OV_OCMCERRADA").equals("Y")) {
			m_processMsg = "Orden de Compra Madre Cerrada";
			return false;
		}
		if (newRecord) {
			String sql = "SELECT COALESCE(COUNT(*),0)+1 FROM OV_Llegada WHERE C_Order_ID=?";
			int ii = DB.getSQLValueEx (get_TrxName(), sql, getC_Order_ID());
			setDocumentNo(order.getDocumentNo() + " - " + ii);
		}

		// Calculo de fecha llegada (3 dias habiles)
		Timestamp fechaETA = (Timestamp)get_Value("DATEETA");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(fechaETA.getTime());
		for (int i=1; i <= 3; i++) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				cal.add(Calendar.DAY_OF_YEAR, 2);
			} else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				cal.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		if ((Timestamp)get_ValueOld("DATEETA") != (Timestamp)get_Value("DATEETA")) {
			set_CustomColumn("FECHA_LLEGADA", new Timestamp(cal.getTimeInMillis()));
		}
		// Calculo fecha recepcion (5 dias habiles)
		Timestamp fechaLlegada = (Timestamp)get_Value("FECHA_LLEGADA");
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(fechaLlegada.getTime());
		for (int i=1; i <= 5; i++) {
			cal2.add(Calendar.DAY_OF_YEAR, 1);
			if (cal2.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				cal2.add(Calendar.DAY_OF_YEAR, 2);
			} else if (cal2.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				cal2.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		if ((Timestamp)get_ValueOld("FECHA_LLEGADA") != (Timestamp)get_Value("FECHA_LLEGADA")) {
			set_CustomColumn("FECHA_RECEPCION", new Timestamp(cal2.getTimeInMillis()));
		}
		String sql = "SELECT DATEETA, DATEETD, FECHA_RECEPCION, FECHA_LLEGADA, OV_ESTADO_ETD, OV_ESTADO_ETA, OV_ESTADO_LLEGADA, OV_ESTADO_RECEPCION"
				+ " FROM OV_Llegada"
				+ " WHERE C_Order_ID=" + getC_Order_ID() + ""
				+ " ORDER BY FECHA_LLEGADA DESC";
		
		Statement stm = DB.createStatement();
		ResultSet res = null;
		try {
			res = stm.executeQuery(sql);
			if (res.next()) {
				if (res.getTimestamp("FECHA_LLEGADA").after((Timestamp)get_Value("FECHA_LLEGADA"))) {
					order.set_CustomColumn("DATEETA", res.getTimestamp("DATEETA"));
					order.set_CustomColumn("DATEETD", res.getTimestamp("DATEETD"));
					order.set_CustomColumn("FECHA_RECEPCION", res.getTimestamp("FECHA_RECEPCION"));
					order.set_CustomColumn("FECHA_LLEGADA", res.getTimestamp("FECHA_LLEGADA"));
					order.set_CustomColumn("OV_ESTADO_ETD", res.getString("OV_ESTADO_ETD"));
					order.set_CustomColumn("OV_ESTADO_ETA", res.getString("OV_ESTADO_ETA"));
					order.set_CustomColumn("OV_ESTADO_LLEGADA", res.getString("OV_ESTADO_LLEGADA"));
					order.set_CustomColumn("OV_ESTADO_RECEPCION", res.getString("OV_ESTADO_RECEPCION"));
					order.set_CustomColumn("OV_DIASTRANSITO", TimeUnit.DAYS.convert(res.getTimestamp("DATEETA").getTime() - res.getTimestamp("DATEETD").getTime(), TimeUnit.MILLISECONDS));
					if (!order.save()) {
						m_processMsg = "Error al guardar fechas en OC";
						return false;
					}
				} else {
					order.set_CustomColumn("DATEETA", (Timestamp)get_Value("DATEETA"));
					order.set_CustomColumn("DATEETD", (Timestamp)get_Value("DATEETD"));
					order.set_CustomColumn("FECHA_RECEPCION", (Timestamp)get_Value("FECHA_RECEPCION"));
					order.set_CustomColumn("FECHA_LLEGADA", (Timestamp)get_Value("FECHA_LLEGADA"));
					order.set_CustomColumn("OV_ESTADO_ETD", (String)get_Value("OV_ESTADO_ETD"));
					order.set_CustomColumn("OV_ESTADO_ETA", (String)get_Value("OV_ESTADO_ETA"));
					order.set_CustomColumn("OV_ESTADO_LLEGADA", (String)get_Value("OV_ESTADO_LLEGADA"));
					order.set_CustomColumn("OV_ESTADO_RECEPCION", (String)get_Value("OV_ESTADO_RECEPCION"));
					order.set_CustomColumn("OV_DIASTRANSITO", TimeUnit.DAYS.convert(res.getTimestamp("DATEETA").getTime() - res.getTimestamp("DATEETD").getTime(), TimeUnit.MILLISECONDS));
					if (!order.save()) {
						m_processMsg = "Error al guardar fechas en OC";
						return false;
					}
				}
			} else {
				order.set_CustomColumn("DATEETA", (Timestamp)get_Value("DATEETA"));
				order.set_CustomColumn("DATEETD", (Timestamp)get_Value("DATEETD"));
				order.set_CustomColumn("FECHA_RECEPCION", (Timestamp)get_Value("FECHA_RECEPCION"));
				order.set_CustomColumn("FECHA_LLEGADA", (Timestamp)get_Value("FECHA_LLEGADA"));
				order.set_CustomColumn("OV_ESTADO_ETD", (String)get_Value("OV_ESTADO_ETD"));
				order.set_CustomColumn("OV_ESTADO_ETA", (String)get_Value("OV_ESTADO_ETA"));
				order.set_CustomColumn("OV_ESTADO_LLEGADA", (String)get_Value("OV_ESTADO_LLEGADA"));
				order.set_CustomColumn("OV_ESTADO_RECEPCION", (String)get_Value("OV_ESTADO_RECEPCION"));
				order.set_CustomColumn("OV_DIASTRANSITO", TimeUnit.DAYS.convert(res.getTimestamp("DATEETA").getTime() - res.getTimestamp("DATEETD").getTime(), TimeUnit.MILLISECONDS));
				if (!order.save()) {
					m_processMsg = "Error al guardar fechas en OC";
					return false;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
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
		MOrder order = new MOrder(getCtx(), getC_Order_ID(), null);
		if (newRecord) {
			MOrderLine[] orderLines = order.getLines();
			for (MOrderLine orderLine : orderLines) {
				if (orderLine.getM_Product_ID() != 0) {
					int ll_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(1004984,'N') from c_charge where c_charge_ID=1000010"));
					String sqlSumQtyLlegadas = "SELECT SUM(ll.qty)"
							+ " FROM OV_LlegadaLine ll, OV_Llegada l"
							+ " WHERE ll.OV_Llegada_ID = l.OV_Llegada_ID"
							+ " AND l.C_Order_ID = "+getC_Order_ID()
							+ " AND ll.M_Product_ID = "+orderLine.getM_Product_ID()
							+ " AND l.DocStatus = 'CO'";
					BigDecimal qty = orderLine.getQtyEntered().subtract(new BigDecimal(DB.getSQLValue(get_TrxName(), sqlSumQtyLlegadas)));
					String sql = "INSERT INTO OV_LlegadaLine (ov_llegadaline_id, ad_client_id, ad_org_id, createdby, updatedby, ov_llegada_id, m_product_id, c_orderline_id, qty, priceactual, c_uom_id, linenetamt, line) "
							+ "VALUES ("+ll_id+", "+getAD_Client_ID()+", "+getAD_Org_ID()+", "+getCreatedBy()+", "+getCreatedBy()+", "+getOV_Llegada_ID()+", "+orderLine.getM_Product_ID()+", "+orderLine.getC_OrderLine_ID()+", "+qty+", "+orderLine.getPriceEntered()+", "+orderLine.getC_UOM_ID()+", 0, "+orderLine.getLine()+")";
					DB.executeUpdate(sql, get_TrxName());
//					MLlegadaLine llegadaLine = new MLlegadaLine(llegada);
//					llegadaLine.setOV_Llegada_ID(getOV_Llegada_ID());
//					llegadaLine.setAD_Org_ID(getAD_Org_ID());
//					llegadaLine.setM_Product_ID(orderLine.getM_Product_ID());
//					llegadaLine.setC_OrderLine_ID(orderLine.getC_OrderLine_ID());
//					llegadaLine.setQty(orderLine.getQtyEntered());
//					llegadaLine.setPriceActual(orderLine.getPriceEntered());
//					llegadaLine.setC_UOM_ID(orderLine.getC_UOM_ID());
//					llegadaLine.setLineNetAmt(orderLine.getQtyEntered().multiply(orderLine.getPriceEntered()));
//					llegadaLine.saveEx();
				}
			}
		}
		return true;
	}	//	afterSave
	
	@Override
	protected boolean beforeDelete() {
		for (MLlegadaLine line : getLines()) {
			line.deleteEx(true);
		}
		return true;
	}

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
	private String			m_processMsg = null;
	/**	Just Prepared Flag			*/
	private boolean 		m_justPrepared = false;

	/**
	 * 	Unlock Document.
	 * 	@return true if success 
	 */
	public boolean unlockIt()
	{
		log.info("unlockIt - " + toString());
		setProcessing(false);
		return true;
	}	//	unlockIt
	
	/**
	 * 	Invalidate Document
	 * 	@return true if success 
	 */
	public boolean invalidateIt()
	{
		log.info("invalidateIt - " + toString());
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
		MLlegadaLine[] lines = getLines();
		
		if(lines.length == 0)
		{
			throw new AdempiereException("@NoLines@");
		}
		
		//	Std Period open?
		MPeriod.testPeriodOpen(getCtx(), getDateDoc(), MDocType.DOCBASETYPE_PurchaseRequisition, getAD_Org_ID());
		
		//	Add up Amounts
		BigDecimal totalLines = Env.ZERO;
		for (int i = 0; i < lines.length; i++)
		{
			MLlegadaLine line = lines[i];
			BigDecimal lineNet = line.getQty().multiply(line.getPriceActual());
			if (lineNet.compareTo(line.getLineNetAmt()) != 0)
			{
				line.setLineNetAmt(lineNet);
				line.saveEx();
			}
			totalLines = totalLines.add (line.getLineNetAmt());
		}
		if (totalLines.compareTo(getTotalLines()) != 0)
		{
			setTotalLines(totalLines);
			saveEx();
		}
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		m_justPrepared = true;
		return DocAction.STATUS_InProgress;
	}	//	prepareIt
	
	/**
	 * 	Approve Document
	 * 	@return true if success 
	 */
	public boolean  approveIt()
	{
		log.info("approveIt - " + toString());
		setIsApproved(true);
		return true;
	}	//	approveIt
	
	/**
	 * 	Reject Approval
	 * 	@return true if success 
	 */
	public boolean rejectIt()
	{
		log.info("rejectIt - " + toString());
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
		
		//	Implicit Approval
		if (!isApproved())
			approveIt();
		log.info(toString());
		
		//	User Validation
		String valid = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (valid != null)
		{
			m_processMsg = valid;
			return DocAction.STATUS_Invalid;
		}

		// Set the definite document number after completed (if needed)
		setDefiniteDocumentNo();

		//
		setProcessed(true);
		setDocAction(ACTION_Close);
		return DocAction.STATUS_Completed;
	}	//	completeIt
	
	/**
	 * 	Set the definite document number after completed
	 */
	private void setDefiniteDocumentNo() {
		/*MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		if (dt.isOverwriteDateOnComplete()) {
			setDateDoc(new Timestamp (System.currentTimeMillis()));
		}
		if (dt.isOverwriteSeqOnComplete()) {
			String value = DB.getDocumentNo(getC_DocType_ID(), get_TrxName(), true, this);
			if (value != null)
				setDocumentNo(value);
		}*/
		
		MDocType dt = MDocType.get(getCtx(), 0);
		if (dt.isOverwriteDateOnComplete()) {
			setDateDoc(new Timestamp (System.currentTimeMillis()));
		}
		if (dt.isOverwriteSeqOnComplete()) {
			String value = DB.getDocumentNo(0, get_TrxName(), true, this);
			if (value != null)
				setDocumentNo(value);
		}
	}
	
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
		String sql = "UPDATE OV_LlegadaLine SET Processed='"
			+ (processed ? "Y" : "N")
			+ "' WHERE OV_Llegada_ID=" + getOV_Llegada_ID();
		int noLine = DB.executeUpdate(sql, get_TrxName());
		m_lines = null;
		log.fine(processed + " - Lines=" + noLine);
	}	//	setProcessed

	/**
	 * 	Void Document.
	 * 	Same as Close.
	 * 	@return true if success 
	 */
	public boolean voidIt()
	{
		log.info("voidIt - " + toString());
		// Before Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_VOID);
		if (m_processMsg != null)
			return false;
		
		if (!closeIt())
			return false;
		
		// After Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_VOID);
		if (m_processMsg != null)
			return false;
		
		return true;
	}	//	voidIt
	
	/**
	 * 	Close Document.
	 * 	Cancel not delivered Qunatities
	 * 	@return true if success 
	 */
	public boolean closeIt()
	{
		log.info("closeIt - " + toString());
		// Before Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
		if (m_processMsg != null)
			return false;
		
		//	Close Not delivered Qty
		MLlegadaLine[] lines = getLines();
		BigDecimal totalLines = Env.ZERO;
		for (int i = 0; i < lines.length; i++)
		{
			MLlegadaLine line = lines[i];
			BigDecimal finalQty = line.getQty();
			if (line.getC_OrderLine_ID() == 0)
				finalQty = Env.ZERO;
			//	final qty is not line qty
			if (finalQty.compareTo(line.getQty()) != 0)
			{
				String description = line.getDescription();
				if (description == null)
					description = "";
				description += " [" + line.getQty() + "]"; 
				line.setDescription(description);
				line.setQty(finalQty);
				line.setLineNetAmt();
				line.saveEx();
			}
			totalLines = totalLines.add (line.getLineNetAmt());
		}
		if (totalLines.compareTo(getTotalLines()) != 0)
		{
			setTotalLines(totalLines);
			saveEx();
		}
		// After Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
		if (m_processMsg != null)
			return false;
		
		return true;
	}	//	closeIt
	
	/**
	 * 	Reverse Correction
	 * 	@return true if success 
	 */
	public boolean reverseCorrectIt()
	{
		log.info("reverseCorrectIt - " + toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		// After reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		return false;
	}	//	reverseCorrectionIt
	
	/**
	 * 	Reverse Accrual - none
	 * 	@return true if success 
	 */
	public boolean reverseAccrualIt()
	{
		log.info("reverseAccrualIt - " + toString());
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
	 * 	@return true if success 
	 */
	public boolean reActivateIt()
	{
		log.info("reActivateIt - " + toString());
		// Before reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REACTIVATE);
		if (m_processMsg != null)
			return false;

	//	setProcessed(false);
		if (! reverseCorrectIt())
			return false;

		// After reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REACTIVATE);
		if (m_processMsg != null)
			return false;

		return true;
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
		sb.append(": ").
			append(Msg.translate(getCtx(),"TotalLines")).append("=").append(getTotalLines())
			.append(" (#").append(getLines().length).append(")");
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
	 * 	Get Document Currency
	 *	@return C_Currency_ID
	 */
	public int getC_Currency_ID()
	{
		MPriceList pl = MPriceList.get(getCtx(), getM_PriceList_ID(), get_TrxName());
		return pl.getC_Currency_ID();
//		return 228;
	}

	/**
	 * 	Get Document Approval Amount
	 *	@return amount
	 */
	public BigDecimal getApprovalAmt()
	{
		return getTotalLines();
	}
	
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
	
	public MLlegada[] getLlegadasByOrder(int C_Order_ID) {
		List<MLlegada> list = new Query(getCtx(), I_OV_Llegada.Table_Name, "C_Order_ID=?", get_TrxName())
				.setParameters(C_Order_ID)
				.list();
		m_llegadas = new MLlegada[list.size()];
		list.toArray(m_llegadas);
		return m_llegadas;
	}
	
	/**
	 * 	Set Business Partner (Ship)
	 *	@param C_BPartner_ID bpartner
	 */
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		super.setC_BPartner_ID (C_BPartner_ID);
	}	//	setC_BPartner_ID
	
	/**
	 * 	Set Business Partner Location (Ship+Bill)
	 *	@param C_BPartner_Location_ID bp location
	 */
	public void setC_BPartner_Location_ID (int C_BPartner_Location_ID)
	{
		super.setC_BPartner_Location_ID (C_BPartner_Location_ID);
	}	//	setC_BPartner_Location_ID

	public int getDoc_User_ID() {
		return 0;
	}

	
}	//	MLlegada
