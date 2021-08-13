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
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.GridTab;
import org.compiere.model.MCierreComex;
import org.compiere.model.MCierreComexLine;
import org.compiere.model.MClient;
import org.compiere.model.MCost;
import org.compiere.model.MCountry;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.openvia.inacatalog.I_iPedidos;

/**
 *
 *  @author Fabian Aguilar
 *  @version  $Id: CreateFromOrder.java,v 1.4 2006/07/30 00:51:28 jjanke Exp $
 *
 * @author Teo Sarca, SC ARHIPAC SERVICE SRL
 * 			<li>BF [ 1896947 ] Generate invoice from Order error
 * 			<li>BF [ 2007837 ] VCreateFrom.save() should run in trx
 */
public class CreateFromCierreComex extends CreateFrom
{
	private int p_M_PriceList_Version_Liq = 1000016; // Ventas_Liquidacion->Ventas-Liquidacion
	private int p_M_PriceList_Version_Venta = 1000012; // Ventas->Precios 30-03-09
	private int p_M_CostType_ID = 1000000; // Windsor UN/33 Chilean Peso
	private int p_C_AcctSchema_ID = 1000000; // Windsor UN/33 Chilean Peso
	private int p_M_CostElement_ID = 1000000; // Standard Costing
	private int p_M_PriceList_Version_Compra = 1000001; // Compras
	/**
	 *  Protected Constructor
	 *  @param mTab MTab
	 */
	public CreateFromCierreComex(GridTab mTab)
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
		setTitle("Cierre Comex");

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
//		miniTable.setColumnClass(3, BigDecimal.class, true);		//  3- QtyEntered
		
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
//		for (int i = 0; i < miniTable.getRowCount(); i++)
//		{
//			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue())
//			{
//				KeyNamePair orderline_id = (KeyNamePair)miniTable.getValueAt(i, 1);
//				MOrderLine orderLine = new MOrderLine(Env.getCtx(), orderline_id.getKey(), trxName);
//				
//				KeyNamePair product_id = (KeyNamePair)miniTable.getValueAt(i, 2);
//				MProduct product = new MProduct(Env.getCtx(), product_id.getKey(), trxName);
//			}   //   if selected
//		}   //  for all rows
		
		int order_ID=((Integer)getGridTab().getValue("C_Order_ID")).intValue();
		MOrder order = new MOrder(Env.getCtx(), order_ID, null);
//		MCierreComex newCierreComex = new MCierreComex(Env.getCtx(), order_ID, null);
//		newCierreComex.setC_Order_ID(orderLine.getC_Order_ID());
//		newCierreComex.saveEx();
		// Eliminar lineas de CierreComex
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM OV_CierreComexLine WHERE C_Order_ID=" + order_ID);
		try {
			PreparedStatement pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.execute();
			pstmt.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, sql.toString(), e);
		}
		
		// Calcular FobTotal
		BigDecimal fobTotal = BigDecimal.ZERO;
		StringBuffer errors = new StringBuffer();
		
		if (calculaFleteTotal(order_ID).compareTo(BigDecimal.ZERO) == 0)
			errors.append("Flete Total 0").append("\n");
		
		MCountry country = new MCountry(Env.getCtx(), order.get_ValueAsInt("C_Country_ID"), null);
		if (country.get_Value("ov_arancel") == null)
			errors.append("Pais " + country.getName() + " sin Arancel").append("\n");
		
		if (MClient.get(Env.getCtx(), order.getAD_Client_ID()).get_Value("ov_gastos_generales") == null)
			errors.append("Compañia sin Gastos Generales").append("\n");
		
		for (int i = 0; i < miniTable.getRowCount(); i++) {
			if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue()) {
				MOrderLine orderLine = new MOrderLine(Env.getCtx(), ((KeyNamePair)miniTable.getValueAt(i, 1)).getKey(), trxName);
//				MProduct product = new MProduct(Env.getCtx(), ((KeyNamePair)miniTable.getValueAt(i, 2)).getKey(), trxName);
				fobTotal = fobTotal.add(orderLine.getQtyEntered().multiply(orderLine.getPriceEntered()));
				
				// Valida que todos los datos esten ingresados
//				if (orderLine.getQtyEntered().compareTo(BigDecimal.ZERO) <= 0)
//					errors.append("Producto " + product.getValue() + " Cantidad recibida debe ser mayor a 0 (Linea " + orderLine.getLine() + ")").append("\n");
//				
//				MCost cost = MCost.get(Env.getCtx(), orderLine.getAD_Client_ID(), 0, product.get_ID(), 1000000, 1000000, 1000000, 0, trxName);
//				if (cost == null || cost.getCurrentCostPrice().compareTo(BigDecimal.ZERO) <= 0)
//					errors.append("Producto " + product.getValue() + " sin costo o costo 0 (Linea " + orderLine.getLine() + ")").append("\n");
//				
//				MProductPrice productPrice = new MProductPrice(Env.getCtx(), 1000012, product.get_ID(), trxName);
//				if (productPrice.getPriceList().compareTo(BigDecimal.ZERO) == 0)
//					errors.append("Producto " + product.getValue() + " sin precio de venta (Linea " + orderLine.getLine() + ")").append("\n");
//				
//				MProductPrice productPriceLiq = new MProductPrice(Env.getCtx(), 1000016, product.get_ID(), trxName);
//				if (productPriceLiq.getPriceList().compareTo(BigDecimal.ZERO) == 0)
//					errors.append("Producto " + product.getValue() + " sin precio de liquidación (Linea " + orderLine.getLine() + ")").append("\n");
			}
		}
//		System.out.println("fobTotal: " + fobTotal);
		
		if (errors.length() == 0) {
		//  Lines
			for (int i = 0; i < miniTable.getRowCount(); i++) {
				if (((Boolean)miniTable.getValueAt(i, 0)).booleanValue()) {
					try {
						MCierreComexLine line = new MCierreComexLine(order);
						KeyNamePair product_id = (KeyNamePair)miniTable.getValueAt(i, 2);
						MOrderLine orderLine = new MOrderLine(Env.getCtx(), ((KeyNamePair)miniTable.getValueAt(i, 1)).getKey(), trxName);
						line.setC_Order_ID(order_ID);
						line.setC_OrderLine_ID(orderLine.get_ID());
						line.setM_Product_ID(product_id.getKey());
						line.setQtyDelivered(orderLine.getQtyEntered());
						line.setPriceEntered(orderLine.getPriceEntered());
						line.setLineNetAmt(orderLine.getPriceEntered().multiply(orderLine.getQtyEntered()));
						//MCost cost = MCost.get(Env.getCtx(), orderLine.getAD_Client_ID(), 0, product_id.getKey(), p_M_CostType_ID, p_C_AcctSchema_ID, p_M_CostElement_ID, 0, trxName);
						line.setOldCost(MProductPrice.get(Env.getCtx(), p_M_PriceList_Version_Compra, product_id.getKey(), trxName).getPriceList());
						BigDecimal fleteTotal = calculaFleteTotal(order_ID);
						BigDecimal porcentaje_flete = fleteTotal.divide(fobTotal, 6, BigDecimal.ROUND_HALF_UP);
						//BigDecimal flete = BigDecimal.ONE.add(new BigDecimal(order.get_Value("ov_porcentaje_flete").toString()));
						BigDecimal porcentajeGtoArancel = (new BigDecimal(MClient.get(Env.getCtx(), order.getAD_Client_ID()).get_Value("ov_gastos_generales").toString()).add(new BigDecimal(country.get_Value("ov_arancel").toString()))).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
						BigDecimal gtoArancel = BigDecimal.ONE.add(porcentajeGtoArancel);
						BigDecimal flete = porcentaje_flete.add(BigDecimal.ONE);
						System.out.println("orderLine.getPriceEntered(): " + orderLine.getPriceEntered());
						System.out.println("flete: " + flete);
						System.out.println("gtoArancel: " + gtoArancel);
						System.out.println("dolar: " + new BigDecimal(order.get_Value("dolar").toString()));
						BigDecimal newCost = orderLine.getPriceEntered().multiply(flete).multiply(gtoArancel).multiply(new BigDecimal(order.get_Value("dolar").toString())).setScale(0, BigDecimal.ROUND_HALF_UP);
						line.setNewCost(newCost);
						// 1000012: Ventas->Precios 30-03-09
						line.setPriceList(MProductPrice.get(Env.getCtx(), p_M_PriceList_Version_Venta, product_id.getKey(), trxName).getPriceList());
						line.setMkup_List(MProductPrice.get(Env.getCtx(), p_M_PriceList_Version_Venta, product_id.getKey(), trxName).getPriceList().divide(newCost, 2, BigDecimal.ROUND_HALF_UP));
						// 1000016: Ventas_Liquidacion->Ventas-Liquidacion
						line.setPriceLiq(MProductPrice.get(Env.getCtx(), p_M_PriceList_Version_Liq, product_id.getKey(), trxName).getPriceList());
						line.setMkup_Liq(MProductPrice.get(Env.getCtx(), p_M_PriceList_Version_Liq, product_id.getKey(), trxName).getPriceList().divide(newCost, 2, BigDecimal.ROUND_HALF_UP));
						line.setTotalCost(orderLine.getQtyEntered().multiply(newCost));
						line.setPorcentaje_Flete(porcentaje_flete.multiply(new BigDecimal(100)));
						line.setGastos_Generales(new BigDecimal(MClient.get(Env.getCtx(), order.getAD_Client_ID()).get_Value("ov_gastos_generales").toString()));
						line.setArancel(new BigDecimal(country.get_Value("ov_arancel").toString()));
					    if (!line.save())
					    	log.severe("Error al crear linea de cierre comex (Linea OC " + orderLine.getLine() + ")");
					} catch (Exception e) {
						e.printStackTrace();
						log.fine("Error: " + e);
						throw new AdempiereException("Error: " + e.toString());
					}
				}   //   if selected
			}   //  for all rows
		} else {
			throw new AdempiereException(errors.toString());
		}
		
		return true;
	}   //  saveInvoice
	
	private BigDecimal calculaFleteTotal(int orderId) {
		BigDecimal fleteTotal = BigDecimal.ZERO;
		String sql = "";
		String ocmCerrada = DB.getSQLValueString(null, "SELECT OV_OCMCERRADA FROM C_Order WHERE C_Order_ID = " + orderId);
		
		if (ocmCerrada.equals("Y")) {
			sql = "SELECT coalesce(sum((coalesce(ov_fletecontainer,0)*OV_CANTCONT) + (coalesce(ov_fletecontainer2,0)*OV_CANTCONT2) + (coalesce(ov_fletecontainer3,0)*OV_CANTCONT3) + (coalesce(ov_fletecontainer4,0)*OV_CANTCONT4)),0)"
					+ " FROM ov_llegada"
					+ " WHERE C_Order_ID = " + orderId;
			fleteTotal = new BigDecimal(DB.getSQLValue(null, sql));
		} else {
			sql = "SELECT coalesce(ov_fletecontainer,0)*coalesce(OV_CANTCONT,0) + coalesce(ov_fletecontainer2,0)*coalesce(OV_CANTCONT2,0) + coalesce(ov_fletecontainer3,0)*coalesce(OV_CANTCONT3,0) + coalesce(ov_fletecontainer4,0)*coalesce(OV_CANTCONT4,0)"
					+ " FROM C_Order"
					+ " WHERE C_Order_ID = " + orderId;
			fleteTotal = new BigDecimal(DB.getSQLValue(null, sql));
		}
		return fleteTotal;
	}

	protected Vector<String> getOISColumnNames()
	{
		//  Header Info
		Vector<String> columnNames = new Vector<String>(10);
		columnNames.add(Msg.getMsg(Env.getCtx(), "Select"));
		columnNames.add(Msg.getElement(Env.getCtx(), "Line"));
		columnNames.add("Código");
		columnNames.add(Msg.translate(Env.getCtx(), "ProductName"));
		columnNames.add("Cantidad");
		columnNames.add("Precio");
		columnNames.add("Neto Linea");

	    return columnNames;
	}

}
