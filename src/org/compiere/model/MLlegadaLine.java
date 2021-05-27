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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
/**
 *	Llegada Line Model
 *	
 */
public class MLlegadaLine extends X_OV_LlegadaLine
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2567343619431184322L;
	
	/**	Product					*/
	private MProduct 		m_product = null;

	/**
	 * Get corresponding Llegada Line for given Order Line
	 * @param ctx
	 * @param C_OrderLine_ID order line
	 * @param trxName
	 * @return Llegada Line
	 */
	public static MLlegadaLine[] forC_Order_ID(Properties ctx, int C_Order_ID, String trxName)
	{
		final String whereClause = "EXISTS (SELECT 1 FROM C_OrderLine ol"
										+" WHERE ol.C_OrderLine_ID=OV_LlegadaLine.C_OrderLine_ID"
										+" AND ol.C_Order_ID=?)";
		List<MLlegadaLine> list = new Query(ctx, I_OV_LlegadaLine.Table_Name, whereClause, trxName)
			.setParameters(C_Order_ID)
			.list();
		return list.toArray(new MLlegadaLine[list.size()]);
	}
	
	/**
	 * UnLink Llegada Lines for given Order
	 * @param ctx
	 * @param C_Order_ID
	 * @param trxName
	 */
	public static void unlinkC_Order_ID(Properties ctx, int C_Order_ID, String trxName)
	{
		for (MLlegadaLine line : MLlegadaLine.forC_Order_ID(ctx, C_Order_ID, trxName))
		{
			line.setC_OrderLine_ID(0);
			line.saveEx();
		}
	}
	

	/**
	 * Get corresponding Llegada Line(s) for given Order Line
	 * @param ctx
	 * @param C_OrderLine_ID order line
	 * @param trxName
	 * @return array of Llegada Line(s)
	 */
	public static MLlegadaLine[] forC_OrderLine_ID(Properties ctx, int C_OrderLine_ID, String trxName)
	{
		final String whereClause = COLUMNNAME_C_OrderLine_ID+"=?";
		List<MLlegadaLine> list = new Query(ctx, I_OV_LlegadaLine.Table_Name, whereClause, trxName)
			.setParameters(C_OrderLine_ID)
			.list();
		return list.toArray(new MLlegadaLine[list.size()]);
	}

	/**
	 * UnLink Llegada Lines for given Order Line
	 * @param ctx
	 * @param C_OrderLine_ID
	 * @param trxName
	 */
	public static void unlinkC_OrderLine_ID(Properties ctx, int C_OrderLine_ID, String trxName)
	{
		for (MLlegadaLine line : forC_OrderLine_ID(ctx, C_OrderLine_ID, trxName))
		{
			line.setC_OrderLine_ID(0);
			line.saveEx();
		}
	}


	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param OV_LlegadaLine_ID id
	 *	@param trxName transaction
	 */
	public MLlegadaLine (Properties ctx, int OV_LlegadaLine_ID, String trxName)
	{
		super (ctx, OV_LlegadaLine_ID, trxName);
		if (OV_LlegadaLine_ID == 0)
		{
		//	setOV_Llegada_ID (0);
			setLine (0);	// @SQL=SELECT COALESCE(MAX(Line),0)+10 AS DefaultValue FROM OV_LlegadaLine WHERE OV_Llegada_ID=@OV_Llegada_ID@
			setLineNetAmt (Env.ZERO);
			setPriceActual (Env.ZERO);
			setQty (Env.ONE);	// 1
		}
		
	}	//	OVLlegadaLine

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MLlegadaLine (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MLlegadaLine

	/**
	 * 	Parent Constructor
	 *	@param req llegada
	 */
	public MLlegadaLine (MLlegada req)
	{
		this (req.getCtx(), 0, req.get_TrxName());
		setClientOrg(req);
		setOV_Llegada_ID(req.getOV_Llegada_ID());
		m_parent = req;
	}	//	MLlegadaLine

	/** Parent					*/
	private MLlegada	m_parent = null;
	
	/**	PriceList				*/
	private int 	m_M_PriceList_ID = 0;
	
	/**
	 * Get Ordered Qty
	 * @return Ordered Qty
	 */
	public BigDecimal getQtyOrdered()
	{
		if (getC_OrderLine_ID() > 0)
			return getQty();
		else
			return Env.ZERO;
	}
	
	/**
	 * 	Get Parent
	 *	@return parent
	 */
	public MLlegada getParent()
	{
		if (m_parent == null)
			m_parent = new MLlegada (getCtx(), getOV_Llegada_ID(), get_TrxName());
		return m_parent;
	}	//	getParent
	
	@Override
	public I_OV_Llegada getOV_Llegada()
	{
		return getParent();
	}

	/**
	 * @return Date when this product is required by planner
	 * @see MLlegada#getDateRequired()
	 */
	public Timestamp getFecha_Llegada()
	{
		return getParent().getFecha_Llegada();
	}
	
	/**
	 * 	Set Price
	 */
	public void setPrice()
	{
		if (getM_Product_ID() == 0)
			return;
		if (m_M_PriceList_ID == 0)
			m_M_PriceList_ID = getParent().getM_PriceList_ID();
//		if (m_M_PriceList_ID == 0)
//		{
//			throw new AdempiereException("PriceList unknown!");
//		}
		setPrice (m_M_PriceList_ID);
	}	//	setPrice
	
	/**
	 * 	Set Price for Product and PriceList
	 * 	@param M_PriceList_ID price list
	 */
	public void setPrice (int M_PriceList_ID)
	{
		if (getM_Product_ID() == 0)
			return;
		//
		log.fine("M_PriceList_ID=" + M_PriceList_ID);
		boolean isSOTrx = false;
		MProductPricing pp = new MProductPricing (getM_Product_ID(), getOV_Llegada().getC_BPartner_ID(), getQty(), isSOTrx);
		pp.setM_PriceList_ID(M_PriceList_ID);
	//	pp.setPriceDate(getDateOrdered());
		//
		setPriceActual (pp.getPriceStd());
	}	//	setPrice

	/**
	 * 	Calculate Line Net Amt
	 */
	public void setLineNetAmt ()
	{
		BigDecimal lineNetAmt = getQty().multiply(getPriceActual());
//		BigDecimal lineNetAmt = getQty().multiply(new BigDecimal(get_Value("PriceEntered").toString()));
		super.setLineNetAmt (lineNetAmt);
	}	//	setLineNetAmt
	
	
	/**************************************************************************
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		if (newRecord && getParent().isComplete()) {
			log.saveError("ParentComplete", Msg.translate(getCtx(), "OV_LegadaLine"));
			return false;
		}
		
		if (getLine() == 0)
		{
			String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM OV_LlegadaLine WHERE OV_Llegada_ID=?";
			int ii = DB.getSQLValueEx (get_TrxName(), sql, getOV_Llegada_ID());
			setLine (ii);
		}
		
		//	Product & ASI
		if (getM_AttributeSetInstance_ID() != 0)
			setM_AttributeSetInstance_ID(0);
		// Product UOM
		if (getM_Product_ID() > 0 && getC_UOM_ID() <= 0)
		{
			setC_UOM_ID(getM_Product().getC_UOM_ID());
		}
		//
		if (getPriceActual().signum() == 0)
			setPrice();
		setLineNetAmt();
		return true;
	}	//	beforeSave
	
	/**
	 * 	After Save.
	 * 	Update Total on Header
	 *	@param newRecord if new record
	 *	@param success save was success
	 *	@return true if saved
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (!success)
			return success;
		return updateHeader();
	}	//	afterSave

	
	/**
	 * 	After Delete
	 *	@param success
	 *	@return true/false
	 */
	protected boolean afterDelete (boolean success)
	{
		if (!success)
			return success;
		return updateHeader();
	}	//	afterDelete
	
	/**
	 * 	Set Product
	 *	@param product product
	 */
	public void setProduct (MProduct product)
	{
		m_product = product;
		if (m_product != null)
		{
			setM_Product_ID(m_product.getM_Product_ID());
			setC_UOM_ID (m_product.getC_UOM_ID());
		}
		else
		{
			setM_Product_ID(0);
			set_ValueNoCheck ("C_UOM_ID", null);
		}
		setM_AttributeSetInstance_ID(0);
	}	//	setProduct

	
	/**
	 * 	Set M_Product_ID
	 *	@param M_Product_ID product
	 *	@param setUOM set also UOM
	 */
	public void setM_Product_ID (int M_Product_ID, boolean setUOM)
	{
		if (setUOM)
			setProduct(MProduct.get(getCtx(), M_Product_ID));
		else
			super.setM_Product_ID (M_Product_ID);
		setM_AttributeSetInstance_ID(0);
	}	//	setM_Product_ID
	
	/**
	 * 	Set Product and UOM
	 *	@param M_Product_ID product
	 *	@param C_UOM_ID uom
	 */
	public void setM_Product_ID (int M_Product_ID, int C_UOM_ID)
	{
		super.setM_Product_ID (M_Product_ID);
		if (C_UOM_ID != 0)
			super.setC_UOM_ID(C_UOM_ID);
		setM_AttributeSetInstance_ID(0);
	}	//	setM_Product_ID
	
	@Override
	public I_M_Product getM_Product()
	{
		return MProduct.get(getCtx(), getM_Product_ID());
	}

	/**
	 * 	Update Header
	 *	@return header updated
	 */
	private boolean updateHeader()
	{
		log.fine("");
		String sql = "UPDATE OV_Llegada r"
			+ " SET TotalLines="
				+ "(SELECT COALESCE(SUM(LineNetAmt),0) FROM OV_LlegadaLine rl "
				+ "WHERE r.OV_Llegada_ID=rl.OV_Llegada_ID) "
			+ "WHERE OV_Llegada_ID=?";
		int no = DB.executeUpdateEx(sql, new Object[]{getOV_Llegada_ID()}, get_TrxName());
		if (no != 1)
			log.log(Level.SEVERE, "Header update #" + no);
		m_parent = null;
		return no == 1;
	}	//	updateHeader
	
}	//	OVLlegadaLine
