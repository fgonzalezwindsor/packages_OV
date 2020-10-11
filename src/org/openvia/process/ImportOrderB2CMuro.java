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
import org.compiere.model.MProductPrice;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.windsor.model.X_C_OrderB2C;
import org.windsor.model.X_C_OrderB2CLine;

/**
 * Import Order from I_Order
 * 
 * @author Isaac Castro
 *         <li>BF [ 2936629 ] Error when creating bpartner in the importation
 *         order
 *         <li>https://sourceforge.net/tracker/?func=detail&aid=2936629&group_id=176962&atid=879332
 * @author Isaac Castro
 * @version $Id: ImportOrder.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class ImportOrderB2CMuro extends SvrProcess {
	/** Client to be imported to */
	private int m_AD_Client_ID = 1000000;
	/** Organization to be imported to */
	private int m_AD_Org_ID = 1000000;
	/** Delete old Imported */
	// private boolean m_deleteOldImported = false;
	private StringBuffer sql = null;
	private int no = 0;
	private String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;
	/** Document Action */
	// private String m_docAction = MOrder.DOCACTION_Prepare;

	// variables de ccorreo
	final String miCorreo = "soporte@comercialwindsor.cl";
	final String miContrasena = "Cw9121100";
	final String servidorSMTP = "smtp.gmail.com";
	final String puertoEnvio = "465";
	String mailReceptor = null;
	String asunto = null;
	String cuerpo = null;
	
	//Integer unidades = null;
//	Integer numproductosm = null;
	String telefono = null;
	String email = null;
	Integer direccion = null;
	
	/** Effective */
	private Timestamp m_DateValue = null;

	private Integer m_M_WareHouse_ID = 1000001; // Lampa
	private Integer m_M_WareHouse_ab_ID = 1000010; // Abastecimiento

	/**
	 * Prepare - e.g., get Parameters.
	 */
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = 1000000;
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = 1000000;
			else if (name.equals("DeleteOldImported"))
				;// m_deleteOldImported = "Y".equals(para[i].getParameter());
			else if (name.equals("DocAction"))
				;// m_docAction = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (m_DateValue == null)
			m_DateValue = new Timestamp(System.currentTimeMillis());
	} // prepare

	/**
	 * Perform process.
	 * 
	 * @return Message
	 * @throws Exception
	 */
	protected String doIt() throws java.lang.Exception {
		String menj5 = null;
		// **** Prepare ****

		// Set Client, Org, IsActive, Created/Updated
		log.info("Reset=" + actualizaClient());
		// BP Value
		log.fine("Set BP from Value=" + actualizaBP());
		// BP Location
		log.fine("Found Location=" + actualizaBPL());
		// Product
		log.fine("Set Product from Value=" + actualizaProduct());
		// Venedero
		log.fine("Set SalesRep_ID=" + actualizaSalesRep());
		commitEx();

		ArrayList<String> ordenescompra = new ArrayList<String>();
		// -- New Orders -----------------------------------------------------
		String slqoc = "Select Documentno, BPartnerValue from I_OrderB2C where i_isImported<>'Y' " + clientCheck
				+ " Group by DocumentNo, BPartnerValue";
		// obtener ordenes para importar
		try {
			PreparedStatement pstmt = DB.prepareStatement(slqoc, get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String sqlcreados = "Select count(I_OrderB2C.DocumentNO) as cuenta from I_orderB2C where I_orderb2c.i_isImported<>'Y' "
						+ " and I_orderb2c.documentno in (select C_orderb2c.poreference from c_orderb2c "
						+ " where I_orderb2c.documentno=c_orderb2c.poreference and c_orderb2c.bpartnerValue=I_orderb2c.bpartnervalue) "
						+ " and I_orderb2c.documentno=" + rs.getString("DocumentNo");
				try {
					PreparedStatement pstmtc = DB.prepareStatement(sqlcreados, get_TrxName());
					ResultSet rsc = pstmtc.executeQuery();

					if (rsc.next())
						if (rsc.getInt("cuenta") == 0)
							ordenescompra.add(rs.getString("DocumentNo"));
						else {
							log.fine("Set SalesRep_ID="
									+ beforeImport(rs.getString("DocumentNo"), rs.getString("BPartnerValue")));
							commitEx();
						}
					rsc.close();
					pstmtc.close();
				} catch (Exception e) {

					log.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {

			log.log(Level.SEVERE, e.getMessage(), e);
		}

		int docok = 0;
		int docnotok = 0;
		if (!ordenescompra.isEmpty()) // ordenes no esta vacio?
			for (int i = 0; i < ordenescompra.size(); i++) // a recorrer Ordenes
			{
				String recorre = "Select documentno, COALESCE(c_bpartner_ID,0) C_BPartner_ID,  COALESCE(ov_bpartner_location_ID,0)ov_bpartner_location_ID,"
						+ "	 COALESCE(M_product_ID, 0) M_product_ID,"
						+ " replace(description,'/','-') description, name as tipogt, ContactName formacgt,"
						+ " trim(upper(Address2)) as subtienda, ChargeName as vendedor, Coalesce (SalesRep_ID,0) SalesRep_ID , "
						+ " LineDescription as Generico, COALESCE(QtyOrdered,0) as Cantidad, COALESCE(PriceActual,0) Precio, Created, ProductValue, BPartnerValue, "
						+ " Address1, DOCTYPENAME, ov_org_id, C_ORDERMURO_id, NOMBRESHOPIFY, DIRECCIONSHOPIFY "
						+ " from  I_OrderB2C where documentNo=" + ordenescompra.get(i);
				int errorp = 0;
				try {
					PreparedStatement pstmt = DB.prepareStatement(recorre, get_TrxName());
					ResultSet rs = pstmt.executeQuery();
					int lineas = 1;
					X_C_OrderB2C ob2c = null;
					while (rs.next()) // while crea ob2c 1 por 1
					{
						if (ob2c == null) {
							String address = null;
							String region = null;
							ResultSet rsDir = DB.prepareStatement("Select l.address1, r.name, c.name, bpl.phone, bpl.email "
									+ "from c_bpartner_Location bpl "
									+ "inner join c_location l on (bpl.c_location_ID=l.c_location_ID) "
									+ "left outer join c_city c on (l.c_city_ID=c.c_city_ID) "
									+ "left outer join c_region r on (l.c_region_ID= r.c_region_ID) "
									+ "where bpl.c_Bpartner_location_ID=" + rs.getInt("OV_BPartner_Location_ID"), get_TrxName()).executeQuery();
							if (rsDir.next()) {
								address = rsDir.getString("address");
								region = rsDir.getString("region");
								telefono = rsDir.getString("telefono");
								email = rsDir.getString("email");
							}
							rsDir.close();
							
//							ResultSet rsCant = DB.prepareStatement("select coalesce(sum(OL.QTYENTERED),0) unidades, count(ol.m_product_ID) numproductosm "
//									+ "from c_ORDERline ol "
//									+ "where OL.C_ORDER_ID = " + rs.getString("C_Order_ID")
//									+ "and ol.qtyentered>0 "
//									+ "and ol.m_product_ID <> 1026126 "
//									+ "and ol.m_product_ID is not null", get_TrxName()).executeQuery();
//							if (rsCant.next()) {
////								unidades = rsCant.getInt("unidades");
////								numproductosm = rsCant.getInt("numproductosm");
//							}
//							rsCant.close();
							
							if (rs.getInt("OV_Org_ID") == 1000022) {
								direccion = 1011338;
							} else if (rs.getInt("OV_Org_ID") == 1000019) {
								direccion = 1010879;
							}
							
							// cabecera
							ob2c = new X_C_OrderB2C(getCtx(), 0, get_TrxName());
							ob2c.setAD_Org_ID(1000000);
							ob2c.setC_BPartner_ID(rs.getInt("C_BPartner_ID"));
							ob2c.setC_BPartner_Location_ID(direccion);
							ob2c.setDescription((rs.getString("Description") + ": " + email).length()>250?(rs.getString("Description") + ": " + email).substring(0, 250):(rs.getString("Description") + ": " + email));
							ob2c.setPOReference(rs.getString("documentno"));
							ob2c.setDateAcct(rs.getTimestamp("Created"));
							ob2c.set_CustomColumn("BPartnerValue", rs.getString("BPartnerValue"));
							ob2c.set_CustomColumn("Address1", rs.getString("Address1"));
							// medio de compra y forma de compra para definir bodega
//							String mc = "";
//							if (rs.getString("tipogt") != null && !rs.getString("tipogt").isEmpty()) {
//								mc = rs.getString("tipogt").substring(0, 1).toUpperCase() + rs.getString("tipogt")
//										.substring(1, rs.getString("tipogt").length()).toLowerCase();
//							}
//							ob2c.set_CustomColumn("MedioCompra", mc);
//							String fc = "";
//							if (rs.getString("formacgt") != null && !rs.getString("formacgt").isEmpty()) {
//								String nuevacadena = "";
//								String[] palabras = rs.getString("formacgt").split(" ");
//								for (int x = 0; x < palabras.length; x++) {
//									nuevacadena += palabras[x].substring(0, 1).toUpperCase() + palabras[x].substring(1, palabras[x].length()).toLowerCase() + " ";
//								}
//								fc = nuevacadena.trim();
//							}
//							if (fc.equals("En Verde")) {
//								ob2c.setVentaEnVerde(true);
//								ob2c.setM_Warehouse_ID(m_M_WareHouse_ID);
//							} else {
//								ob2c.setVentaEnVerde(false);
//								ob2c.setM_Warehouse_ID(m_M_WareHouse_ID);
//							}
//							ob2c.set_CustomColumn("FormaCompra", fc);
							ob2c.setM_Warehouse_ID(m_M_WareHouse_ID);
							ob2c.setSalesRep_ID(rs.getInt("SalesRep_ID"));
							ob2c.setProcessed(false);
							ob2c.setM_PriceList_ID(1000000);
							// subtienda
//							if (rs.getInt("C_BPartner_ID") > 0 && rs.getString("subtienda") != null) {
//								String sqlst = "Select count (*) as cuenta from C_BPartner_SubTienda where c_Bpartner_ID="
//										+ rs.getInt("C_BPartner_ID") + " and trim(upper(name))= trim(upper('"
//										+ rs.getString("subtienda") + "'))";
//								try {
//									PreparedStatement pstmtst = DB.prepareStatement(sqlst, get_TrxName());
//									ResultSet rsst = pstmtst.executeQuery();
//
//									if (rsst.next()) {
//										if (rsst.getInt("cuenta") > 0) {
//											String sqlstv = "Select max( C_BPartner_SubTienda_ID)as C_BPartner_SubTienda_ID  from C_BPartner_SubTienda where c_Bpartner_ID="
//													+ rs.getInt("C_BPartner_ID")
//													+ " and trim(upper(name))= trim(upper('" + rs.getString("subtienda")
//													+ "'))";
//											try {
//												PreparedStatement pstmtstv = DB.prepareStatement(sqlstv, get_TrxName());
//												ResultSet rsstv = pstmtstv.executeQuery();
//
//												if (rsstv.next())
//													ob2c.setC_BPartner_SubTienda_ID(
//															rsstv.getInt("C_BPartner_SubTienda_ID"));
//
//												rsstv.close();
//												pstmtstv.close();
//											} catch (Exception e) {
//
//												log.log(Level.SEVERE, e.getMessage(), e);
//											}
//										}
//									}
//
//									rsst.close();
//									pstmtst.close();
//								} catch (Exception e) {
//									log.log(Level.SEVERE, e.getMessage(), e);
//								}
//							} // subtienda if

							// Campos Muro
							ob2c.set_CustomColumn("C_ORDERMURO_id", rs.getInt("C_ORDERMURO_id"));
							ob2c.set_CustomColumn("DOCUMENTOMURO", rs.getString("DocumentNo"));
							ob2c.set_CustomColumn("VENTAINVIERNO", "N");
							ob2c.set_CustomColumn("OBSERVACIONES", "Cliente: " + MBPartner.get(getCtx(), 1001237).getName() + " Telefono: " + telefono + " Email: " + email);
							ob2c.set_CustomColumn("unidadesmuro", rs.getInt("cantidad"));
//							ob2c.set_CustomColumn("numproductos", numproductosm);
							ob2c.set_CustomColumn("NOMBRESHOPIFY", rs.getString("NOMBRESHOPIFY"));
							ob2c.set_CustomColumn("DIRECCIONSHOPIFY", rs.getString("DIRECCIONSHOPIFY"));
							
							ob2c.save();

						} // if crea cabecera

						BigDecimal sob = new BigDecimal(0);

						if (ob2c != null) {
							X_C_OrderB2CLine obl = new X_C_OrderB2CLine(getCtx(), 0, get_TrxName());
							
							Integer mProductID = null;
							Integer cUomID = null;
							ResultSet rsProd = DB.prepareStatement("SELECT max(M_PRODUCT_ID) M_PRODUCT_ID, max(C_UOM_ID) C_UOM_ID "
									+ "FROM M_PRODUCT "
									+ "WHERE VALUE = '" + rs.getString("ProductValue") + "' "
									+ "AND ISACTIVE = 'Y'", get_TrxName()).executeQuery();
							if (rsProd.next()) {
								mProductID = rsProd.getInt("M_PRODUCT_ID");
								cUomID = rsProd.getInt("C_UOM_ID");
							}
							rsProd.close();
							// 1000039: Muro
							BigDecimal priceProduct = MProductPrice.get(getCtx(), 1000039, mProductID, get_TrxName()).getPriceList();
							
							obl.setC_OrderB2C_ID(ob2c.getC_OrderB2C_ID());
							obl.setLine(lineas * 10);
							BigDecimal cant = new BigDecimal(rs.getInt("Cantidad"));
							obl.setQtyEntered(cant);
							obl.setPriceEntered(priceProduct.intValue());
							BigDecimal neto = new BigDecimal(rs.getInt("Cantidad") * priceProduct.intValue());
							obl.setLineNetAmt(neto);
							obl.setProductValue(rs.getString("ProductValue"));
							obl.set_ValueOfColumn("NOMBREGENERICO", rs.getString("DOCTYPENAME"));
							obl.set_CustomColumn("DEMAND", rs.getBigDecimal("cantidad"));
							String sqlp = "Select count(*) as cuenta from m_product where m_product_ID=" + mProductID;
							// validar si producto existe y si tiene stock en la bodega correspondiente
							try {
								PreparedStatement pstmtp = DB.prepareStatement(sqlp, get_TrxName());
								ResultSet rsp = pstmtp.executeQuery();
								if (rsp.next()) {
									if (rsp.getInt("cuenta") == 1) {
										obl.setM_Product_ID(mProductID);
										// validar Stock
										String sqlps = "Select (qtyavailableofb(p.m_product_ID," + m_M_WareHouse_ID + ") + qtyavailableofb(p.m_product_ID," + m_M_WareHouse_ab_ID + ")) as Disponible, p.ProductType " +
												" from M_product p where  p.m_product_ID=" + rs.getInt("M_Product_ID");
										try {
											PreparedStatement pstmtps = DB.prepareStatement(sqlps, get_TrxName());
											ResultSet rsps = pstmtps.executeQuery();
											if (rsps.next()) {
												if (rsps.getString("ProductType").equals("I")) {
													if (rs.getInt("Cantidad") <= rsps.getInt("Disponible")) {
														obl.setPASARAOV(true);
													} else {
														errorp = 5;
														obl.setPASARAOV(false);
														obl.setErrorMsg("No hay stock en bodega");
														obl.setStockBodegas(sob);
													}
												} else {// no es alamcenable
													obl.setPASARAOV(true);
												}
											}
											rsps.close();
											pstmtps.close();
										} catch (Exception e) {

											log.log(Level.SEVERE, e.getMessage(), e);
										}
										// valida Stock
									}
								} else {
									obl.setPASARAOV(false);
									obl.setStockBodegas(sob);
								}

								rsp.close();
								pstmtp.close();
							} catch (Exception e) {
								log.log(Level.SEVERE, e.getMessage(), e);
							}
							if (errorp > 0) {
								if (errorp == 5) {
									menj5 = "Error: Producto sin Stock";
									obl.set_CustomColumn("ErrorMsg", menj5);
									obl.setStockBodegas(sob);
									docnotok++;
								}
							}
							if (errorp == 5)
								obl.set_CustomColumn("ErrorMsg", "No hay stock en la bodega actual");
							
							obl.save();
						}
						lineas++;

					} // while que recorre los productos de las ordenes

					// Crear nota de venta sin errores
					if (errorp == 0) {
						// sin errores controlados
						MOrder order = null;
						int contador = 1;
						int contadordoc = 0;

						// validar que todas las lineas esten okey y que hau lineas
						String sqlvl = "Select count(*) cuenta from C_OrderB2CLine where C_OrderB2C_ID="
								+ ob2c.getC_OrderB2C_ID();

						try {

							PreparedStatement pstmtvl = DB.prepareStatement(sqlvl, get_TrxName());
							ResultSet rsvl = pstmtvl.executeQuery();

							if (rsvl.next()) {
								if (rsvl.getInt("cuenta") > 0) {
									// valida que todas las lineas esten OK
									String sqlvlok = "Select count(*) cuenta from C_OrderB2CLine where PASARAOV<>'Y' and C_OrderB2C_ID="
											+ ob2c.getC_OrderB2C_ID();
									try {

										PreparedStatement pstmtvlok = DB.prepareStatement(sqlvlok, get_TrxName());
										ResultSet rsvlok = pstmtvlok.executeQuery();

										if (rsvlok.next())
											if (rsvlok.getInt("cuenta") == 0) {
												String sqllines = "Select * from C_OrderB2CLine where  C_OrderB2C_ID=" + ob2c.getC_OrderB2C_ID();
												try {
													PreparedStatement pstmtlines = DB.prepareStatement(sqllines, get_TrxName());
													ResultSet rslines = pstmtlines.executeQuery();
													while (rslines.next()) {
														if (order == null) {
															contadordoc = contadordoc + 1;
															order = new MOrder(getCtx(), 0, get_TrxName());
															MBPartner bp = new MBPartner(getCtx(), ob2c.getC_BPartner_ID(), get_TrxName());
															order.setClientOrg(ob2c.getAD_Client_ID(), ob2c.getAD_Org_ID());
//															if (ob2c.getC_BPartner_ID() != 1011716)
															order.setC_DocTypeTarget_ID(1000030);
//															else
//																order.setC_DocTypeTarget_ID(1000030);
															order.setIsSOTrx(true);
															order.setDeliveryRule("O");
															order.setC_BPartner_ID(ob2c.getC_BPartner_ID());
															order.setC_BPartner_Location_ID(ob2c.getC_BPartner_Location_ID());
															order.setPOReference(ob2c.getPOReference());
															order.setAD_User_ID(ob2c.getSalesRep_ID());
															// Bill Partner
															order.setBill_BPartner_ID(ob2c.getC_BPartner_ID());
															order.setBill_Location_ID(ob2c.getC_BPartner_Location_ID());
															if (ob2c.getDescription() != null)
																order.setDescription(ob2c.getDescription());
															if (ob2c.get_ValueAsString("FechaPrometidaInt") != null)
																order.set_ValueOfColumn("FechaPrometidaInt", ob2c.get_ValueAsString("FechaPrometidaInt"));
															order.setC_PaymentTerm_ID(bp.getC_PaymentTerm_ID());
															order.setM_PriceList_ID(ob2c.getM_PriceList_ID());
															order.setM_Warehouse_ID(ob2c.getM_Warehouse_ID());
															order.setSalesRep_ID(ob2c.getSalesRep_ID());
															BigDecimal stbd = new BigDecimal(ob2c.getC_BPartner_SubTienda_ID());
															if (stbd != null && ob2c.getC_BPartner_SubTienda_ID() > 0)
																order.set_CustomColumn("C_BPartner_SubTienda_ID", ob2c.getC_BPartner_SubTienda_ID());
															order.setDateOrdered(ob2c.getDateAcct());
															order.setDateAcct(ob2c.getDateAcct());
															order.setInvoiceRule("D");
															order.set_CustomColumn("C_ORDERMURO_id", rs.getInt("C_ORDERMURO_id"));
															order.set_CustomColumn("DOCUMENTOMURO", rs.getString("DocumentNo"));
															order.set_CustomColumn("VENTAINVIERNO", "N");
															order.set_CustomColumn("OBSERVACIONES", "Cliente: " + MBPartner.get(getCtx(), 1001237).getName() + " Telefono: " + telefono + " Email: " + email);
															order.set_CustomColumn("unidadesmuro", rs.getInt("cantidad"));
//															order.set_CustomColumn("numproductos", numproductosm);
															order.set_CustomColumn("NOMBRESHOPIFY", rs.getString("NOMBRESHOPIFY"));
															order.set_CustomColumn("DIRECCIONSHOPIFY", rs.getString("DIRECCIONSHOPIFY"));
															order.save();
														}
														if (contador <= 16) {
															MOrderLine line = new MOrderLine(order);
															X_C_OrderB2CLine lines = new X_C_OrderB2CLine(getCtx(),	rslines.getInt("C_OrderB2CLine_ID"), get_TrxName());
															line.setM_Product_ID(lines.getM_Product_ID());
															line.setPriceEntered(
																	new BigDecimal(lines.getPriceEntered()));
															line.setPriceActual(
																	new BigDecimal(lines.getPriceEntered()));
															line.setPriceList(new BigDecimal(lines.getPriceEntered()));
															line.setQtyEntered((lines.getQtyEntered()));
															line.setLine(contador * 10);
															line.setPrice(new BigDecimal(lines.getPriceEntered()));
															line.setQty(lines.getQtyEntered());
															line.set_CustomColumn("Demand", lines.getQtyEntered());
															BigDecimal df = new BigDecimal(0);
															line.set_CustomColumn("Discount2", df);
															line.set_CustomColumn("Discount3", df);
															line.set_CustomColumn("Discount", df);
															line.set_CustomColumn("Discount3", df);
															line.set_CustomColumn("Discount4", df);
															line.set_CustomColumn("Discount5", df);
															line.set_CustomColumn("NotPrint", "N");
															line.set_CustomColumn("DEMAND", rs.getBigDecimal("cantidad"));

															line.setLineNetAmt();
															line.save();
															lines.setC_Order_ID(order.getC_Order_ID());
															lines.setProcessed(true);
															lines.save();
															contador++;
														}
														if (contador == 17) {
															order.setDocAction("CO");
															order.processIt("CO");
															order.save();
															docok++;
															contador = 1;
															order = null;
														}
													} // while
													order.setDocAction("CO");
													order.processIt("CO");
													order.save();
													ob2c.setProcessed(true);
													ob2c.save();
													contador = 1;
													docok++;
													order = null;
													sql = new StringBuffer("UPDATE I_OrderB2C "
															+ "SET I_IsImported='Y' , processed='Y' "
															+ "WHERE Documentno=" + ob2c.getPOReference());

													no = DB.executeUpdate(sql.toString(), get_TrxName());
													if (no != 0)
														log.warning("No importado=" + no);

													commitEx();
													rslines.close();
													pstmtlines.close();
												} catch (Exception e) {

													log.log(Level.SEVERE, e.getMessage(), e);
												}

											}
										rsvlok.close();
										pstmtvlok.close();
									} catch (Exception e) {

										log.log(Level.SEVERE, e.getMessage(), e);
									}
								}
							}

							rsvl.close();
							pstmtvl.close();
						} catch (Exception e) {

							log.log(Level.SEVERE, e.getMessage(), e);
						}

					} else {
						ob2c.save();
					}
					rs.close();
					pstmt.close();
				} catch (Exception e) {

					log.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		return "Proceso terminado - Ordenes Creadad:" + docok + " - Ordenes con Error: " + docnotok;
	} // doIt

	public int actualizaClient() {
		/*
		 * metodo que actualiza la compa�ia
		 * 
		 */
		sql = new StringBuffer("UPDATE I_OrderB2C " + "SET AD_Client_ID = COALESCE (AD_Client_ID,")
				.append(m_AD_Client_ID).append(")," + " AD_Org_ID = COALESCE (AD_Org_ID,").append(m_AD_Org_ID)
				.append(")," + " IsActive = COALESCE (IsActive, 'Y')," + " Created = COALESCE (Created, SysDate),"
						+ " CreatedBy = COALESCE (CreatedBy, 0)," + " Updated = COALESCE (Updated, SysDate),"
						+ " UpdatedBy = COALESCE (UpdatedBy, 0)," + " I_ErrorMsg = ' '," + " I_IsImported = 'N' "
						+ "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());

		return no;
	}

	// Socio de Negocio
	public int actualizaBP() {
		/*
		 * metodo que actualiza el cliente
		 * 
		 */
		sql = new StringBuffer(
				"UPDATE I_OrderB2C o " + "SET C_BPartner_ID=(SELECT MAX(C_BPartner_ID) FROM C_BPartner bp"
						+ " WHERE o.BPartnerValue=bp.Value AND o.AD_Client_ID=bp.AD_Client_ID) "
						+ "WHERE C_BPartner_ID IS NULL AND BPartnerValue IS NOT NULL" + " AND I_IsImported<>'Y'")
								.append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());

		return no;
	}

	// direccion
	public int actualizaBPL() {
		/*
		 * metodo que actualiza la direccion
		 * 
		 */
		sql = new StringBuffer("UPDATE I_OrderB2C o "
				+ "SET (BillTo_ID,C_BPartner_Location_ID)=(SELECT max(C_BPartner_Location_ID)C_BPartner_Location_ID,max(C_BPartner_Location_ID)C_BPartner_Location_ID"
				+ " FROM C_BPartner_Location bpl INNER JOIN C_Location l ON (bpl.C_Location_ID=l.C_Location_ID)"
				+ " WHERE o.C_BPartner_ID=bpl.C_BPartner_ID AND bpl.AD_Client_ID=o.AD_Client_ID"
				+ " AND ( trim(upper(o.Address1))=trim(upper(l.Address1) )  or  trim(upper(o.Address1))=trim(upper(bpl.Name) )  ) )"
				// + " AND DUMP(o.City)=DUMP(l.City) AND DUMP(o.Postal)=DUMP(l.Postal)"
				// + " AND o.C_Region_ID=l.C_Region_ID AND o.C_Country_ID=l.C_Country_ID) "
				+ "WHERE C_BPartner_ID IS NOT NULL AND C_BPartner_Location_ID IS NULL" + " AND I_IsImported='N'")
						.append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());

		return no;
	}

	// Producto
	public int actualizaProduct() {
		/*
		 * metodo que actualiza el producto
		 * 
		 */
		sql = new StringBuffer("UPDATE I_OrderB2C o " + "SET M_Product_ID=(SELECT MAX(M_Product_ID) FROM M_Product p"
				+ " WHERE o.ProductValue=p.Value AND o.AD_Client_ID=p.AD_Client_ID) "
				+ "WHERE M_Product_ID IS NULL AND ProductValue IS NOT NULL" + " AND I_IsImported<>'Y'")
						.append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	// Vendedor
	public int actualizaSalesRep() {
		/*
		 * metodo que actualiza el vendedor
		 * 
		 */
		sql = new StringBuffer("UPDATE I_OrderB2C o " + "SET o.SalesRep_ID=(SELECT MAX(u.AD_User_ID) FROM ad_user u"
				+ " WHERE trim(lower(o.ChargeName))=trim(lower(u.name)) AND o.AD_Client_ID=u.AD_Client_ID) "
				+ "WHERE ChargeName IS NOT NULL " + " AND I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}

	public int beforeImport(String document, String bpvalue) {
		/*
		 * metodo que actualiza las ordenes que ya existan (poreference y rut igual)
		 * 
		 */
		sql = new StringBuffer(
				"UPDATE I_OrderB2C  " + "SET I_IsImported='Y' , I_ERRORMSG = 'Orden Importada previamente' "
						+ "WHERE documentno= " + document + " and bpartnervalue=" + bpvalue);

		no = DB.executeUpdate(sql.toString(), get_TrxName());
		return no;
	}
} // ImportOrder
