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
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;

/** Generated Interface for OV_CierreComexLine
 *  @author Isaac Castro
 *  @version Release 3.6.0LTS
 */
public interface I_OV_CierreComexLine 
{

    /** TableName=OV_CierreComexLine */
    public static final String Table_Name = "OV_CierreComexLine";

    /** AD_Table_ID */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 1 - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(1);

    /** Load Meta Data */
    
    /** Column name OV_CierreComexLine_ID */
    public static final String COLUMNNAME_OV_CierreComexLine_ID = "OV_CierreComexLine_ID";

	/** Set CierreComex Line.
	  * CierreComex Line
	  */
	public void setOV_CierreComexLine_ID (int OV_CierreComexLine_ID);

	/** Get CierreComex Line.
	  * CierreComex Line
	  */
	public int getOV_CierreComexLine_ID();

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();
	
	/** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();
	
	/** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();
	
	/** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();
	
	/** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();
	
	/** Column name Line */
    public static final String COLUMNNAME_Line = "Line";

	/** Set Line No.
	  * Unique line for this document
	  */
	public void setLine (int Line);

	/** Get Line No.
	  * Unique line for this document
	  */
	public int getLine();
	
	/** Column name C_Order_ID */
    public static final String COLUMNNAME_C_Order_ID = "C_Order_ID";

	/** Set Sales Order.
	  * Sales Order
	  */
	public void setC_Order_ID (int C_Order_ID);

	/** Get Sales Order.
	  * Sales Order
	  */
	public int getC_Order_ID();

	public I_C_Order getC_Order() throws RuntimeException;

    /** Column name C_OrderLine_ID */
    public static final String COLUMNNAME_C_OrderLine_ID = "C_OrderLine_ID";

	/** Set Sales Order Line.
	  * Sales Order Line
	  */
	public void setC_OrderLine_ID (int C_OrderLine_ID);

	/** Get Sales Order Line.
	  * Sales Order Line
	  */
	public int getC_OrderLine_ID();

	public I_C_OrderLine getC_OrderLine() throws RuntimeException;
	
	/** Column name M_Product_ID */
    public static final String COLUMNNAME_M_Product_ID = "M_Product_ID";

	/** Set Product.
	  * Product, Service, Item
	  */
	public void setM_Product_ID (int M_Product_ID);

	/** Get Product.
	  * Product, Service, Item
	  */
	public int getM_Product_ID();

	public I_M_Product getM_Product() throws RuntimeException;
	
	/** Column name QtyDelivered */
    public static final String COLUMNNAME_QtyDelivered = "QtyDelivered";

	/** Set Quantity Delivered.
	  * Quantity
	  */
	public void setQtyDelivered (BigDecimal QtyDelivered);

	/** Get Quantity Delivered.
	  * Quantity
	  */
	public BigDecimal getQtyDelivered();
	
	/** Column name PriceEntered */
    public static final String COLUMNNAME_PriceEntered = "PriceEntered";

	/** Set Unit Price.
	  * Actual Price 
	  */
	public void setPriceEntered (BigDecimal PriceEntered);

	/** Get Unit Price.
	  * Actual Price 
	  */
	public BigDecimal getPriceEntered();
	
	/** Column name LineNetAmt */
    public static final String COLUMNNAME_LineNetAmt = "LineNetAmt";

	/** Set Line Amount.
	  * Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	public void setLineNetAmt (BigDecimal LineNetAmt);

	/** Get Line Amount.
	  * Line Extended Amount (Quantity * Actual Price) without Freight and Charges
	  */
	public BigDecimal getLineNetAmt();
	
	/** Column name OldCost */
    public static final String COLUMNNAME_OldCost = "OldCost";

	/** Set Line OldCost.
	  * OldCost
	  */
	public void setOldCost (BigDecimal OldCost);

	/** Get Line OldCost.
	  * OldCost
	  */
	public BigDecimal getOldCost();
	
	/** Column name NewCost */
    public static final String COLUMNNAME_NewCost = "NewCost";

	/** Set Line NewCost.
	  * NewCost
	  */
	public void setNewCost (BigDecimal NewCost);

	/** Get Line NewCost.
	  * NewCost
	  */
	public BigDecimal getNewCost();
	
	/** Column name PriceList */
    public static final String COLUMNNAME_PriceList = "PriceList";

	/** Set PriceList.
	  * PriceList
	  */
	public void setPriceList (BigDecimal PriceList);

	/** Get PriceList.
	  * PriceList
	  */
	public BigDecimal getPriceList();
	
	/** Column name PriceMKUP_LIST */
    public static final String COLUMNNAME_Mkup_List = "Mkup_List";

	/** Set Mkup_List.
	  * Mkup_List
	  */
	public void setMkup_List (BigDecimal Mkup_List);

	/** Get Mkup_List.
	  * Mkup_List
	  */
	public BigDecimal getMkup_List();
	
	/** Column name PriceLiq */
    public static final String COLUMNNAME_PriceLiq = "PriceLiq";

	/** Set PriceLiq.
	  * PriceLiq
	  */
	public void setPriceLiq (BigDecimal PriceLiq);

	/** Get PriceLiq.
	  * PriceLiq
	  */
	public BigDecimal getPriceLiq();
	
	/** Column name Mkup_Liq */
    public static final String COLUMNNAME_Mkup_Liq = "Mkup_Liq";

	/** Set Mkup_Liq.
	  * Mkup_Liq
	  */
	public void setMkup_Liq (BigDecimal Mkup_Liq);

	/** Get Mkup_Liq.
	  * Mkup_Liq
	  */
	public BigDecimal getMkup_Liq();
	
	/** Column name TotalCost */
    public static final String COLUMNNAME_TotalCost = "TotalCost";

	/** Set TotalCost.
	  * TotalCost
	  */
	public void setTotalCost (BigDecimal TotalCost);

	/** Get TotalCost.
	  * TotalCost
	  */
	public BigDecimal getTotalCost();
	
	/** Column name Porcentaje_Flete */
    public static final String COLUMNNAME_Porcentaje_Flete = "Porcentaje_Flete";

	/** Set Porcentaje_Flete.
	  * Porcentaje_Flete
	  */
	public void setPorcentaje_Flete (BigDecimal Porcentaje_Flete);

	/** Get Porcentaje_Flete.
	  * Porcentaje_Flete
	  */
	public BigDecimal getPorcentaje_Flete();
	
	/** Column name Gastos_Generales */
    public static final String COLUMNNAME_Gastos_Generales = "Gastos_Generales";

	/** Set Gastos_Generales.
	  * Gastos_Generales
	  */
	public void setGastos_Generales (BigDecimal Gastos_Generales);

	/** Get Gastos_Generales.
	  * Gastos_Generales
	  */
	public BigDecimal getGastos_Generales();
	
	/** Column name Arancel */
    public static final String COLUMNNAME_Arancel = "Arancel";

	/** Set Arancel.
	  * Arancel
	  */
	public void setArancel (BigDecimal Arancel);

	/** Get Arancel.
	  * Arancel
	  */
	public BigDecimal getArancel();    

}
