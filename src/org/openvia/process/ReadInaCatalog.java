package org.openvia.process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPaymentTerm;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MUser;
import org.compiere.model.X_C_Order;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.sqlj.BPartner;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openvia.inacatalog.Common;
import org.openvia.inacatalog.IPedidosLinsModel;
import org.openvia.inacatalog.I_iPedidos;
import org.openvia.inacatalog.I_iPedidosLins;
import org.openvia.inacatalog.iclientes.I_iClientes;
import org.openvia.inacatalog.iclienteslcontactos.I_iClientesLContactos;
import org.openvia.inacatalog.iclientesldirs.IClientesLDirsImp;
import org.openvia.inacatalog.iclientesldirs.I_iClientesLDirs;
import org.openvia.inacatalog.icobros.I_iCobros;
import org.posterita.core.DocStatusMap;

public class ReadInaCatalog extends SvrProcess implements I_iPedidos, I_iPedidosLins {

	/** Client to be imported to */
	private int m_AD_Client_ID = 1000000;
	/** Organization to be imported to */
	private int m_AD_Org_ID = 1000000;

	Common comun = new Common();
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
	} // prepare

	public JSONArray readJsonArrayFromUrl(String url) {
		JSONArray jsonArray = null;
		try {
			InputStream is = new URL(url).openStream();
			JSONParser jsonParser = new JSONParser();
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				Object obj = jsonParser.parse(rd);
				jsonArray = (JSONArray) obj;
			} finally {
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	I_iClientesLDirs clientesDir = new IClientesLDirsImp();

	@Override
	protected String doIt() {
		// Insertar Clientes desde inacatalog
		try {
			insertarClientes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Insertar Pedidos desde inacatalog
		JSONArray jsonArrayPedido = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/Api/iPedidos");
		if (jsonArrayPedido != null && jsonArrayPedido.size() > 0 && !jsonArrayPedido.get(0).toString().equals("[]")) {
			String nroPedido = null;
			for (int i = 0; i < jsonArrayPedido.size(); i++) {
				try {
					JSONObject jsonObjPedido = (JSONObject) jsonArrayPedido.get(i);
					nroPedido = jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString();
					// codTipoVenta = 4 no pasan a ADempiere
					if (!jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("4")) {
						if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("3")) { // Nota Pre-Venta
							MRequisition requisition = new MRequisition(getCtx(), 0, get_TrxName());
							requisition.setAD_Org_ID(m_AD_Org_ID);
							requisition.setC_DocType_ID(1000111); // 1000111: Reserva Fisica
//						requisition.setDocumentNo(jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString());
							requisition.setDocumentNo(null);
							requisition.setDateRequired(stringToTimestamp(jsonObjPedido.get(I_iPedidos.COLUMNA_FECPEDIDO).toString()));
							requisition.setDateDoc(stringToTimestamp(jsonObjPedido.get(I_iPedidos.COLUMNA_FECPEDIDO).toString()));
							requisition.set_CustomColumn("C_BPartner_ID", bPartnerByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODCLIENTE).toString()).getC_BPartner_ID());
							requisition.setAD_User_ID(100);
							requisition.setPriorityRule(MRequisition.PRIORITYRULE_Medium);
							requisition.setM_Warehouse_ID(1000001); // 1000001: Lampa
							requisition.setM_PriceList_ID(1000000); // 1000000: Ventas
							requisition.setDescription(jsonObjPedido.get(I_iPedidos.COLUMNA_OBSPEDIDO).toString());
							requisition.set_CustomColumn("C_BPartner_ID", bPartnerByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODCLIENTE).toString()).getC_BPartner_ID());
							requisition.set_CustomColumn("c_bpartner_location_ID", Integer.parseInt(clientesDir.apiGetClienteLDir(Integer.parseInt(jsonObjPedido.get(clientesDir.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(clientesDir.COLUMNA_CODCLIENTE).toString(), Integer.parseInt(jsonObjPedido.get(clientesDir.COLUMNA_LINDIRCLI).toString())).getCodSuDirCli()));
							requisition.set_CustomColumn("POREFERENCE", nroPedido);
							requisition.set_CustomColumn("inacatalog", "Y");
							requisition.save();
							List<IPedidosLinsModel> listaLinsPedidos = new ArrayList<IPedidosLinsModel>();
							JSONArray jsonArrayLine = readJsonArrayFromUrl(
									"http://190.215.113.91/InaCatalogAPI/Api/iPedidosLins?empresa="
											+ jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + "&nomipad="
											+ jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + "&codpedido="
											+ jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO));
							if (jsonArrayLine != null && jsonArrayLine.size() > 0
									&& !jsonArrayLine.get(0).toString().equals("[]")) {
								for (int j = 0; j < jsonArrayLine.size(); j++) {
									IPedidosLinsModel lins = new IPedidosLinsModel();
									JSONObject jsonObjLine = (JSONObject) jsonArrayLine.get(j);
									lins.setLinPedido(Integer.parseInt(jsonObjLine.get(I_iPedidosLins.COLUMNA_LINPEDIDO).toString()));
									lins.setCodArticulo(jsonObjLine.get(I_iPedidosLins.COLUMNA_CODARTICULO).toString());
									lins.setDesLinPed(jsonObjLine.get(I_iPedidosLins.COLUMNA_DESLINPED).toString());
									lins.setCanLinPed(Double.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_CANLINPED).toString()));
									listaLinsPedidos.add(lins);
								}
							}

							Set<String> hasSet = new HashSet<String>();
							for (IPedidosLinsModel lin : listaLinsPedidos) {
								hasSet.add(lin.getCodArticulo());
							}

							List<IPedidosLinsModel> listaFinal = new ArrayList<IPedidosLinsModel>();
							for (String codArticulo : hasSet) {
								List<IPedidosLinsModel> listTmp = null;
								for (IPedidosLinsModel lin : listaLinsPedidos) {
									if (codArticulo.equals(lin.getCodArticulo())) {
										if (listTmp == null) {
											// crea lista temp y asigna articulo
											listTmp = new ArrayList<IPedidosLinsModel>();
											listTmp.add(lin);
										} else {
											listTmp.get(0).setCanLinPed(listTmp.get(0).getCanLinPed() + lin.getCanLinPed());
										}
									}
								}
								listaFinal.add(listTmp.get(0));
							}

							for (IPedidosLinsModel lin : listaFinal) {
								MRequisitionLine requisitionLine = new MRequisitionLine(requisition);
								requisitionLine.setAD_Org_ID(m_AD_Org_ID);
								requisitionLine.setLine(lin.getLinPedido());
								MProduct product = productByValue(lin.getCodArticulo());
								requisitionLine.setM_Product_ID(product.getM_Product_ID());
								requisitionLine.setDescription(lin.getObsLinPed());

								// M_WareHouse_ID 1000010: Abastecimiento
								// M_WareHouse_ID 1000001: Lampa
								BigDecimal disponible = null;
								String sql = "SELECT qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001) as disponible "
										+ "FROM M_product p " + "WHERE  p.m_product_ID=" + product.getM_Product_ID();
								PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
								ResultSet rsps = pstmtps.executeQuery();
								if (rsps.next()) {
									disponible = rsps.getBigDecimal("disponible");
								}
								requisitionLine.setQty(new BigDecimal(lin.getCanLinPed()));
								/*
								 * if ( disponible.compareTo(new BigDecimal(lin.getCanLinPed())) >= 0 ) {
								 * orderLine.setQty(new BigDecimal(lin.getCanLinPed()));
								 * orderLine.setQtyOrdered(new BigDecimal(lin.getCanLinPed())); } else if (
								 * disponible.compareTo(BigDecimal.ZERO) == 1 && disponible.compareTo(new
								 * BigDecimal(lin.getCanLinPed())) == -1 ) {
								 * orderLine.setQtyEntered(disponible); orderLine.setQtyOrdered(disponible);
								 * orderLine.set_CustomColumn("DEMAND", lin.getCanLinPed()); } else if (
								 * disponible.compareTo(BigDecimal.ZERO) <= 0 ) { orderLine.setQtyEntered(new
								 * BigDecimal(0)); orderLine.setQtyOrdered(new BigDecimal(0));
								 * orderLine.set_CustomColumn("DEMAND", lin.getCanLinPed());
								 * orderLine.set_CustomColumn("NOTPRINT", "Y"); }
								 */
								requisitionLine.save();
							}
						} else {
							MOrder order = new MOrder(getCtx(), 0, get_TrxName());
							order.setAD_Org_ID(m_AD_Org_ID);
							order.setPOReference(jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString());
							/*
							 * ResultSet rsDocumentNo = DB.prepareStatement("",
							 * get_TrxName()).executeQuery(); String documentNo =
							 * rsDocumentNo.getString("o_DocumentNo");
							 */
							order.setDateOrdered(
									stringToTimestamp(jsonObjPedido.get(I_iPedidos.COLUMNA_FECPEDIDO).toString()));
							order.setC_BPartner_ID(
									bPartnerByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODCLIENTE).toString())
											.getC_BPartner_ID());
							order.setC_BPartner_Location_ID(Integer.parseInt(clientesDir
									.apiGetClienteLDir(
											Integer.parseInt(
													jsonObjPedido.get(clientesDir.COLUMNA_CODEMPRESA).toString()),
											jsonObjPedido.get(clientesDir.COLUMNA_CODCLIENTE).toString(),
											Integer.parseInt(
													jsonObjPedido.get(clientesDir.COLUMNA_LINDIRCLI).toString()))
									.getCodSuDirCli()));
							order.setSalesRep_ID(
									bPartnerByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODAGENTE).toString())
											.getC_BPartner_ID());
							order.setC_PaymentTerm_ID(
									paymentTermByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODFORMAPAGO).toString())
											.getC_PaymentTerm_ID());
							order.setC_Currency_ID(228);
							order.setTotalLines(
									new BigDecimal(jsonObjPedido.get(I_iPedidos.COLUMNA_TOTNETOPED).toString()));
							order.setGrandTotal(
									new BigDecimal(jsonObjPedido.get(I_iPedidos.COLUMNA_TOTPED).toString()));
							order.setDescription(jsonObjPedido.get(I_iPedidos.COLUMNA_OBSPEDIDO).toString());
							order.setIsSOTrx(true);
							order.setM_PriceList_ID(1000000); // Venta
							order.setM_Warehouse_ID(1000001); // Lampa
							order.setDocStatus(X_C_Order.DOCSTATUS_Drafted);
							order.set_CustomColumn("firma2", "Y"); // comercial
							order.set_CustomColumn("firma3", "N"); // finanzas
							order.set_CustomColumn("mediocompra", "InaCatalog");
							if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("2")) { // Nota
																												// Pre-Venta
								// 1000048: Orden de Pre-Venta
								order.setC_DocType_ID(1000048);
								order.setC_DocTypeTarget_ID(1000048);
								order.set_CustomColumn("CODCATALOGOINA",
										getCodCatalogo(
												Integer.parseInt(
														jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()),
												jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(),
												jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
								order.set_CustomColumn("CODTARIFAINA",
										getCodTarifa(
												Integer.parseInt(
														jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()),
												jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(),
												jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
							} else if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("0")) { // Nota
																													// Venta
																													// Normal
								// 1000030: Orden de Venta
								order.setC_DocType_ID(1000030);
								order.setC_DocTypeTarget_ID(1000030);
								order.set_CustomColumn("CODCATALOGOINA",
										getCodCatalogo(
												Integer.parseInt(
														jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()),
												jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(),
												jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
								order.set_CustomColumn("CODTARIFAINA",
										getCodTarifa(
												Integer.parseInt(
														jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()),
												jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(),
												jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
							} else if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("1")) { // Nota
																													// de
																													// Venta
																													// 72
																													// Horas
								// 1000030: Orden de Venta
								order.setC_DocType_ID(1000030);
								order.setC_DocTypeTarget_ID(1000030);
								order.set_CustomColumn("PAGO72HORAS", "Y");
								order.set_CustomColumn("CODCATALOGOINA",
										getCodCatalogo(
												Integer.parseInt(
														jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()),
												jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(),
												jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
								order.set_CustomColumn("CODTARIFAINA",
										getCodTarifa(
												Integer.parseInt(
														jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()),
												jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(),
												jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
							}
							order.setDocumentNo(null);
							order.save();

							List<IPedidosLinsModel> listaLinsPedidos = new ArrayList<IPedidosLinsModel>();
							JSONArray jsonArrayLine = readJsonArrayFromUrl(
									"http://190.215.113.91/InaCatalogAPI/Api/iPedidosLins?empresa="
											+ jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + "&nomipad="
											+ jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + "&codpedido="
											+ jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO));
							if (jsonArrayLine != null && jsonArrayLine.size() > 0
									&& !jsonArrayLine.get(0).toString().equals("[]")) {
								for (int j = 0; j < jsonArrayLine.size(); j++) {
									IPedidosLinsModel lins = new IPedidosLinsModel();
									JSONObject jsonObjLine = (JSONObject) jsonArrayLine.get(j);
									lins.setLinPedido(Integer
											.parseInt(jsonObjLine.get(I_iPedidosLins.COLUMNA_LINPEDIDO).toString()));
									lins.setCodArticulo(jsonObjLine.get(I_iPedidosLins.COLUMNA_CODARTICULO).toString());
									lins.setDesLinPed(jsonObjLine.get(I_iPedidosLins.COLUMNA_DESLINPED).toString());
									lins.setCanLinPed(Double
											.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_CANLINPED).toString()));
									listaLinsPedidos.add(lins);
								}
							}

							Set<String> hasSet = new HashSet<String>();
							for (IPedidosLinsModel lin : listaLinsPedidos) {
								hasSet.add(lin.getCodArticulo());
							}

							List<IPedidosLinsModel> listaFinal = new ArrayList<IPedidosLinsModel>();
							for (String codArticulo : hasSet) {
								List<IPedidosLinsModel> listTmp = null;
								for (IPedidosLinsModel lin : listaLinsPedidos) {
									if (codArticulo.equals(lin.getCodArticulo())) {
										if (listTmp == null) {
											// crea lista temp y asigna articulo
											listTmp = new ArrayList<IPedidosLinsModel>();
											listTmp.add(lin);
										} else {
											listTmp.get(0)
													.setCanLinPed(listTmp.get(0).getCanLinPed() + lin.getCanLinPed());
										}
									}
								}
								listaFinal.add(listTmp.get(0));
							}

							for (IPedidosLinsModel lin : listaFinal) {
								MOrderLine orderLine = new MOrderLine(order);
								orderLine.setAD_Org_ID(m_AD_Org_ID);
								orderLine.setLine(lin.getLinPedido());
								MProduct product = productByValue(lin.getCodArticulo());
								orderLine.setM_Product_ID(product.getM_Product_ID());
								orderLine.setDescription(lin.getObsLinPed());

								// M_WareHouse_ID 1000010: Abastecimiento
								// M_WareHouse_ID 1000001: Lampa
								BigDecimal disponible = null;
								String sql = "SELECT qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001) as disponible "
										+ "FROM M_product p " + "WHERE  p.m_product_ID=" + product.getM_Product_ID();
								PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
								ResultSet rsps = pstmtps.executeQuery();
								if (rsps.next()) {
									disponible = rsps.getBigDecimal("disponible");
								}

								if (disponible.compareTo(new BigDecimal(lin.getCanLinPed())) >= 0) {
									orderLine.setQtyEntered(new BigDecimal(lin.getCanLinPed()));
									orderLine.setQtyOrdered(new BigDecimal(lin.getCanLinPed()));
								} else if (disponible.compareTo(BigDecimal.ZERO) == 1
										&& disponible.compareTo(new BigDecimal(lin.getCanLinPed())) == -1) {
									orderLine.setQtyEntered(disponible);
									orderLine.setQtyOrdered(disponible);
									orderLine.set_CustomColumn("DEMAND", lin.getCanLinPed());
								} else if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
									orderLine.setQtyEntered(new BigDecimal(0));
									orderLine.setQtyOrdered(new BigDecimal(0));
									orderLine.set_CustomColumn("DEMAND", lin.getCanLinPed());
									orderLine.set_CustomColumn("NOTPRINT", "Y");
								}
								orderLine.save();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					comun.registrarLog("iPedidos", 500, "Error al insertar pedido nro " + nroPedido, e.toString(), "", "");
				}
			}
		}

		return "Pedidos Importados.";
	}

	private String getCodCatalogo(Integer empresa, String nomIPad, String codPedido) {
		String codCatalogo = "";
		try {
			JSONArray jsonArrayLine = readJsonArrayFromUrl(
					"http://190.215.113.91/InaCatalogAPI/Api/iPedidosLins?empresa=" + empresa + "&nomipad=" + nomIPad
							+ "&codpedido=" + codPedido);
			if (jsonArrayLine != null && jsonArrayLine.size() > 0 && !jsonArrayLine.get(0).toString().equals("[]")) {
				JSONObject jsonObjLine = (JSONObject) jsonArrayLine.get(0);
				codCatalogo = jsonObjLine.get(I_iPedidosLins.COLUMNA_CODCATALOGO).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return codCatalogo;
	}

	private String getCodTarifa(Integer empresa, String nomIPad, String codPedido) {
		String codTarifa = "";
		try {
			JSONArray jsonArrayLine = readJsonArrayFromUrl(
					"http://190.215.113.91/InaCatalogAPI/Api/iPedidosLins?empresa=" + empresa + "&nomipad=" + nomIPad
							+ "&codpedido=" + codPedido);
			if (jsonArrayLine != null && jsonArrayLine.size() > 0 && !jsonArrayLine.get(0).toString().equals("[]")) {
				JSONObject jsonObjLine = (JSONObject) jsonArrayLine.get(0);
				codTarifa = jsonObjLine.get(I_iPedidosLins.COLUMNA_CODTARIFA).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return codTarifa;
	}

	private Timestamp stringToTimestamp(String str) {
		Timestamp timestamp = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
			Date parsedDate = dateFormat.parse(str);
			timestamp = new java.sql.Timestamp(parsedDate.getTime());
		} catch (Exception e) { // this generic but you can control another types of exception
			e.printStackTrace();
		}
		return timestamp;
	}

	private MBPartner bPartnerByValue(String value) {
		MBPartner bPartner = null;
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append("SELECT * FROM C_BPartner WHERE AD_Client_ID=? AND Value=?");
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			int param = 1;
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(param++, m_AD_Client_ID);
			pstmt.setString(param++, value);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				bPartner = new MBPartner(Env.getCtx(), rs, null);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Could not retrieve BPartners", ex);
		} finally {
			DB.close(rs, pstmt);
		}
		return bPartner;
	}

	private MPaymentTerm paymentTermByValue(String value) {
		MPaymentTerm paymentTerm = null;
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append("SELECT * FROM C_PaymentTerm WHERE AD_Client_ID=? AND REPLACE(Value,' ', '')=?");
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			int param = 1;
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(param++, m_AD_Client_ID);
			pstmt.setString(param++, value);

			rs = pstmt.executeQuery();

			while (rs.next()) {

				paymentTerm = new MPaymentTerm(Env.getCtx(), rs, null);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Could not retrieve MPaymentTerm", ex);
		} finally {
			DB.close(rs, pstmt);
		}
		return paymentTerm;
	}

	private MProduct productByValue(String value) {
		MProduct product = null;
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append("SELECT * FROM M_Product WHERE AD_Client_ID=? AND Value=?");
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			int param = 1;
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(param++, m_AD_Client_ID);
			pstmt.setString(param++, value);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				product = new MProduct(Env.getCtx(), rs, null);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Could not retrieve MProduct", ex);
		} finally {
			DB.close(rs, pstmt);
		}
		return product;
	}

	private void insertarClientes() throws Exception {
		JSONArray jsonArrayCliente = readJsonArrayFromUrl(
				"http://190.215.113.91/InaCatalogAPI/api/iClientes?empresa=1&flaNvoCliente=2&flaExpCliente=0");
		if (jsonArrayCliente != null && jsonArrayCliente.size() > 0
				&& !jsonArrayCliente.get(0).toString().equals("[]")) {
			for (int i = 0; i < jsonArrayCliente.size(); i++) {
				JSONObject jsonObjCliente = (JSONObject) jsonArrayCliente.get(i);
				MBPartner bp = new MBPartner(getCtx(), 0, get_TrxName());
				bp.setAD_Org_ID(m_AD_Org_ID);
				bp.setValue(jsonObjCliente.get(I_iClientes.COLUMNA_CODCLIENTE).toString());
				bp.setName(jsonObjCliente.get(I_iClientes.COLUMNA_NOMCLIENTE).toString().isEmpty()
						? jsonObjCliente.get(I_iClientes.COLUMNA_CODCLIENTE).toString()
						: jsonObjCliente.get(I_iClientes.COLUMNA_NOMCLIENTE).toString());
				bp.setC_BP_Group_ID(1000005); // 1000005: Clientes
				bp.setIsCustomer(true);
				String codCliente = jsonObjCliente.get(I_iClientes.COLUMNA_CODCLIENTE).toString();
				if (bp.save()) {
					// Insertar Direcciones Cliente
					JSONArray jsonArrayDir = readJsonArrayFromUrl(
							"http://190.215.113.91/InaCatalogAPI/api/iClientesLDirs?empresa=1&codcliente="
									+ codCliente.replaceAll(" ", "%20"));
					if (jsonArrayDir != null && jsonArrayDir.size() > 0
							&& !jsonArrayDir.get(0).toString().equals("[]")) {
						for (int ii = 0; ii < jsonArrayDir.size(); ii++) {
							JSONObject jsonObjDir = (JSONObject) jsonArrayDir.get(ii);
							MBPartnerLocation bpLoc = new MBPartnerLocation(bp);
							bpLoc.setAD_Org_ID(m_AD_Org_ID);
							bpLoc.setName(jsonObjDir.get(I_iClientesLDirs.COLUMNA_NOMDIRCLI).toString());
							bpLoc.save();
						}
					}

					// Insertar Contactos Cliente
					JSONArray jsonArrayContacto = readJsonArrayFromUrl(
							"http://190.215.113.91/InaCatalogAPI/api/iClientesLContactos?empresa=1&codcliente="
									+ codCliente.replaceAll(" ", "%20"));
					if (jsonArrayContacto != null && jsonArrayContacto.size() > 0
							&& !jsonArrayContacto.get(0).toString().equals("[]")) {
						for (int iii = 0; iii < jsonArrayContacto.size(); iii++) {
							JSONObject jsonObjContacto = (JSONObject) jsonArrayContacto.get(iii);
							MUser user = new MUser(bp);
							user.setAD_Org_ID(m_AD_Org_ID);
							user.setName(jsonObjContacto.get(I_iClientesLContactos.COLUMNA_NOMCONTACTCLI).toString());
							user.save();
						}
					}
				}
			}
		}
	}

}
