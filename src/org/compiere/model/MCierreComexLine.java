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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
/**
 *	CierreComex Line Model
 *	
 */
public class MCierreComexLine extends X_OV_CierreComexLine
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2567343619431184322L;
	
	/**	Product					*/
	private MProduct 		m_product = null;

	/**
	 * Get corresponding CierreComex Line for given Order Line
	 * @param ctx
	 * @param C_OrderLine_ID order line
	 * @param trxName
	 * @return CierreComex Line
	 */
	public static MCierreComexLine[] forC_Order_ID(Properties ctx, int C_Order_ID, String trxName)
	{
		final String whereClause = "EXISTS (SELECT 1 FROM C_OrderLine ol"
										+" WHERE ol.C_OrderLine_ID=OV_CierreComexLine.C_OrderLine_ID"
										+" AND ol.C_Order_ID=?)";
		List<MCierreComexLine> list = new Query(ctx, I_OV_CierreComexLine.Table_Name, whereClause, trxName)
			.setParameters(C_Order_ID)
			.list();
		return list.toArray(new MCierreComexLine[list.size()]);
	}
	
	/**
	 * UnLink CierreComex Lines for given Order
	 * @param ctx
	 * @param C_Order_ID
	 * @param trxName
	 */
	public static void unlinkC_Order_ID(Properties ctx, int C_Order_ID, String trxName)
	{
		for (MCierreComexLine line : MCierreComexLine.forC_Order_ID(ctx, C_Order_ID, trxName))
		{
			line.setC_OrderLine_ID(0);
			line.saveEx();
		}
	}
	

	/**
	 * Get corresponding CierreComex Line(s) for given Order Line
	 * @param ctx
	 * @param C_OrderLine_ID order line
	 * @param trxName
	 * @return array of CierreComex Line(s)
	 */
	public static MCierreComexLine[] forC_OrderLine_ID(Properties ctx, int C_OrderLine_ID, String trxName)
	{
		final String whereClause = COLUMNNAME_C_OrderLine_ID+"=?";
		List<MCierreComexLine> list = new Query(ctx, I_OV_CierreComexLine.Table_Name, whereClause, trxName)
			.setParameters(C_OrderLine_ID)
			.list();
		return list.toArray(new MCierreComexLine[list.size()]);
	}

	/**
	 * UnLink CierreComex Lines for given Order Line
	 * @param ctx
	 * @param C_OrderLine_ID
	 * @param trxName
	 */
	public static void unlinkC_OrderLine_ID(Properties ctx, int C_OrderLine_ID, String trxName)
	{
		for (MCierreComexLine line : forC_OrderLine_ID(ctx, C_OrderLine_ID, trxName))
		{
			line.setC_OrderLine_ID(0);
			line.saveEx();
		}
	}


	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param OV_CierreComexLine_ID id
	 *	@param trxName transaction
	 */
	public MCierreComexLine (Properties ctx, int OV_CierreComexLine_ID, String trxName)
	{
		super (ctx, OV_CierreComexLine_ID, trxName);
		if (OV_CierreComexLine_ID == 0)
		{
			setLine(0);
			setQtyDelivered(BigDecimal.ZERO);
			setPriceEntered(BigDecimal.ZERO);
			setLineNetAmt(BigDecimal.ZERO);
			setOldCost(BigDecimal.ZERO);
			setNewCost(BigDecimal.ZERO);
			setPriceList(BigDecimal.ZERO);
			setMkup_List(BigDecimal.ZERO);
			setPriceLiq(BigDecimal.ZERO);
			setMkup_Liq(BigDecimal.ZERO);
			setTotalCost(BigDecimal.ZERO);
			setPorcentaje_Flete(BigDecimal.ZERO);
			setGastos_Generales(BigDecimal.ZERO);
			setArancel(BigDecimal.ZERO);
		}
		
	}	//	OVCierreComexLine

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MCierreComexLine (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MCierreComexLine

	/**
	 * 	Parent Constructor
	 *	@param req CierreComex
	 */
	public MCierreComexLine (MOrder order)
	{
		this (order.getCtx(), 0, order.get_TrxName());
		setClientOrg(order);
		setC_Order_ID(order.getC_Order_ID());
		m_M_PriceList_ID = order.getM_PriceList_ID();
		m_parent = order;
	}	//	MCierreComexLine

	/** Parent					*/
	private MOrder	m_parent = null;
	
	/**	PriceList				*/
	private int 	m_M_PriceList_ID = 0;
	
	/**
	 * 	Get Parent
	 *	@return parent
	 */
	public MOrder getParent()
	{
		if (m_parent == null)
			m_parent = new MOrder (getCtx(), 0, get_TrxName());
		return m_parent;
	}	//	getParent
	
	/**
	 * 	Set Price
	 */
	public void setPrice()
	{
		if (getM_Product_ID() == 0)
			return;
		if (m_M_PriceList_ID == 0)
			m_M_PriceList_ID = getParent().getM_PriceList_ID();
		if (m_M_PriceList_ID == 0)
		{
			throw new AdempiereException("PriceList unknown!");
		}
	}	//	setPrice
	
	
	/**************************************************************************
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
//		if (newRecord && getParent().isComplete()) {
//			log.saveError("ParentComplete", Msg.translate(getCtx(), "OV_CierreComexLine"));
//			return false;
//		}
		
//		String sqlSum = "SELECT (SELECT coalesce(sum(pl.Qty),0)" + 
//						" 					  FROM OV_PrereservaLine pl, OV_Prereserva p" + 
//						" 					  WHERE pl.OV_Prereserva_ID = p.OV_Prereserva_ID" + 
//						"                     AND p.DocStatus IN ('CO','CL')" + 
//						"                     AND pl.C_OrderLine_ID = ol.C_OrderLine_ID" + 
//						"                     AND OV_PrereservaLine_ID != ?)" + 
//						" FROM C_OrderLine ol" + 
//						" WHERE C_OrderLine_ID = ?";
//		int suma = DB.getSQLValueEx(get_TrxName(), sqlSum, getOV_PrereservaLine_ID(), getC_OrderLine_ID());

//		if (getC_OrderLine_ID() != 0) {
//			BigDecimal qtyDisponible = getC_OrderLine().getQtyEntered().subtract(new BigDecimal(suma));
//			if (getQty().compareTo(qtyDisponible) > 0) {
//				throw new AdempiereException("Cant. Solicitada("+getQty().setScale(2)+") del producto "+getM_Product().getValue()+" no puede ser mayor a Cant. Disponible("+qtyDisponible+")");
////				log.saveError("Error", "Cantidad no puede ser mayor a la Cantidad de la Orden");
////				return false;
//			}
//		}
		
//		if (((BigDecimal)get_Value("Discount2")).compareTo(BigDecimal.ZERO) < 0 || ((BigDecimal)get_Value("Discount2")).compareTo(new BigDecimal(100)) > 0) {
//			throw new AdempiereException("Porcentaje debe ser entre 0 y 100");
//		}
//		
//		if (((BigDecimal)get_Value("Discount3")).compareTo(BigDecimal.ZERO) < 0 || ((BigDecimal)get_Value("Discount3")).compareTo(new BigDecimal(100)) > 0) {
//			throw new AdempiereException("Porcentaje debe ser entre 0 y 100");
//		}
		
		if (getLine() == 0)
		{
			String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM OV_CierreComexLine WHERE C_Order_ID=?";
			int ii = DB.getSQLValueEx (get_TrxName(), sql, getC_Order_ID());
			setLine (ii);
		}
		//	Product & ASI
//		if (getM_AttributeSetInstance_ID() != 0)
//			setM_AttributeSetInstance_ID(0);
		
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
		return success;
	}	//	afterSave

	
	/**
	 * 	After Delete
	 *	@param success
	 *	@return true/false
	 */
	protected boolean afterDelete (boolean success)
	{
		return success;
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
		}
		else
		{
			setM_Product_ID(0);
			set_ValueNoCheck ("C_UOM_ID", null);
		}
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
	}	//	setM_Product_ID
	
	/**
	 * 	Set Product and UOM
	 *	@param M_Product_ID product
	 *	@param C_UOM_ID uom
	 */
	public void setM_Product_ID (int M_Product_ID, int C_UOM_ID)
	{
		super.setM_Product_ID (M_Product_ID);
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
//	private boolean updateHeader()
//	{
//		log.fine("");
//		String sql = "UPDATE OV_CierreComex r"
//			+ " SET TotalLines="
//				+ "(SELECT COALESCE(SUM(LineNetAmt),0) FROM OV_CierreComexLine rl "
//				+ "WHERE r.OV_CierreComex_ID=rl.OV_CierreComex_ID) "
//			+ "WHERE OV_CierreComex_ID=?";
//		int no = DB.executeUpdateEx(sql, get_TrxName());
//		if (no != 1)
//			log.log(Level.SEVERE, "Header update #" + no);
//		m_parent = null;
//		return no == 1;
//	}	//	updateHeader
	
}	//	OVCierreComexLine
