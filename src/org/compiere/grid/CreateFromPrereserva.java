/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.compiere.grid;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPrereserva;
import org.compiere.model.MPrereservaLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRMA;
import org.compiere.model.MRMALine;
import org.compiere.model.MRequisitionLine;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

/**
 *
 *  @author Fabian Aguilar
 *  @version  $Id: CreateFromOrder.java,v 1.4 2006/07/30 00:51:28 jjanke Exp $
 *
 * @author Teo Sarca, SC ARHIPAC SERVICE SRL
 * 			<li>BF [ 1896947 ] Generate invoice from Order error
 * 			<li>BF [ 2007837 ] VCreateFrom.save() should run in trx
 */
public class CreateFromPrereserva extends CreateFrom
{
	/**
	 *  Protected Constructor
	 *  @param mTab MTab
	 */
	public CreateFromPrereserva(GridTab mTab)
	{
		super(mTab);
		log.info(mTab.toString());
	}   //  VCreateFromInvoice

	/**
	 *  Dynamic Init
	 *  @return true if initialized
	 */
	public boolean dynInit() throws Exception
	{
		log.config("");
		setTitle(Msg.getElement(Env.getCtx(), "OV_Prereserva_ID", false) + " .. " + Msg.translate(Env.getCtx(), "CreateFrom"));

		return true;
	}   //  dynInit

	/**
	 *  List number of rows selected
	 */
	public void info()
	{

	}   //  infoInvoice

	protected void configureMiniTable (IMiniTable miniTable)
	{
		miniTable.setColumnClass(0, Boolean.class, false);      //  0-Selection
		miniTable.setColumnClass(1, String.class, true);        //  1--C_OrderLine
		miniTable.setColumnClass(2, String.class, true);    //  2-Product
		miniTable.setColumnClass(3, BigDecimal.class, true);		//  3- QtyEntered
		miniTable.setColumnClass(4, BigDecimal.class, true);        // 4-QtyDisponible
		miniTable.setColumnClass(5, BigDecimal.class, false);        // 4-QtyDisponible
		
		//  Table UI
		miniTable.autoSize();
	}

	/**
	 *  Save - Create Invoice Lines
	 *  @return true if saved
	 */
	public boolean save(IMiniTable miniTable, String trxName)
	{
//		int Order_ID=((Integer)getGridTab().getValue("C_Order_ID")).intValue();
//		MOrder newOrder = new MOrder (Env.getCtx(), Order_ID, null);
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue())
			{				
				KeyNamePair orderline_id = (KeyNamePair)miniTable.getValueAt(i, 1);
				MOrderLine orderLine = new MOrderLine(Env.getCtx(), orderline_id.getKey(), trxName);
				
				KeyNamePair product_id = (KeyNamePair)miniTable.getValueAt(i, 2);
				MProduct product = new MProduct(Env.getCtx(), product_id.getKey(), trxName);
				
				if ( ((BigDecimal)miniTable.getValueAt(i, 5)).compareTo((BigDecimal)miniTable.getValueAt(i, 4)) > 0 )
					throw new AdempiereException("Cant. Solicitada("+((BigDecimal)miniTable.getValueAt(i, 5)).setScale(2)+") del producto "+product.getValue()+" no puede ser mayor a Cant. Disponible("+(BigDecimal)miniTable.getValueAt(i, 4)+")");
			}   //   if selected
		}   //  for all rows
		
		int prereserva_ID=((Integer)getGridTab().getValue("OV_Prereserva_ID")).intValue();
		MPrereserva newPrereserva = new MPrereserva(Env.getCtx(), prereserva_ID, null);
		MOrderLine orderLine = new MOrderLine(Env.getCtx(), ((KeyNamePair)miniTable.getValueAt(0, 1)).getKey(), trxName);
		newPrereserva.setC_Order_ID(orderLine.getC_Order_ID());
		newPrereserva.saveEx();
		// Eliminar lineas de prereserva
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM OV_PrereservaLine WHERE OV_Prereserva_ID=" + prereserva_ID);
		try {
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.execute();
			pstmt.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql.toString(), e);
		}
  
		//  Lines
		int linePreventa = 0;
		for (int i = 0; i < miniTable.getRowCount(); i++)
		{
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue())
			{
				MPrereservaLine line = new MPrereservaLine(newPrereserva);
				linePreventa += 10;
				KeyNamePair product_id = (KeyNamePair)miniTable.getValueAt(i, 2);
				line.setProduct(new MProduct(Env.getCtx(),product_id.getKey(),null));
				line.setPrice();
				line.set_CustomColumn("PriceList", line.getPriceActual());
				line.set_CustomColumn("PriceEntered", line.getPriceActual());
				KeyNamePair orderline_id = (KeyNamePair)miniTable.getValueAt(i, 1);
				line.setC_OrderLine_ID(orderline_id.getKey());
				line.setQty((BigDecimal)miniTable.getValueAt(i, 5));
				
				line.set_CustomColumn("discount2", BigDecimal.ZERO);
				line.set_CustomColumn("discount3", BigDecimal.ZERO);
				line.set_CustomColumn("discount4", BigDecimal.ZERO);
				line.set_CustomColumn("discount5", BigDecimal.ZERO);
				
				line.setLine(linePreventa);
				
			    line.save();
			}   //   if selected
		}   //  for all rows
		
		return true;
	}   //  saveInvoice

	protected Vector<String> getOISColumnNames()
	{
		//  Header Info
		Vector<String> columnNames = new Vector<String>(10);
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add(Msg.getElement(Env.getCtx(), "DateRequired"));
		columnNames.add("Usuario");
		columnNames.add("NoSolicitud");
		columnNames.add("Proyecto/OT");
		columnNames.add(Msg.translate(Env.getCtx(), "ProductName"));
		columnNames.add("Cargo");
		columnNames.add(Msg.translate(Env.getCtx(), "Description"));
		columnNames.add(Msg.translate(Env.getCtx(), "Quantity"));
		columnNames.add(Msg.translate(Env.getCtx(), "Price"));
		columnNames.add("Control");
		columnNames.add("Seguimiento");
		

	    return columnNames;
	}

}
