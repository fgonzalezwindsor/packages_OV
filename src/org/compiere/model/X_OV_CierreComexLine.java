/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

/** Generated Model for OV_CierreComexLine
 *  @author Isaac Castro
 *  @version Release 3.6.0LTS - $Id$ */
public class X_OV_CierreComexLine extends PO implements I_OV_CierreComexLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20100614L;

    /** Standard Constructor */
    public X_OV_CierreComexLine (Properties ctx, int OV_CierreComexLine_ID, String trxName)
    {
      super (ctx, OV_CierreComexLine_ID, trxName);
    }

    /** Load Constructor */
    public X_OV_CierreComexLine (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 1 - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_VO_CierreComexLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }
	
	public I_C_OrderLine getC_OrderLine() throws RuntimeException
    {
		return (I_C_OrderLine)MTable.get(getCtx(), I_C_OrderLine.Table_Name)
			.getPO(getC_OrderLine_ID(), get_TrxName());	}

	/** Set Sales Order Line.
		@param C_OrderLine_ID 
		Sales Order Line
	  */
	public void setC_OrderLine_ID (int C_OrderLine_ID)
	{
		if (C_OrderLine_ID < 1) 
			set_Value (COLUMNNAME_C_OrderLine_ID, null);
		else 
			set_Value (COLUMNNAME_C_OrderLine_ID, Integer.valueOf(C_OrderLine_ID));
	}

	/** Get Sales Order Line.
		@return Sales Order Line
	  */
	public int getC_OrderLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_OrderLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Line No.
		@param Line 
		Unique line for this document
	  */
	public void setLine (int Line)
	{
		set_Value (COLUMNNAME_Line, Integer.valueOf(Line));
	}

	/** Get Line No.
		@return Unique line for this document
	  */
	public int getLine () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getLine()));
    }
    
    /** Set Line Amount.
	@param LineNetAmt 
	Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	*/
	public void setLineNetAmt (BigDecimal LineNetAmt)
	{
		set_Value (COLUMNNAME_LineNetAmt, LineNetAmt);
	}
	
	/** Get Line Amount.
		@return Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	public BigDecimal getLineNetAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_LineNetAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
	
	public I_M_Product getM_Product() throws RuntimeException
    {
		return (I_M_Product)MTable.get(getCtx(), I_M_Product.Table_Name)
			.getPO(getM_Product_ID(), get_TrxName());	
	}

	/** Set Product.
		@param M_Product_ID 
		Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID)
	{
		if (M_Product_ID < 1) 
			set_Value (COLUMNNAME_M_Product_ID, null);
		else 
			set_Value (COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
	}

	/** Get Product.
		@return Product, Service, Item
	  */
	public int getM_Product_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_M_Product_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set CierreComex Line.
		@param OV_CierreComexLine_ID 
		CierreComex Line
	  */
	public void setOV_CierreComexLine_ID (int OV_CierreComexLine_ID)
	{
		if (OV_CierreComexLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_OV_CierreComexLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_OV_CierreComexLine_ID, Integer.valueOf(OV_CierreComexLine_ID));
	}

	/** Get CierreComex Line.
		@return CierreComex Line
	  */
	public int getOV_CierreComexLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_OV_CierreComexLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
	
	/** Set Unit Price.
	@param PriceEntered
	Actual Price 
	 */
	public void setPriceEntered (BigDecimal PriceEntered)
	{
		set_Value (COLUMNNAME_PriceEntered, PriceEntered);
	}
	
	/** Get Unit Price.
		@return PriceEntered
	  */
	public BigDecimal getPriceEntered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceEntered);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Quantity.
		@param QtyDelivered
		Quantity
	  */
	public void setQtyDelivered (BigDecimal QtyDelivered)
	{
		set_Value (COLUMNNAME_QtyDelivered, QtyDelivered);
	}

	/** Get Quantity.
		@return QtyDelivered
	  */
	public BigDecimal getQtyDelivered () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyDelivered);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setC_Order_ID(int C_Order_ID) {
		if (C_Order_ID < 1) 
			set_Value (COLUMNNAME_C_Order_ID, null);
		else 
			set_Value (COLUMNNAME_C_Order_ID, Integer.valueOf(C_Order_ID));
	}

	public int getC_Order_ID() {
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Order_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_Order getC_Order() throws RuntimeException {
		return (I_C_Order)MTable.get(getCtx(), I_C_Order.Table_Name)
				.getPO(getC_Order_ID(), get_TrxName());
	}

	public void setOldCost(BigDecimal OldCost) {
		set_Value (COLUMNNAME_OldCost, OldCost);
	}

	public BigDecimal getOldCost() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_OldCost);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setNewCost(BigDecimal NewCost) {
		set_Value (COLUMNNAME_NewCost, NewCost);
	}

	public BigDecimal getNewCost() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_NewCost);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setPriceList(BigDecimal PriceList) {
		set_Value (COLUMNNAME_PriceList, PriceList);
	}

	public BigDecimal getPriceList() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceList);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setMkup_List(BigDecimal Mkup_List) {
		set_Value (COLUMNNAME_Mkup_List, Mkup_List);
	}

	public BigDecimal getMkup_List() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Mkup_List);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setPriceLiq(BigDecimal PriceLiq) {
		set_Value (COLUMNNAME_PriceLiq, PriceLiq);
	}

	public BigDecimal getPriceLiq() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_PriceLiq);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setMkup_Liq(BigDecimal Mkup_Liq) {
		set_Value (COLUMNNAME_Mkup_Liq, Mkup_Liq);
	}

	public BigDecimal getMkup_Liq() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Mkup_Liq);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setTotalCost(BigDecimal TotalCost) {
		set_Value (COLUMNNAME_TotalCost, TotalCost);
	}

	public BigDecimal getTotalCost() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TotalCost);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setPorcentaje_Flete(BigDecimal Porcentaje_Flete) {
		set_Value (COLUMNNAME_Porcentaje_Flete, Porcentaje_Flete);
	}

	public BigDecimal getPorcentaje_Flete() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Porcentaje_Flete);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setGastos_Generales(BigDecimal Gastos_Generales) {
		set_Value (COLUMNNAME_Gastos_Generales, Gastos_Generales);
	}

	public BigDecimal getGastos_Generales() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Gastos_Generales);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public void setArancel(BigDecimal Arancel) {
		set_Value (COLUMNNAME_Arancel, Arancel);
	}

	public BigDecimal getArancel() {
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Arancel);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
	
}