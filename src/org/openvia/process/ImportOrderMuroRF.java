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
//import org.compiere.model.MProductPrice;
import org.compiere.model.X_C_Order;
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
public class ImportOrderMuroRF extends SvrProcess
{
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 1000000;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 1000000;
	private int				m_C_BPartner_ID = 1001237;
	/**	Delete old Imported				*/
	//private boolean			m_deleteOldImported = false;
	private StringBuffer sql = null;
	private int no = 0;
	private String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;
	/**	Document Action					*/
	//private String			m_docAction = MOrder.DOCACTION_Prepare;

	//variables de ccorreo
		final String miCorreo = "soporte@comercialwindsor.cl";
	    final String miContraseña = "Cw9121100";
	    final String servidorSMTP = "smtp.gmail.com";
	    final String puertoEnvio = "465";
	    String mailReceptor = null;
	    String asunto = null;
	    String cuerpo = null;
	/** Effective						*/
	private Timestamp		m_DateValue = null;

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
			else if (name.equals("C_BPartner_ID"))
				m_C_BPartner_ID=1001237;//	m_deleteOldImported = "Y".equals(para[i].getParameter());
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
		
		
		
		String menj1 = null, menj2 = null, menj3 = null,menj4 = null, menj5=null, menj6=null, menj7 = null;
		//	****	Prepare	****

		
		//	Set Client, Org, IsActive, Created/Updated
		log.info ("Reset=" + actualizaClient ());
        //BP Value
		log.fine("Set BP from Value=" + actualizaBP());
		// BP Location
//		log.fine("Found Location=" + actualizaBPL());
		//	Product
		log.fine("Set Product from Value=" + actualizaProduct ());
		//Venedero
		log.fine("Set SalesRep_ID=" +actualizaSalesRep ());
		commitEx();
		log.fine("tets"+m_C_BPartner_ID);
	 ArrayList<String> ordenescompra = new ArrayList<String>(); 
	// ArrayList<String> docerror = new ArrayList<String>();
	// ArrayList<String> recorredocerror = new ArrayList<String>();
		//	-- New Orders -----------------------------------------------------
	String slqoc  = "Select Documentno, BPartnerValue from I_OrderB2C where i_isImported<>'Y' and C_BPartner_ID=1001237 "+clientCheck +  " Group by DocumentNo, BPartnerValue";
	//obtener ordenes para importar
	try
	{
		PreparedStatement pstmt = DB.prepareStatement (slqoc, get_TrxName());
		ResultSet rs = pstmt.executeQuery ();
		while (rs.next())
		{
			String sqlcreados = "Select count(o.DocumentNO) as cuenta from I_orderB2C o  where o.i_isImported<>'Y' "+
								" and o.bpartnervalue='76281810' and o.documentno in (select b2.poreference from c_orderb2c b2 "+
								" where o.documentno=b2.poreference and b2.bpartnerValue=o.bpartnervalue) "
								+" and o.documentno='"+rs.getString("DocumentNo")+"'";
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
			String recorre = "Select documentno, COALESCE(c_bpartner_ID,0) C_BPartner_ID,  COALESCE(1011338,0)c_bpartner_location_ID,"+
							"	 COALESCE(M_product_ID, 0) M_product_ID,"+
							" replace(description,'/','-') description, name as tipogt, ContactName formacgt,"+
							" trim(upper(Address2)) as subtienda, ChargeName as vendedor, Coalesce (SalesRep_ID,0) SalesRep_ID , "+
							" LineDescription as Generico, COALESCE(QtyOrdered,0) as Cantidad, COALESCE(PriceActual,0) Precio, Created, ProductValue, BPartnerValue, Address1, DOCTYPENAME, POReference,"+
							" Address1, DOCTYPENAME, ov_org_id, C_ORDERMURO_id, NOMBRESHOPIFY, DIRECCIONSHOPIFY  "+
							" from  I_OrderB2C where bpartnervalue='76281810' and documentNo='"+ ordenescompra.get(i)+"'";
			//String direccion, rut, vendedor = "";
			int errorp = 0;
			int errorbp= 0;
			int errorbpl=0;
			int erroruser=0;
			int errorst = 0;
			int errorsl= 0;
			
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
					
					if(rs.getInt("Cantidad")>=1)
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
					
					}
					
					if (ob2c==null)
					{
						//cabecera
						 ob2c = new X_C_OrderB2C ( getCtx() ,0,get_TrxName() );
						 if (errorbp==0)
								ob2c.setC_BPartner_ID(rs.getInt("C_BPartner_ID"));
						 if(errorbpl==0)
							 ob2c.setC_BPartner_Location_ID(rs.getInt("C_BPartner_Location_ID"));
						 String description = rs.getString("description");
						 if(description !=null)
						 {
							
							 ob2c.setDescription(description);
							  
						 }
						 
						 ob2c.setPOReference(rs.getString("POReference"));
						 ob2c.setDateAcct(rs.getTimestamp("Created"));
						 ob2c.set_CustomColumn("BPartnerValue", rs.getString("BPartnerValue"));
						 ob2c.set_CustomColumn("Address1", rs.getString("Address1"));
						// String tgt=rs.getString ("tipogt");
						 //log.info("GT:"+ tgt);
						 //medio de compra y forma de compra para definir bodega
				//		 String mc ="";
					
						
							 ob2c.setVentaEnVerde(false);
							 ob2c.setM_Warehouse_ID(1000001);
						
							 ob2c.set_CustomColumn("C_OrderMuro_ID", rs.getInt("C_ORDERMURO_id"));
								ob2c.set_CustomColumn("DocumentoMuro", rs.getString("DocumentNo"));
								ob2c.set_CustomColumn("VentaEnVerde", "N");
								//ob2c.set_CustomColumn("OBSERVACIONES", "Cliente: " + MBPartner.get(getCtx(), 1001237).getName() + " Telefono: " + telefono + " Email: " + email);
								//ob2c.set_CustomColumn("unidadesmuro", rs.getInt("cantidad"));
//								ob2c.set_CustomColumn("numproductos", numproductosm);
								ob2c.set_CustomColumn("NOMBRESHOPIFY", rs.getString("NOMBRESHOPIFY"));
								ob2c.set_CustomColumn("DIRECCIONSHOPIFY", rs.getString("DIRECCIONSHOPIFY")); 
					
					
							if(rs.getInt("SalesRep_ID")==0)
							{	erroruser=6;
								menj6="Venedor no encontrado:"+ rs.getString("vendedor");
							}
							else
								ob2c.setSalesRep_ID(rs.getInt("SalesRep_ID"));
						//de aqui saque un cierre parentesisis
						ob2c.setProcessed(false);
						ob2c.setM_PriceList_ID(1000040);
						
				
						ob2c.save();
				
					}//if crea cabecera
						
					BigDecimal sob = new BigDecimal (0);
					
					if (ob2c!=null)
					{
						int salto=0;
						//salto = 0 no hay reserva ecoomerce
						//salto =1 Reserva Ecommerce cubre todo
						//salto = 2 Reserva Ecommerce no cubre todo y debe buscar en reserva fisica
						//salto = 3 reserva fisica cubre lo que falta
						//salto =4 reserva fisica no cubre todo lo que falta debe pasar a disponible
						//salto = 5 disponible cubre todo
						//salto = 6 disponible no alcanza 
						X_C_OrderB2CLine obl = new X_C_OrderB2CLine  (getCtx() ,0,get_TrxName());
						obl.setC_OrderB2C_ID(ob2c.getC_OrderB2C_ID());
						obl.setLine(lineas*10);
						obl.setM_Product_ID(rs.getInt("M_Product_ID"));
						obl.setProductValue(rs.getString("ProductValue"));
						obl.save();
						BigDecimal cant = new BigDecimal (rs.getInt("Cantidad"));
					//	log.fine("Precio=" + Integer.parseInt(DB.getSQLValueString(null, "Select coalesce((PriceList),0) from m_productprice where isactive='Y'  and M_PriceList_Version_ID=1000039 "+
					//			" and m_product_id="+rs.getInt("M_Product_ID")) ));
					
						int precio= Integer.parseInt(DB.getSQLValueString(null, "Select round(coalesce((PriceList),0),0) from m_productprice where isactive='Y'  and M_PriceList_Version_ID=1000039 "+
								" and m_product_id="+rs.getInt("M_Product_ID")) ); 
						
						BigDecimal priceProduct = new BigDecimal(precio); //MProductPrice.get(getCtx(), 1000039, rs.getInt("M_Product_ID"), get_TrxName()).getPriceList();
						
						obl.setPriceEntered(priceProduct.intValue());
						
						
						if(errorp==0)
						{
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
										obl.save();
										//Reserva e-commerce
									String sqlqr =	"select count(1) encontrado "+
										" from m_requisition r " +
										" where r.docstatus='CO' and r.c_doctype_ID=1000570 "+
										" and r.c_bpartner_ID="+rs.getInt("C_BPartner_ID")
										+ "and "+ 
										" r.M_RequisitionRef_ID in " +
										" (select r2.m_requisition_ID "+
										" from m_requisition r2 "+
										" inner join m_requisitionline rl on (r2.m_requisition_ID=rl.m_Requisition_ID) "+ 
										" where r2.docstatus='CO' and rl.m_product_ID="+rs.getInt("M_Product_ID")+
										 " and (rl.qtyreserved)>0 and r2.c_doctype_ID=1000569 and rl.liberada='N' )";
								
									
									try
									{
										PreparedStatement pstmtre = DB.prepareStatement (sqlqr, get_TrxName());
										ResultSet rsre = pstmtre.executeQuery ();
									
										
										if(rsre.next())
										{
											//si existe rf Ecommerce reabaja de esta,
											if(rsre.getInt("encontrado")>0)
											{//si existe rf Ecommerce reabaja de esta,
												
												
												String sqlqrq =	" select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "+
														" from m_Requisitionline rl "+
														" inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "+
														" where r.docstatus='CO'  "+
														" and r.c_doctype_id=1000569 "+
														" and rl.liberada='N' "+
														" and rl.m_product_ID ="+rs.getInt("M_Product_ID") +
														" and exists  "+
																" (select * "+ 
																" from m_requisition r2 "+
																" where R2.M_REQUISITIONREF_ID=r.m_requisition_ID "+
																"  and r2.c_doctype_ID=1000570 "+
																" and r2.docstatus='CO' "+
																" and r2.c_bpartner_ID="+rs.getInt("C_BPartner_ID")+ " ) ";
												try
												{
													PreparedStatement pstmtreq = DB.prepareStatement (sqlqrq, get_TrxName());
													ResultSet rsreq = pstmtreq.executeQuery ();
													if(rsreq.next())
													{
														if (rs.getInt("Cantidad")<=rsreq.getInt("qtyreserved"))
														{
															obl.setQtyEntered(cant);
															BigDecimal neto = new BigDecimal (rs.getInt("Cantidad") *priceProduct.intValue() );
															obl.setLineNetAmt(neto);
															obl.setPASARAOV(true);
															obl.set_CustomColumn("M_RequisitionLine_ID",rsreq.getInt("M_RequisitionLine_ID") );
															cant = new BigDecimal (0);
															obl.save();
															salto=1;
														}
														if (rs.getInt("Cantidad")>rsreq.getInt("qtyreserved"))
														{ //reserva eccomerce no cubre todo
															BigDecimal aux = new BigDecimal (rsreq.getInt("qtyreserved"));
															obl.setQtyEntered(aux);
															cant = new BigDecimal(cant.intValue() - aux.intValue());
															BigDecimal neto = new BigDecimal (rsreq.getInt("qtyreserved") *priceProduct.intValue() );
															obl.set_CustomColumn("M_RequisitionLine_ID",rsreq.getInt("M_RequisitionLine_ID") );
															obl.setLineNetAmt(neto);
															obl.setPASARAOV(true);
															
															obl.save();
															salto=2;
														}
													
														
															
															
													}
												}
												catch(Exception e)
												{
													
													log.log(Level.SEVERE, e.getMessage(), e);
												}
											}//si existe rf Ecommerce reabaja de esta 
											
											// NO EXISTE RESERVA FISICA ECcOMERCE
												
												
												
												
												
												
											
											
												
										}
									
									}//try reserva fisica
									catch(Exception e)
									{
										
										log.log(Level.SEVERE, e.getMessage(), e);
									}
										
									
									if(salto==0 || salto==2)
									{//salto 0 o 2
										//reserva ecommerce no cubre todo sacar de la fisica
										//		
												String sqlrfn=" select count(1) encontrado "+ 
												 " from m_requisition r  "+
														" inner join m_requisitionline rl on (r.m_requisition_ID=rl.m_Requisition_ID) "+ 
														" where r.docstatus='CO' and r.c_doctype_ID=1000111 "+
														" and r.c_bpartner_ID="+rs.getInt("C_BPartner_ID")+
														" and (r.c_bpartner_location_ID="+rs.getInt("c_bpartner_location_ID")
														+ "or R.OVERWRITEREQUISITION='Y') "+
														" and  "+
														" rl.m_product_ID="+rs.getInt("m_product_ID")+
														" and (rl.qtyreserved)>0  "+
														" and  rl.LIBERADA='N' " ;
												
											
												
													try
													{
														PreparedStatement pstmtrfn = DB.prepareStatement (sqlrfn, get_TrxName());
														ResultSet rsrfn = pstmtrfn.executeQuery ();
														if(rsrfn.next())
														{
															//si existe rfisica,
															if(rsrfn.getInt("encontrado")>0)
															{
																if(salto==2)
																{
																	lineas++;
																	obl = new X_C_OrderB2CLine  (getCtx() ,0,get_TrxName());
																}
																
																obl.setC_OrderB2C_ID(ob2c.getC_OrderB2C_ID());
																obl.setLine(lineas*10);
																//BigDecimal cant = new BigDecimal (rs.getInt("Cantidad"));
																obl.setPriceEntered(priceProduct.intValue());
															
																obl.setM_Product_ID(rs.getInt("M_Product_ID"));
																String sqlrn =	" select sum (rl.qtyreserved)qtyreserved, max(rl.m_requisitionline_ID)m_requisitionline_ID "+
																		" from m_Requisitionline rl "+
																		" inner join m_requisition r on (rl.m_Requisition_ID=r.m_requisition_ID) "+
																		" where r.docstatus='CO'  "+
																		" and r.c_doctype_id=1000111 "+
																		" and rl.m_product_ID ="+rs.getInt("M_Product_ID") +
																		" and r.c_bpartner_ID="+rs.getInt("C_BPartner_ID") +
																		" and (r.c_bpartner_location_ID="+rs.getInt("c_bpartner_location_ID") +
																		 " or R.OVERWRITEREQUISITION='Y') "+
																		" and (rl.qtyreserved)>0"+
																		 " and  rl.LIBERADA='N' " ;
																try
																{
																	PreparedStatement pstmtrn = DB.prepareStatement (sqlrn, get_TrxName());
																	ResultSet rsrn = pstmtrn.executeQuery ();
																	if(rsrn.next())
																	{//reserva fisica normal rebajar
																		if (rs.getInt("Cantidad")<=rsrn.getInt("qtyreserved"))
																		{//cantiadad es cubierta por rf normal
																			obl.setQtyEntered(cant);
																			BigDecimal neto = new BigDecimal (cant.intValue() *priceProduct.intValue() );
																			obl.setLineNetAmt(neto);
																			obl.setPASARAOV(true);
																			obl.set_CustomColumn("M_RequisitionLine_ID",rsrn.getInt("M_RequisitionLine_ID") );
																			cant = new BigDecimal (0);
																			obl.save();
																			salto=3;
																		}
																		if (rs.getInt("Cantidad")>rsrn.getInt("qtyreserved"))
																		{//cantiadad NO es cubierta por rf normal
																			
																			BigDecimal aux = new BigDecimal (rsrn.getInt("qtyreserved"));
																			obl.setQtyEntered(aux);
																			cant = new BigDecimal(cant.intValue() - aux.intValue());
																			BigDecimal neto = new BigDecimal (rsrn.getInt("qtyreserved") *priceProduct.intValue() );
																			obl.set_CustomColumn("M_RequisitionLine_ID",rsrn.getInt("M_RequisitionLine_ID") );
																			obl.setLineNetAmt(neto);
																			obl.setPASARAOV(true);
																			
																			obl.save();
																			salto=4;
																		}
																	
																			
																		
																		
																	}
																	
																}
																catch(Exception e)
																{
																	
																	log.log(Level.SEVERE, e.getMessage(), e);
																}
																	
																
															}//existe reserva fisica
															/*else { //NO existe reserva fisica eliminado para probar
																			obl = new X_C_OrderB2CLine  (getCtx() ,0,get_TrxName());
																			obl.setC_OrderB2C_ID(ob2c.getC_OrderB2C_ID());
																			obl.setLine(lineas*10);
																			//BigDecimal cant = new BigDecimal (rs.getInt("Cantidad"));
																			obl.setPriceEntered(rs.getInt("Precio"));
																			if(!rs.getString("Generico").isEmpty() || rs.getString("Generico")!=null)
																				obl.setCODIGOGENERICO(rs.getString("Generico"));
																				obl.setProductValue(rs.getString("ProductValue"));
																				
																			String sqlps = "Select qtyavailableofb(p.m_product_ID, 1000001  ) + qtyavailableofb(p.m_product_ID, 1000010  ) as Disponible, p.ProductType,  " +
																					"qtyavailableofb(p.m_product_ID,1000024)+qtyavailableofb(p.m_product_ID,1000025) OtroDisponible"+
																				   " from M_product p where  p.m_product_ID="+ rs.getInt("M_Product_ID");
																			try
																			{
																				
		
																				PreparedStatement pstmtps = DB.prepareStatement (sqlps, get_TrxName());
																				ResultSet rsps = pstmtps.executeQuery ();
																				
																				if(rsps.next())
																				{
																				//	System.out.print(rsps.getInt("Disponible"));
																				
																				if(rsps.getString("ProductType").equals("I"))
																				{
																				if (cant.intValue()<=rsps.getInt("Disponible"))
																				{
																		//			System.out.print("Cantidad:"+rs.getInt("Cantidad") + " Disponible"+rsps.getInt("Disponible"));
																					//obl.setProductValue(rs.getString("ProductValue"));
																					obl.setQtyEntered(cant);
																					BigDecimal neto = new BigDecimal (cant.intValue() *rs.getInt("Precio") );
																					obl.setLineNetAmt(neto);
																					obl.setPASARAOV(true);
																					obl.save();
																					salto=5;
																				}
																				else
																				{
																					//System.out.print("Cantidad:"+rs.getInt("Cantidad") + " Disponible"+rsps.getInt("Disponible"));
																					errorp=5;
																					salto=6;
																					obl.setPASARAOV(false);
																					 sob = new BigDecimal (rsps.getInt("OtroDisponible"));
																					 obl.setErrorMsg("No hay stock en la bodega actual, verificar en otras bodegas");
																					obl.setStockBodegas(sob);
																				
																				}
																				}
																				else //no es alamcenable
																					obl.setPASARAOV(true);
																				}
																				
																				rsps.close();
																				pstmtps.close();
																			}
																			catch(Exception e)
																			{
																				
																				log.log(Level.SEVERE, e.getMessage(), e);
																			}
																	}*/ //else no existe reserva fisica
																}//try antes del if de reserva fisica. 
															
															}
															catch(Exception e)
															{
																
																log.log(Level.SEVERE, e.getMessage(), e);
															}
													
													
											
												
												
									}//salto 0 o 2
									
										if(salto==0 || salto==4 || salto==2)
										{ //rf normal NO cubre todo
											if (salto==4 || salto==2)
											{
												obl = new X_C_OrderB2CLine  (getCtx() ,0,get_TrxName());
												lineas++;
												obl.setLine(lineas*10);
											}
											
											obl.setC_OrderB2C_ID(ob2c.getC_OrderB2C_ID());
											obl.setM_Product_ID(rs.getInt("M_Product_ID"));
											
											//BigDecimal cant = new BigDecimal (rs.getInt("Cantidad"));
											obl.setPriceEntered(priceProduct.intValue());
										
											String sqlps = "Select qtyavailableofb(p.m_product_ID, 1000001  ) + qtyavailableofb(p.m_product_ID, 1000010  ) as Disponible, p.ProductType,  " +
													"qtyavailableofb(p.m_product_ID,1000024)+qtyavailableofb(p.m_product_ID,1000025) OtroDisponible"+
												   " from M_product p where  p.m_product_ID="+ rs.getInt("M_Product_ID");
									try
									{
										

										PreparedStatement pstmtps = DB.prepareStatement (sqlps, get_TrxName());
										ResultSet rsps = pstmtps.executeQuery ();
										
										if(rsps.next())
										{
										//	System.out.print(rsps.getInt("Disponible"));
										
										if(rsps.getString("ProductType").equals("I"))
										{
										if (cant.intValue()<=rsps.getInt("Disponible"))
										{
								//			System.out.print("Cantidad:"+rs.getInt("Cantidad") + " Disponible"+rsps.getInt("Disponible"));
											//obl.setProductValue(rs.getString("ProductValue"));
											obl.setQtyEntered(cant);
											BigDecimal neto = new BigDecimal (cant.intValue() *priceProduct.intValue() );
											obl.setLineNetAmt(neto);
											obl.setPASARAOV(true);
											obl.save();
											salto=5;
										}
										else
										{
											//System.out.print("Cantidad:"+rs.getInt("Cantidad") + " Disponible"+rsps.getInt("Disponible"));
											errorp=5;
											salto=6;
											obl.setPASARAOV(false);
											 sob = new BigDecimal (rsps.getInt("OtroDisponible"));
											 obl.setErrorMsg("No hay stock en la bodega actual, verificar en otras bodegas");
											obl.setStockBodegas(sob);
										
										}
										}
										else //no es alamcenable
											obl.setPASARAOV(true);
										}
										
										rsps.close();
										pstmtps.close();
									}
									catch(Exception e)
									{
										
										log.log(Level.SEVERE, e.getMessage(), e);
									}
										} //if salto 4
										
									
									}//producto
								}
								else
								{
									errorp=7;
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
						}
						
						
						
						
						
						if(errorp>0)
						{
							if(errorp==3)
								obl.setErrorMsg(menj3);
							else if(errorp==4)
								obl.setErrorMsg(menj4);
							else if(errorp==5)
							{
								menj5="Error: Producto sin Stock, verificar en otras bodegas";
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
							else if(errorp==7)
								menj7="Error: Codigo Windsor ingresadp no Existe.";
								obl.setErrorMsg(menj7);
							
							// si no encuentra el producto
								
								obl.setQtyEntered(cant);
								//System.out.print(rs.getInt("Precio"));
								
								BigDecimal neto = new BigDecimal (rs.getInt("Cantidad") *rs.getInt("Precio") );
								obl.setLineNetAmt(neto);
						}
						if (errorp==5)
						obl.set_CustomColumn("ErrorMsg", "No hay stock en la bodega actual, verificar en otras bodegas");	

						if(errorst==8)
							obl.set_CustomColumn("ErrorMsg", "Subtienda no existe:"+rs.getString("subtienda"));
						
					
					obl.save();
					}
					lineas++;
				
				
				
				} //while que recorre los productos de las ordenes
				
				//Crear nota de venta sin errores
				if (errorp + errorbp +  errorbpl +  erroruser + errorst==0) {
					//sin errores controlados
					MOrder order = null;
					int contador = 1;
					int contadordoc=0;
					boolean completar = true;
					int documentNo = 1;
					
					// Se guardan unidades y productos Muro
					String sqlNumProductos = "SELECT COUNT(DISTINCT(M_Product_ID)) FROM C_OrderB2CLine WHERE C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
					String sqlUnidadesMuro = "SELECT SUM(QtyEntered) FROM C_OrderB2CLine WHERE C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
					
					//validar que todas las lineas esten okey y que hau lineas
					String sqlvl = "Select count(*) cuenta from C_OrderB2CLine where C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
					try {
						PreparedStatement pstmtvl = DB.prepareStatement (sqlvl, get_TrxName());
						ResultSet rsvl = pstmtvl.executeQuery ();
						if(rsvl.next()) {
							if(rsvl.getInt("cuenta")>0) {
								//valida que todas las lineas esten OK
								String sqlvlok= "Select count(*) cuenta from C_OrderB2CLine where PASARAOV<>'Y' and C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
								try {
									PreparedStatement pstmtvlok = DB.prepareStatement (sqlvlok, get_TrxName());
									ResultSet rsvlok = pstmtvlok.executeQuery();
									
									if(rsvlok.next()) {
										if(rsvlok.getInt("cuenta")==0) {
											String sqllines = "Select * from C_OrderB2CLine where  C_OrderB2C_ID="+ob2c.getC_OrderB2C_ID();
										//	int aux=1;
											try {
												PreparedStatement pstmtlines = DB.prepareStatement (sqllines, get_TrxName());
												ResultSet rslines = pstmtlines.executeQuery ();
												while (rslines.next()) {
													if(order==null) {
														//noInsert ++;
														contadordoc=contadordoc+1;
														order = new MOrder (getCtx() ,0,get_TrxName() );
														MBPartner bp = new MBPartner (getCtx(), ob2c.getC_BPartner_ID(), get_TrxName());
														order.setClientOrg (ob2c.getAD_Client_ID(),ob2c.getAD_Org_ID());
														if (ob2c.getC_BPartner_Location_ID()==1011338)
														order.set_CustomColumn("MedioCompra", "Internet");
														order.setC_DocTypeTarget_ID(1000030);
														order.setIsSOTrx(true);
														order.setDeliveryRule("O");
														order.setC_BPartner_ID(ob2c.getC_BPartner_ID());
														order.setC_BPartner_Location_ID(ob2c.getC_BPartner_Location_ID());
														order.setPOReference(ob2c.getPOReference());
														order.setAD_User_ID(ob2c.getSalesRep_ID());
														//	Bill Partner
														order.setBill_BPartner_ID(ob2c.getC_BPartner_ID());
														order.setBill_Location_ID(ob2c.getC_BPartner_Location_ID());
														if (ob2c.getDescription() != null)
															order.setDescription(ob2c.getDescription());
														order.setC_PaymentTerm_ID(bp.getC_PaymentTerm_ID());
														order.setM_PriceList_ID(ob2c.getM_PriceList_ID());
														order.setM_Warehouse_ID(ob2c.getM_Warehouse_ID());
														order.setSalesRep_ID(ob2c.getSalesRep_ID());
														order.setDateOrdered(ob2c.getDateAcct());
														order.setDateAcct( ob2c.getDateAcct());
														order.setInvoiceRule("D");
														//Faltan firmas horas de firma y quien firmo forma de compra medio compra
														order.set_CustomColumn("FIRMA2", "Y");
														order.set_CustomColumn("FIRMACOM", ob2c.getDateAcct());
														order.set_CustomColumn("USERFIRMCOM", 1003655); //vsandoval
														order.set_CustomColumn("FormaCompra", ob2c.get_Value("FormaCompra"));
														order.set_CustomColumn("MedioCompra", "Internet");
														order.set_CustomColumn("VentaInvierno", "N");
														if (documentNo > 1)
															order.setDocumentNo( ob2c.get_ValueAsString ("DocumentoMuro")+"-"+documentNo );
														else
															order.setDocumentNo( ob2c.get_ValueAsString ("DocumentoMuro"));
														order.set_CustomColumn("C_ORDERMURO_id", ob2c.get_ValueAsInt("C_OrderMuro_ID"));
														order.set_CustomColumn("NOMBRESHOPIFY", ob2c.get_Value("NOMBRESHOPIFY"));
														order.set_CustomColumn("DIRECCIONSHOPIFY", ob2c.get_Value("DIRECCIONSHOPIFY"));
														// Guardar numproductos, unidadesmuro
														order.set_CustomColumn("numproductos", DB.getSQLValue(get_TrxName(), sqlNumProductos));
														order.set_CustomColumn("unidadesmuro", DB.getSQLValue(get_TrxName(), sqlUnidadesMuro));
														order.save();
													}
													if (contador<=25) {
														MOrderLine line = new MOrderLine(order);
														X_C_OrderB2CLine lines= new X_C_OrderB2CLine (getCtx() , rslines.getInt("C_OrderB2CLine_ID"),get_TrxName());
														line.setM_Product_ID(lines.getM_Product_ID());
														line.setPriceEntered( new BigDecimal  (lines.getPriceEntered()));
														line.setPriceActual(new BigDecimal  (lines.getPriceEntered()));
														line.setPriceList(new BigDecimal  (lines.getPriceEntered()));
														line.setQtyEntered( (lines.getQtyEntered()));
														line.setLine(contador*10);
														line.setPrice(new BigDecimal  (lines.getPriceEntered()));
														line.setQty(lines.getQtyEntered());
														line.set_CustomColumn("Demand", lines.getQtyEntered());
														line.setC_Tax_ID(1000000);
														BigDecimal df = new BigDecimal (0);
														line.set_CustomColumn("Discount", df);
														line.set_CustomColumn("Discount2", df);
														line.set_CustomColumn("Discount3", df);
														line.set_CustomColumn("Discount4", df);
														line.set_CustomColumn("Discount5", df);
														line.set_CustomColumn("NotPrint", "N");
														line.setLineNetAmt();
														line.set_CustomColumn("M_RequisitionLine_ID",(Integer) lines.get_Value("M_RequisitionLine_ID"));
														if(!line.save()) {
															completar = false;
															line.setQty(BigDecimal.ZERO);
															line.set_CustomColumn("NotPrint", "Y");
															line.save();
														}
														lines.setC_Order_ID(order.getC_Order_ID());
														lines.setProcessed(true);
														lines.save();
														contador++;
													}
													if (contador==26) {
														if(completar) {
															String sqlNumProductosOrden = "SELECT COUNT(DISTINCT(M_Product_ID)) FROM C_OrderLine WHERE C_Order_ID="+order.getC_Order_ID();
															String sqlUnidadesMuroOrden = "SELECT SUM(QtyEntered) FROM C_OrderLine WHERE C_Order_ID="+order.getC_Order_ID();
															if (new BigDecimal(DB.getSQLValue(get_TrxName(), sqlNumProductosOrden)).compareTo(new BigDecimal(DB.getSQLValue(get_TrxName(), sqlNumProductos)))==0 || new BigDecimal(DB.getSQLValue(get_TrxName(), sqlUnidadesMuroOrden)).compareTo(new BigDecimal(DB.getSQLValue(get_TrxName(), sqlUnidadesMuro)))==0) {
																order.processIt(X_C_Order.DOCACTION_Complete);
																order.save();
																order.setDocAction("CO");
																order.processIt ("CO");
																order.save();
															} else {
																order.setDescription("No se pudo completar porque la orden se ha generado con diferencia en cantidades");
																order.saveEx();
															}
															docok++;
														}
														contador=1;
														order=null;
														documentNo++;
													}
												} //while
												order.set_CustomColumn("FIRMA2", "Y");
												order.set_CustomColumn("FIRMA3", "Y");
												if(completar) {
													// Comparar con Orden CW
													String sqlNumProductosOrden = "SELECT COUNT(DISTINCT(M_Product_ID)) FROM C_OrderLine WHERE C_Order_ID="+order.getC_Order_ID();
													String sqlUnidadesMuroOrden = "SELECT SUM(QtyEntered) FROM C_OrderLine WHERE C_Order_ID="+order.getC_Order_ID();
													if (new BigDecimal(DB.getSQLValue(get_TrxName(), sqlNumProductosOrden)).compareTo(new BigDecimal(DB.getSQLValue(get_TrxName(), sqlNumProductos)))==0 || new BigDecimal(DB.getSQLValue(get_TrxName(), sqlUnidadesMuroOrden)).compareTo(new BigDecimal(DB.getSQLValue(get_TrxName(), sqlUnidadesMuro)))==0) {
														order.processIt(X_C_Order.DOCACTION_Complete);
														order.save();
														order.setDocAction("CO");
														order.processIt ("CO");
													} else {
														order.setDescription("No se pudo completar porque la orden se ha generado con diferencia en cantidades");
														order.saveEx();
													}
												}
												order.save();
											
												ob2c.setProcessed(true);
												ob2c.save();
												contador=1;
												docok++;
												order=null;
												documentNo++;
											
												sql = new StringBuffer ("UPDATE I_OrderB2C "
													  + "SET I_IsImported='Y' , processed='Y' "
													  + "WHERE bpartnervalue='76281810' and POReference='" + ob2c.getPOReference()+"'");
													 
												no = DB.executeUpdate(sql.toString(), get_TrxName());
												if (no != 0)
													log.warning ("No importado=" + no);

											

												commitEx();
												rslines.close();
												pstmtlines.close();
											} catch(Exception e) {
												log.log(Level.SEVERE, e.getMessage(), e);
											}	
										} else {
											errorsl=10;
										}
									}
									rsvlok.close();
									pstmtvlok.close();
								} catch(Exception e) {
									log.log(Level.SEVERE, e.getMessage(), e);
								}
							} else {
								errorsl=9;
							}
						}
						rsvl.close();
						pstmtvl.close();
					} catch(Exception e) {
						log.log(Level.SEVERE, e.getMessage(), e);
					}
				} else {
					if (errorbp!=0) {
						ob2c.set_CustomColumn("ErrorMsg", menj1 );
						docnotok++;
					} else if (errorbpl!=0) {
						ob2c.set_CustomColumn("ErrorMsg", menj2);
						docnotok++;
					} else if (erroruser!=0) {
						ob2c.set_CustomColumn("ErrorMsg", menj6);
						docnotok++;
					}
					ob2c.save();
				}
				if (errorsl==9)
				;
				
				rs.close();
				pstmt.close();
			} catch(Exception e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}//for que recorre las ordenes de compra subidas
		
		
		//	Go through Order Records w/o
	

		//	Set Error to indicator to not imported
		
		
		//

		return "Proceso terminado - Ordenes Creadad:" + docok + " - Ordenes con Error: "+ docnotok  ;
	}	//	doIt
	//Compañia
	public int actualizaClient ()
	{ 
		/*
		 * metodo que actualiza la compañia
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
			  + " WHERE trim(o.BPartnerValue)=trim(bp.Value) AND o.AD_Client_ID=bp.AD_Client_ID) "
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
				  + " WHERE trim(o.ProductValue)=trim(p.Value) AND o.AD_Client_ID=p.AD_Client_ID) "
				  + "WHERE  ProductValue IS NOT NULL"
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
		sql = new StringBuffer ("UPDATE I_OrderB2C  "
				  + "SET SalesRep_ID=(SELECT MAX(u.AD_User_ID) FROM ad_user u"
				  + " WHERE trim(lower(ChargeName))=trim(lower(u.name)) AND AD_Client_ID=u.AD_Client_ID) "
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

