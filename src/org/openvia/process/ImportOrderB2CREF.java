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
package org.openvia.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
//import org.compiere.model.X_I_Order;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
//import org.compiere.util.Env;
import org.windsor.model.X_C_OrderB2C;
import org.windsor.model.X_C_OrderB2CLine;

/**
 *	Import Order from I_Order
 *  @author Oscar Gomez
 * 			<li>BF [ 2936629 ] Error when creating bpartner in the importation order
 * 			<li>https://sourceforge.net/tracker/?func=detail&aid=2936629&group_id=176962&atid=879332
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ImportOrder.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class ImportOrderB2CREF extends SvrProcess
{
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 1000000;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 1000000;
	/**	Delete old Imported				*/
	//private boolean			m_deleteOldImported = false;
	private StringBuffer sql = null;
	private int no = 0;
	private String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;
	/**	Document Action					*/
	//private String			m_docAction = MOrder.DOCACTION_Prepare;

	//variables de ccorreo
		final String miCorreo = "soporte@comercialwindsor.cl";
	    final String miContrasena = "Cw9121100";
	    final String servidorSMTP = "smtp.gmail.com";
	    final String puertoEnvio = "465";
	    String mailReceptor = null;
	    String asunto = null;
	    String cuerpo = null;
	/** Effective						*/
	private Timestamp		m_DateValue = null;
	
	//ICastro //se comentan validaciones //09-07-2020
	private Integer m_M_WareHouse_ID = 1000028;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = 1000000;
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = 1000000;
			else if (name.equals("DeleteOldImported"))
			;//	m_deleteOldImported = "Y".equals(para[i].getParameter());
			else if (name.equals("DocAction"))
		;//		m_docAction = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (m_DateValue == null)
			m_DateValue = new Timestamp (System.currentTimeMillis());
	}	//	prepare


	/**
	 *  Perform process.
	 *  @return Message
	 *  @throws Exception
	 */
	protected String doIt() throws java.lang.Exception
	{
		
		
		//ICastro //se comentan validaciones //09-07-2020
		String menj5=null;
		//String menj1 = null, menj2 = null, menj3 = null,menj4 = null, menj5=null, menj6=null, menj7 = null;
		//	****	Prepare	****

		//	Set Client, Org, IsActive, Created/Updated
		log.info ("Reset=" + actualizaClient ());
        //BP Value
		log.fine("Set BP from Value=" + actualizaBP());
		// BP Location
		log.fine("Found Location=" + actualizaBPL());
		//	Product
		log.fine("Set Product from Value=" + actualizaProduct ());
		//Venedero
		log.fine("Set SalesRep_ID=" +actualizaSalesRep ());
		commitEx();
	
	 ArrayList<String> ordenescompra = new ArrayList<String>(); 
	// ArrayList<String> docerror = new ArrayList<String>();
	// ArrayList<String> recorredocerror = new ArrayList<String>();
		//	-- New Orders -----------------------------------------------------
	String slqoc  = "Select Documentno, BPartnerValue from I_OrderB2C where i_isImported<>'Y' "+clientCheck +  " Group by DocumentNo, BPartnerValue";
	//obtener ordenes para importar
	try
	{
		PreparedStatement pstmt = DB.prepareStatement(slqoc, get_TrxName());
		ResultSet rs = pstmt.executeQuery();
		while (rs.next())
		{
			String sqlcreados = "Select count(I_OrderB2C.DocumentNO) as cuenta from I_orderB2C where I_orderb2c.i_isImported<>'Y' "+
								" and I_orderb2c.documentno in (select C_orderb2c.poreference from c_orderb2c "+
								" where I_orderb2c.documentno=c_orderb2c.poreference and c_orderb2c.bpartnerValue=I_orderb2c.bpartnervalue) "
								+" and I_orderb2c.documentno="+rs.getString("DocumentNo");
			try
			{
				PreparedStatement pstmtc = DB.prepareStatement (sqlcreados, get_TrxName());
				ResultSet rsc = pstmtc.executeQuery ();
				
				if (rsc.next())
					if(rsc.getInt("cuenta")==0)
						ordenescompra.add(rs.getString("DocumentNo"));
					else
					{
						log.fine("Set SalesRep_ID=" +beforeImport (rs.getString("DocumentNo"),rs.getString("BPartnerValue")));
						commitEx();
					}
				rsc.close();
				pstmtc.close();
			}
			catch(Exception e)
			{
				
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		rs.close();
		pstmt.close();
	}
	catch(Exception e)
	{
		
		log.log(Level.SEVERE, e.getMessage(), e);
	}
	//fin obtener ordenes para importar
	
		//int noInsert = 0;
		//int noInsertLine = 0;
	int docok = 0;
	int docnotok= 0;
		if (!ordenescompra.isEmpty()) //ordenes no esta vacio?
		for ( int i=0; i < ordenescompra.size() ; i++ ) // a recorrer Ordenes
		{
			String recorre = "Select documentno, COALESCE(c_bpartner_ID,0) C_BPartner_ID,  COALESCE(c_bpartner_location_ID,0)c_bpartner_location_ID,"+
							"	 COALESCE(M_product_ID, 0) M_product_ID,"+
							" replace(description,'/','-') description, name as tipogt, ContactName formacgt,"+
							" trim(upper(Address2)) as subtienda, ChargeName as vendedor, Coalesce (SalesRep_ID,0) SalesRep_ID , "+
							" LineDescription as Generico, COALESCE(QtyOrdered,0) as Cantidad, COALESCE(PriceActual,0) Precio, Created, ProductValue, BPartnerValue, Address1, DOCTYPENAME "+
							" from  I_OrderB2C where documentNo="+ ordenescompra.get(i);
			//String direccion, rut, vendedor = "";
			//ICastro //se comentan validaciones //09-07-2020
			int errorp = 0;
			/*int errorbp= 0;
			int errorbpl=0;
			int erroruser=0;
			int errorst = 0;
			int errorsl= 0;*/
			
			/* Tabla de errores
			 * 	0: sin errores
			 *  1: sin socio de negicio
			 *  2: sin direccion
			 *  3: sin producto
			 *  4: sin producto ni generico
			 *  5: sin stock
			 *  6: sin vendedor
			 *  7: producto ingresado no existe
			 *  8: Error subtienda
			 *  9: Error sin lineas
			 *  10: una o mas lineas que no pasaron
			 */
			try
			{
				PreparedStatement pstmt = DB.prepareStatement (recorre, get_TrxName());
				ResultSet rs = pstmt.executeQuery ();
				int lineas=1;
				X_C_OrderB2C ob2c= null;
				while (rs.next()) //while crea ob2c 1 por 1
				{
					//ICastro //se comentan validaciones //09-07-2020
					/*if(rs.getInt("Cantidad")>=1)
					{
						//docerror=ordenescompra.get(i);
						if (rs.getInt("C_BPartner_ID")==0)
						{
							errorbp=1;
							menj1="Rut Cliente no encontrado:"+ rs.getString("BPartnerValue");
						}
						if(rs.getInt("C_BPartner_Location_ID")==0)
							{errorbpl=2;
							menj2="Direccion no coincide con la base de datos:"+ rs.getString("Address1");
							}
						if(rs.getInt("M_Product_ID")==0)
							{errorp=3;
							menj3="No viene el codigo Windsor o no coincide con la Base de datos";
							}
						if(rs.getInt("M_Product_ID")==0 && (rs.getString("Generico")==null  | rs.getString("Generico").equals("") ))
							{errorp=4;
							menj4="Sin Codigo Windsor y sin Sku cliente";
							}
					
					}*/
					if (ob2c==null)
					{
						//cabecera
						 ob2c = new X_C_OrderB2C ( getCtx() ,0,get_TrxName() );
//						 if (errorbp==0)
								ob2c.setC_BPartner_ID(rs.getInt("C_BPartner_ID"));
//						 if(errorbpl==0)
							 ob2c.setC_BPartner_Location_ID(rs.getInt("C_BPartner_Location_ID"));
						 String description = rs.getString("description");
						 if(description !=null)
						 {
							 ob2c.set_ValueOfColumn("FechaPrometidaInt", description.substring(0, 10).trim());
							 if (description.length()>10)
							 ob2c.setDescription(description.substring(11, description.length()));
						 }
						
						 ob2c.setPOReference(rs.getString("documentno"));
						 ob2c.setDateAcct(rs.getTimestamp("Created"));
						 ob2c.set_CustomColumn("BPartnerValue", rs.getString("BPartnerValue"));
						 ob2c.set_CustomColumn("Address1", rs.getString("Address1"));
						 //medio de compra y forma de compra para definir bodega
						 String mc ="";
						 if (rs.getString ("tipogt")!=null && !rs.getString("tipogt").isEmpty())
						 {
							 mc=rs.getString("tipogt").substring(0, 1).toUpperCase()+ rs.getString("tipogt").substring(1, rs.getString("tipogt").length()).toLowerCase();
						 }
						 ob2c.set_CustomColumn("MedioCompra", mc);
						 String fc="";
						 if (rs.getString ("formacgt")!=null && !rs.getString("formacgt").isEmpty())
						 {
							
						        String nuevacadena = "";
						        String[] palabras = rs.getString("formacgt").split(" ");
						        for (int x = 0; x < palabras.length; x++) {
						 
						        nuevacadena += palabras[x].substring(0, 1).toUpperCase() + palabras[x].substring(1, palabras[x].length()).toLowerCase() + " ";
						        }
						        fc = nuevacadena.trim();
						       
						 }
						 if (fc.equals("En Verde"))
						 {
							 ob2c.setVentaEnVerde(true);
//							 ob2c.setM_Warehouse_ID(1000025);
							 ob2c.setM_Warehouse_ID(m_M_WareHouse_ID);
						 }
						 else
						 {
							 ob2c.setVentaEnVerde(false);
//							 ob2c.setM_Warehouse_ID(1000025);
							 ob2c.setM_Warehouse_ID(m_M_WareHouse_ID);
						 }
							 
						 ob2c.set_CustomColumn("FormaCompra", fc);
					
							/*if(rs.getInt("SalesRep_ID")==0)
							{	erroruser=6;
								menj6="Venedor no encontrado:"+ rs.getString("vendedor");
							}
							else*/
								ob2c.setSalesRep_ID(rs.getInt("SalesRep_ID"));
						//de aqui saque un cierre parentesisis
						ob2c.setProcessed(false);
						ob2c.setM_PriceList_ID(1000000);
						
						//subtienda
						if(rs.getInt("C_BPartner_ID")>0 && rs.getString("subtienda")!=null)
						{
							String sqlst = "Select count (*) as cuenta from C_BPartner_SubTienda where c_Bpartner_ID="+rs.getInt("C_BPartner_ID") +
									       " and trim(upper(name))= trim(upper('"+rs.getString("subtienda")+"'))";
							 try
							 {
								 	PreparedStatement pstmtst = DB.prepareStatement (sqlst, get_TrxName());
									ResultSet rsst = pstmtst.executeQuery ();
									
									if(rsst.next())
									{
										if (rsst.getInt("cuenta")>0)
										{
											String sqlstv= "Select max( C_BPartner_SubTienda_ID)as C_BPartner_SubTienda_ID  from C_BPartner_SubTienda where c_Bpartner_ID="+rs.getInt("C_BPartner_ID") +
													 " and trim(upper(name))= trim(upper('"+rs.getString("subtienda")+"'))";
											 try
											 {
												 	PreparedStatement pstmtstv = DB.prepareStatement (sqlstv, get_TrxName());
													ResultSet rsstv = pstmtstv.executeQuery ();
													
													if(rsstv.next())
														ob2c.setC_BPartner_SubTienda_ID(rsstv.getInt("C_BPartner_SubTienda_ID"));
													
													rsstv.close();
													pstmtstv.close();
												}
												catch(Exception e)
												{
													
													log.log(Level.SEVERE, e.getMessage(), e);
												}
										}
										/*else
											errorst=8;*/
									}
										
											
									
									rsst.close();
									pstmtst.close();
								}
								catch(Exception e)
								{
									
									log.log(Level.SEVERE, e.getMessage(), e);
								}
							 
						} //subtienda if
						ob2c.save();
				
					}//if crea cabecera
						
					BigDecimal sob = new BigDecimal (0);
					
					if (ob2c!=null)
					{
						X_C_OrderB2CLine obl = new X_C_OrderB2CLine  (getCtx() ,0,get_TrxName());
						obl.setC_OrderB2C_ID(ob2c.getC_OrderB2C_ID());
						obl.setLine(lineas*10);
						BigDecimal cant = new BigDecimal (rs.getInt("Cantidad"));
						obl.setQtyEntered(cant);
						//System.out.print(rs.getInt("Precio"));
						obl.setPriceEntered(rs.getInt("Precio"));
						BigDecimal neto = new BigDecimal (rs.getInt("Cantidad") *rs.getInt("Precio") );
						obl.setLineNetAmt(neto);
						if(rs.getString("Generico")!=null || !rs.getString("Generico").isEmpty())
						obl.setCODIGOGENERICO(rs.getString("Generico"));
						obl.setProductValue(rs.getString("ProductValue"));
						obl.set_ValueOfColumn("NOMBREGENERICO", rs.getString("DOCTYPENAME"));
//						if(errorp==0)
//						{
							String sqlp = "Select count(*) as cuenta from m_product where m_product_ID="+rs.getInt("M_Product_ID");
							//validar si producto existe y si tiene stock en la bodega correspondiente
							try
							{
								
								PreparedStatement pstmtp = DB.prepareStatement (sqlp, get_TrxName());
								ResultSet rsp = pstmtp.executeQuery ();
								
								if (rsp.next())
								{
									if (rsp.getInt("cuenta")==1)
									{
										obl.setM_Product_ID(rs.getInt("M_Product_ID"));
										//validar Stock
										String sqlps = "Select qtyavailableofb(p.m_product_ID,"+ m_M_WareHouse_ID  + ") as Disponible, p.ProductType " +
														//"qtyavailableofb(p.m_product_ID,1000001)+qtyavailableofb(p.m_product_ID,1000010) OtroDisponible"+
													   " from M_product p where  p.m_product_ID="+ rs.getInt("M_Product_ID");
										try
										{
											

											PreparedStatement pstmtps = DB.prepareStatement (sqlps, get_TrxName());
											ResultSet rsps = pstmtps.executeQuery ();
											
											if(rsps.next()) {
												//	System.out.print(rsps.getInt("Disponible"));
												
												if(rsps.getString("ProductType").equals("I")) {
													if (rs.getInt("Cantidad")<=rsps.getInt("Disponible")) {
														//System.out.print("Cantidad:"+rs.getInt("Cantidad") + " Disponible"+rsps.getInt("Disponible"));
														//obl.setProductValue(rs.getString("ProductValue"));
														obl.setPASARAOV(true);
													} else {
														//System.out.print("Cantidad:"+rs.getInt("Cantidad") + " Disponible"+rsps.getInt("Disponible"));
														errorp=5;
														obl.setPASARAOV(false);
//														sob = new BigDecimal (rsps.getInt("OtroDisponible"));
														obl.setErrorMsg("No hay stock en bodega");
														obl.setStockBodegas(sob);
												
													}
												} else {//no es alamcenable
													obl.setPASARAOV(true);
												}
											}
											
											rsps.close();
											pstmtps.close();
										}
										catch(Exception e)
										{
											
											log.log(Level.SEVERE, e.getMessage(), e);
										}
										//valida Stock
									}
								}
								else
								{
//									errorp=7;
									obl.setPASARAOV(false);
									obl.setStockBodegas(sob);
								}
								
							rsp.close();
							pstmtp.close();
						}
						catch(Exception e)
						{
							
							log.log(Level.SEVERE, e.getMessage(), e);
						}
							//validar si producto existe y si tiene stock en la bodega correspondiente
//						}
						if(errorp>0)
						{
							/*if(errorp==3)
								obl.setErrorMsg(menj3);
							else if(errorp==4)
								obl.setErrorMsg(menj4);
							else */
							if(errorp==5) {
								menj5="Error: Producto sin Stock";
							/*	if (rs.getInt("Cantidad")<=sob.intValue())
									menj5+=" Solicite Movimiento de inventario, hay disponible en las otras bodegas";
								else
									menj5+= " No hay o no alcanza el disponible de ninguna bodega";*/
								obl.set_CustomColumn("ErrorMsg", menj5);
								//obl.setErrorMsg("No hay stock en la bodega actual, verificar en otras bodegas");
								obl.setStockBodegas(sob);
								docnotok++;
							//	System.out.print(menj5);
							//	obl.save();
							}
							/*else if(errorp==7)
								menj7="Error: Codigo Windsor ingresadp no Existe.";
								obl.setErrorMsg(menj7);
							
						*/	
						}
						if (errorp==5)
							obl.set_CustomColumn("ErrorMsg", "No hay stock en la bodega actual");	

						/*if(errorst==8)
							obl.set_CustomColumn("ErrorMsg", "Subtienda no existe:"+rs.getString("subtienda"));*/
					obl.save();
					}
					lineas++;
				
				
				
				} //while que recorre los productos de las ordenes
				
				//Crear nota de venta sin errores
//				if (errorp + errorbp +  errorbpl +  erroruser + errorst==0)
				if (errorp==0)
				{
					//sin errores controlados
					MOrder order = null;
					int contador = 1;
					int contadordoc=0;
					
					//validar que todas las lineas esten okey y que hau lineas
					String sqlvl = "Select count(*) cuenta from C_OrderB2CLine where C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
					
					try
					{
						
						PreparedStatement pstmtvl = DB.prepareStatement (sqlvl, get_TrxName());
						ResultSet rsvl = pstmtvl.executeQuery ();
						
						if(rsvl.next())
						{
							if(rsvl.getInt("cuenta")>0)
							{
								//valida que todas las lineas esten OK
								String sqlvlok= "Select count(*) cuenta from C_OrderB2CLine where PASARAOV<>'Y' and C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
								try
								{
									
									PreparedStatement pstmtvlok = DB.prepareStatement (sqlvlok, get_TrxName());
									ResultSet rsvlok = pstmtvlok.executeQuery ();
									
									if(rsvlok.next())
										if(rsvlok.getInt("cuenta")==0)
										{
											String sqllines = "Select * from C_OrderB2CLine where  C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
										//	int aux=1;
										
											try
											{
												
												PreparedStatement pstmtlines = DB.prepareStatement (sqllines, get_TrxName());
												ResultSet rslines = pstmtlines.executeQuery ();
											while (rslines.next())
											{
												if(order==null)
												{
											//		noInsert ++;
													contadordoc=contadordoc+1;
													order = new MOrder (getCtx() ,0,get_TrxName() );
													MBPartner bp = new MBPartner (getCtx(), ob2c.getC_BPartner_ID(), get_TrxName());
													order.setClientOrg (ob2c.getAD_Client_ID(),ob2c.getAD_Org_ID());
													if (ob2c.getC_BPartner_ID()!=1011716)
													order.setC_DocTypeTarget_ID(1000030);
													else
													order.setC_DocTypeTarget_ID(1000568);	
													order.setIsSOTrx(true);
													order.setDeliveryRule("O");
													order.setC_BPartner_ID(ob2c.getC_BPartner_ID());
													order.setC_BPartner_Location_ID(ob2c.getC_BPartner_Location_ID());
													order.setPOReference(ob2c.getPOReference());
													order.setAD_User_ID(ob2c.getSalesRep_ID());
													//	Bill Partner
													order.setBill_BPartner_ID(ob2c.getC_BPartner_ID());
													order.setBill_Location_ID(ob2c.getC_BPartner_Location_ID());
													//
													if (ob2c.getDescription() != null)
														order.setDescription(ob2c.getDescription());
													if (ob2c.get_ValueAsString ("FechaPrometidaInt") != null)
														order.set_ValueOfColumn("FechaPrometidaInt", ob2c.get_ValueAsString ("FechaPrometidaInt"));
													order.setC_PaymentTerm_ID(bp.getC_PaymentTerm_ID());
													order.setM_PriceList_ID(ob2c.getM_PriceList_ID());
													order.setM_Warehouse_ID(ob2c.getM_Warehouse_ID());
													order.setSalesRep_ID(ob2c.getSalesRep_ID());
													BigDecimal stbd= new BigDecimal(ob2c.getC_BPartner_SubTienda_ID());
													if(stbd!=null && ob2c.getC_BPartner_SubTienda_ID()>0)
														order.set_CustomColumn("C_BPartner_SubTienda_ID", ob2c.getC_BPartner_SubTienda_ID());
													//
													order.setDateOrdered(ob2c.getDateAcct()   );
													order.setDateAcct( ob2c.getDateAcct());
													order.setInvoiceRule("D");
													
													//Faltan firmas horas de firma y quien firmo forma de compra medio compra
													//
													order.set_CustomColumn("FIRMA2", "Y");
													order.set_CustomColumn("FIRMA3", "Y");
													order.set_CustomColumn("FIRMACOM", ob2c.getDateAcct());
													order.set_CustomColumn("FIRMAFIN", ob2c.getDateAcct());
													order.set_CustomColumn("USERFIRMCOM", 1003890); //mcladeron
													order.set_CustomColumn("USERFIRMFIN", 1003303); //eumanzor
													order.set_CustomColumn("FormaCompra", ob2c.get_Value("FormaCompra"));
													order.set_CustomColumn("MedioCompra", ob2c.get_Value("MedioCompra"));
													order.set_CustomColumn("VentaInvierno", "N");
													order.save();
												}
												if (contador<=16)
												{
												//	  noInsertLine++;
													MOrderLine line = new MOrderLine(order);
													X_C_OrderB2CLine lines= new X_C_OrderB2CLine (getCtx() , rslines.getInt("C_OrderB2CLine_ID"),get_TrxName());
												// 	line.setC_Order_ID(order.getC_Order_ID());
													line.setM_Product_ID(lines.getM_Product_ID());
													line.setPriceEntered( new BigDecimal  (lines.getPriceEntered()));
													line.setPriceActual(new BigDecimal  (lines.getPriceEntered()));
													line.setPriceList(new BigDecimal  (lines.getPriceEntered()));
													line.setQtyEntered( (lines.getQtyEntered()));
													line.setLine(contador*10);
													line.setPrice(new BigDecimal  (lines.getPriceEntered()));
													line.setQty(lines.getQtyEntered());
													line.set_CustomColumn("Demand", lines.getQtyEntered());
													
												//	line.setLineNetAmt(rs.getBigDecimal("LineNetAmt"));
													//int la = rs.getBigDecimal("PriceEntered").intValue() * rs.getBigDecimal("QtyEntered").intValue();
													//BigDecimal lab = new BigDecimal (la);
													
													
														
														//line.set_CustomColumn("Discount2", rs.getBigDecimal("Discount2"));
														//line.set_CustomColumn("Discount3", rs.getBigDecimal("Discount3"));
														BigDecimal df = new BigDecimal (0);
														line.set_CustomColumn("Discount2", df);
														//BigDecimal df = new BigDecimal (0);
														line.set_CustomColumn("Discount3", df);
														//BigDecimal df = new BigDecimal (0);
														line.set_CustomColumn("Discount", df);
														line.set_CustomColumn("Discount3", df);
														line.set_CustomColumn("Discount4", df);
														line.set_CustomColumn("Discount5", df);
														line.set_CustomColumn("NotPrint", "N");
													
													
													line.setLineNetAmt();
													line.save();
													//line.set_ValueOfColumn("TEMPLINE_ID", rs.getInt("M_INVENTORYLINETEMP_ID"));
													
													lines.setC_Order_ID(order.getC_Order_ID());
													lines.setProcessed(true);
													lines.save();
													contador++;
												}
											if (contador==17)
											{
												order.setDocAction("CO");
												order.processIt ("CO");
												order.save();
												docok++;
												contador=1;
												
												order=null;
											}
											//aux++;
											} //while
											order.setDocAction("CO");
											order.processIt ("CO");
											order.save();
											ob2c.setProcessed(true);
											ob2c.save();
											contador=1;
											docok++;
											order=null;
											sql = new StringBuffer ("UPDATE I_OrderB2C "
													  + "SET I_IsImported='Y' , processed='Y' "
													  + "WHERE Documentno=" + ob2c.getPOReference());
													  
												no = DB.executeUpdate(sql.toString(), get_TrxName());
												if (no != 0)
													log.warning ("No importado=" + no);

											

												commitEx();
											rslines.close();
											pstmtlines.close();
										}
										catch(Exception e)
										{
											
											log.log(Level.SEVERE, e.getMessage(), e);
										}	
											
										}/*else
											errorsl=10;*/
									
									rsvlok.close();
									pstmtvlok.close();
								}
								catch(Exception e)
								{
									
									log.log(Level.SEVERE, e.getMessage(), e);
								}
							}/*else
							{
								errorsl=9;
							}*/
						}
						
						rsvl.close();
						pstmtvl.close();
					}
					catch(Exception e)
					{
						
						log.log(Level.SEVERE, e.getMessage(), e);
					}
					
				}
				else
				{
					
					/*if (errorbp!=0)
					{
						ob2c.set_CustomColumn("ErrorMsg", menj1 );
						docnotok++;
					}
					else if (errorbpl!=0)
					{
							ob2c.set_CustomColumn("ErrorMsg", menj2);
							docnotok++;
					}
					else if (erroruser!=0)
					{
						ob2c.set_CustomColumn("ErrorMsg", menj6);
						docnotok++;
					}*/
					ob2c.save();
				}
//				if (errorsl==9)
//				;
				
				rs.close();
				pstmt.close();
			}
			catch(Exception e)
			{
				
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}//for que recorre las ordenes de compra subidas
		
		
		//	Go through Order Records w/o
	

		//	Set Error to indicator to not imported
		
		
		//

		return "Proceso terminado - Ordenes Creadad:" + docok + " - Ordenes con Error: "+ docnotok  ;
	}	//	doIt
	//Compa�ia
	public int actualizaClient ()
	{ 
		/*
		 * metodo que actualiza la compa�ia
		 * 
		 * */
		sql = new StringBuffer ("UPDATE I_OrderB2C "
				  + "SET AD_Client_ID = COALESCE (AD_Client_ID,").append (m_AD_Client_ID).append ("),"
				  + " AD_Org_ID = COALESCE (AD_Org_ID,").append (m_AD_Org_ID).append ("),"
				  + " IsActive = COALESCE (IsActive, 'Y'),"
				  + " Created = COALESCE (Created, SysDate),"
				  + " CreatedBy = COALESCE (CreatedBy, 0),"
				  + " Updated = COALESCE (Updated, SysDate),"
				  + " UpdatedBy = COALESCE (UpdatedBy, 0),"
				  + " I_ErrorMsg = ' ',"
				  + " I_IsImported = 'N' "
				  + "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
			no = DB.executeUpdate(sql.toString(), get_TrxName());
		
		return no;
	}
	//Socio de Negocio
	public int actualizaBP ()
	{ 
		/*
		 * metodo que actualiza el cliente
		 * 
		 * */
		sql = new StringBuffer ("UPDATE I_OrderB2C o "
			  + "SET C_BPartner_ID=(SELECT MAX(C_BPartner_ID) FROM C_BPartner bp"
			  + " WHERE o.BPartnerValue=bp.Value AND o.AD_Client_ID=bp.AD_Client_ID) "
			  + "WHERE C_BPartner_ID IS NULL AND BPartnerValue IS NOT NULL"
			  + " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		
		return no;
	}
	//direccion
	public int actualizaBPL ()
	{ 
		/*
		 * metodo que actualiza la direccion
		 * 
		 * */
		sql = new StringBuffer ("UPDATE I_OrderB2C o "
				  + "SET (BillTo_ID,C_BPartner_Location_ID)=(SELECT max(C_BPartner_Location_ID)C_BPartner_Location_ID,max(C_BPartner_Location_ID)C_BPartner_Location_ID"
				  + " FROM C_BPartner_Location bpl INNER JOIN C_Location l ON (bpl.C_Location_ID=l.C_Location_ID)"
				  + " WHERE o.C_BPartner_ID=bpl.C_BPartner_ID AND bpl.AD_Client_ID=o.AD_Client_ID"
				  + " AND ( trim(upper(o.Address1))=trim(upper(l.Address1) )  or  trim(upper(o.Address1))=trim(upper(bpl.Name) )  ) )"
				 // + " AND DUMP(o.City)=DUMP(l.City) AND DUMP(o.Postal)=DUMP(l.Postal)"
				//  + " AND o.C_Region_ID=l.C_Region_ID AND o.C_Country_ID=l.C_Country_ID) "
				  + "WHERE C_BPartner_ID IS NOT NULL AND C_BPartner_Location_ID IS NULL"
				  + " AND I_IsImported='N'").append (clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
		
		return no;
	}
	//Producto
	public int actualizaProduct ()
	{ 
		/*
		 * metodo que actualiza el producto
		 * 
		 * */
		sql = new StringBuffer ("UPDATE I_OrderB2C o "
				  + "SET M_Product_ID=(SELECT MAX(M_Product_ID) FROM M_Product p"
				  + " WHERE o.ProductValue=p.Value AND o.AD_Client_ID=p.AD_Client_ID) "
				  + "WHERE M_Product_ID IS NULL AND ProductValue IS NOT NULL"
				  + " AND I_IsImported<>'Y'").append (clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}
	
	//Vendedor
	public int actualizaSalesRep ()
	{ 
		/*
		 * metodo que actualiza el vendedor
		 * 
		 * */
		sql = new StringBuffer ("UPDATE I_OrderB2C o "
				  + "SET o.SalesRep_ID=(SELECT MAX(u.AD_User_ID) FROM ad_user u"
				  + " WHERE trim(lower(o.ChargeName))=trim(lower(u.name)) AND o.AD_Client_ID=u.AD_Client_ID) "
				  + "WHERE ChargeName IS NOT NULL "
				  + " AND I_IsImported<>'Y'").append (clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}
	public int beforeImport (String document, String bpvalue)
	{ 
		/*
		 * metodo que actualiza las ordenes que ya existan (poreference y rut igual)
		 * 
		 * */
		sql = new StringBuffer ("UPDATE I_OrderB2C  "
				  + "SET I_IsImported='Y' , I_ERRORMSG = 'Orden Importada previamente' "
				  + "WHERE documentno= " + document
				  + " and bpartnervalue="+ bpvalue);
				  
			no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}
}	//	ImportOrder



