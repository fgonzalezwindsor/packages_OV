package org.openvia.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PutMethod;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPaymentTerm;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.model.MUser;
import org.compiere.model.X_C_Order;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openvia.inacatalog.Common;
import org.openvia.inacatalog.IPedidosLinsModel;
import org.openvia.inacatalog.I_iPedidos;
import org.openvia.inacatalog.I_iPedidosLins;
import org.openvia.inacatalog.iclientes.IClientesImp;
import org.openvia.inacatalog.iclientes.IClientesModel;
import org.openvia.inacatalog.iclientes.I_iClientes;
import org.openvia.inacatalog.iclienteslcontactos.I_iClientesLContactos;
import org.openvia.inacatalog.iclientesldirs.IClientesLDirsImp;
import org.openvia.inacatalog.iclientesldirs.I_iClientesLDirs;

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
	I_iClientes clientes = new IClientesImp();

	@Override
	protected String doIt() {
		// Insertar Clientes desde inacatalog
		try {
			insertarClientes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/**
		 * 1 Nota Venta Normal: se inserta en ADempiere como venta valida stock y reserva fisica (C_Order)
		 * 2 Nota de Venta 72 Hora: se inserta en ADempiere con campo PAGO72HORAS=Y valida stock y reserva fisica (C_Order)
		 * 3 Nota Pre-Venta: se inserta en ADempiere, no valida stock ni reserva fisica (C_Order)
		 * 4 Reserva Física: (M_Requisition)
		 * 5 Propuesta: (No se inserta en ADempiere)
		 */

		// Insertar Pedidos desde inacatalog
		JSONArray jsonArrayPedido = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/Api/iPedidos");
		if (jsonArrayPedido != null && jsonArrayPedido.size() > 0 && !jsonArrayPedido.get(0).toString().equals("[]")) {
			String nroPedido = null;
			for (int i = 0; i < jsonArrayPedido.size(); i++) {
				try {
					JSONObject jsonObjPedido = (JSONObject) jsonArrayPedido.get(i);
					if (jsonObjPedido.get(I_iPedidos.COLUMNA_FLAEXPPEDIDO).toString().equals("0")
							&& jsonObjPedido.get(I_iPedidos.COLUMNA_DATESTADOPEDIDO).toString().equals("Traspasar")) {
						nroPedido = jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString();
						// codTipoVenta = 4 no pasan a ADempiere
						if (!jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("5")) {
							if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("4")) { // Reserva Física
								StringBuffer s_sql = new StringBuffer();
								// C_DocType_id = 1000111 Reserva Fisica
								s_sql.append("SELECT r.M_Requisition_ID")
										.append(" FROM M_Requisition r")
										.append(" WHERE r.C_BPartner_ID = ")
										.append(bPartnerByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODCLIENTE).toString()).getC_BPartner_ID())
										.append(" AND r.C_DocType_id = 1000111")
										.append(" AND r.DocStatus IN ('CO','CL')")
										.append(" ORDER BY r.Created DESC");
								PreparedStatement pst = DB.prepareStatement(s_sql.toString(), get_TrxName());
								ResultSet rs = pst.executeQuery();
								MRequisition requisition = null;
								boolean tieneRequisition = false;
								if (rs.next()) {
									requisition = new MRequisition(getCtx(), rs.getInt("M_Requisition_ID"), get_TrxName());
									tieneRequisition = true;
								} else {
									requisition = new MRequisition(getCtx(), 0, get_TrxName());
									requisition.setAD_Org_ID(m_AD_Org_ID);
									requisition.set_CustomColumn("IsSotrx", "Y");
									requisition.setC_DocType_ID(1000111); // 1000111: Reserva Fisica
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
									requisition.set_CustomColumn("documentnoinacat", jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + " - " + nroPedido);
									requisition.set_CustomColumn("inacatalog", "Y");
									requisition.save();
								}
								rs.close();
								pst.close();
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
										lins.setLinPedido(Integer.parseInt(
												jsonObjLine.get(I_iPedidosLins.COLUMNA_LINPEDIDO).toString()));
										lins.setCodArticulo(
												jsonObjLine.get(I_iPedidosLins.COLUMNA_CODARTICULO).toString());
										lins.setDesLinPed(jsonObjLine.get(I_iPedidosLins.COLUMNA_DESLINPED).toString());
										lins.setCanLinPed(Double.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_CANLINPED).toString()));
										lins.setPreLinPed(Double.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_PRELINPED).toString()));
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
												listTmp.get(0).setCanLinPed(
														listTmp.get(0).getCanLinPed() + lin.getCanLinPed());
											}
										}
									}
									listaFinal.add(listTmp.get(0));
								}

								for (IPedidosLinsModel lin : listaFinal) {
									MRequisitionLine requisitionLine;
									s_sql = new StringBuffer();
									MProduct product = productByValue(lin.getCodArticulo());
									s_sql.append("SELECT rl.M_RequisitionLine_ID, rl.QtyReserved")
											.append(" FROM M_RequisitionLine rl")
											.append(" WHERE rl.M_Product_ID = ").append(product.getM_Product_ID())
											.append(" AND rl.M_Requisition_ID = ").append(requisition.getM_Requisition_ID())
											.append(" ORDER BY rl.Created");
									pst = DB.prepareStatement(s_sql.toString(), get_TrxName());
									rs = pst.executeQuery();
									if (rs.next()) {
										requisitionLine = new MRequisitionLine(Env.getCtx(), rs.getInt("M_RequisitionLine_ID"), get_TrxName());
										requisitionLine.setQty(requisitionLine.getQty().add(new BigDecimal(lin.getCanLinPed())));
										requisitionLine.set_CustomColumn("QtyReserved", new BigDecimal(requisitionLine.get_Value("QtyReserved").toString()).add(new BigDecimal(lin.getCanLinPed())));
										if (requisitionLine.save()) {
											// guarda registro ov_pedido_requisitionline
											if (tieneRequisition)
												guardarRegistroPedidoReqLine(nroPedido, requisitionLine.getM_RequisitionLine_ID());
										} else {
											comun.registrarLog("iPedidosLins", 500, "Error al insertar linea " + lin.getLinPedido() + " pedido " + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + " - " + nroPedido, "", "", "");
										}
									} else {
										requisitionLine = new MRequisitionLine(requisition);
										requisitionLine.setAD_Org_ID(m_AD_Org_ID);
										requisitionLine.setLine(lin.getLinPedido());
//										MProduct product = productByValue(lin.getCodArticulo());
										requisitionLine.setM_Product_ID(product.getM_Product_ID());
										requisitionLine.setDescription(lin.getObsLinPed());

										// M_WareHouse_ID 1000010: Abastecimiento
										// M_WareHouse_ID 1000001: Lampa
										BigDecimal disponible = null;
										String sql = "SELECT qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001) as disponible "
												+ "FROM M_product p " + "WHERE  p.m_product_ID="
												+ product.getM_Product_ID();
										PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
										ResultSet rsps = pstmtps.executeQuery();
										if (rsps.next()) {
											disponible = rsps.getBigDecimal("disponible");
										}
										rsps.close();
										pstmtps.close();
										
										requisitionLine.setQty(new BigDecimal(lin.getCanLinPed()));
										requisitionLine.set_CustomColumn("QtyReserved", new BigDecimal(lin.getCanLinPed()));
										if ( disponible.compareTo(new BigDecimal(lin.getCanLinPed())) >= 0 ) {
											requisitionLine.setQty(new BigDecimal(lin.getCanLinPed()));
											requisitionLine.set_CustomColumn("QtyReserved", new BigDecimal(lin.getCanLinPed()));
										} else if (disponible.compareTo(BigDecimal.ZERO) == 1 && disponible.compareTo(new BigDecimal(lin.getCanLinPed())) == -1 ) {
											requisitionLine.setQty(disponible);
											requisitionLine.set_CustomColumn("QtyReserved", new BigDecimal(lin.getCanLinPed()));
											requisitionLine.set_CustomColumn("DEMAND", lin.getCanLinPed()); 
										} else if (disponible.compareTo(BigDecimal.ZERO) <= 0 ) { 
											requisitionLine.setQty(new BigDecimal(0));
											requisitionLine.set_CustomColumn("DEMAND", lin.getCanLinPed());
											requisitionLine.set_CustomColumn("NOTPRINT", "Y"); 
										}
										if (requisitionLine.save()) {
											// guarda registro ov_pedido_requisitionline
											if (tieneRequisition)
												guardarRegistroPedidoReqLine(nroPedido, requisitionLine.getM_RequisitionLine_ID());
										} else {
											comun.registrarLog("iPedidosLins", 500, "Error al insertar linea " + lin.getLinPedido() + " pedido " + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + " - " + nroPedido, "", "", "");
										}
									}
									rs.close();
									pst.close();
								}
							} else {
								MOrder order = new MOrder(getCtx(), 0, get_TrxName());
								order.setAD_Org_ID(m_AD_Org_ID);
								order.set_CustomColumn("documentnoinacat", jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString() + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString() + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString());
								order.setDateOrdered(stringToTimestamp(jsonObjPedido.get(I_iPedidos.COLUMNA_FECPEDIDO).toString()));
								order.setC_BPartner_ID(bPartnerByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODCLIENTE).toString()).getC_BPartner_ID());
								if (clientesDir.apiGetClienteLDir(Integer.parseInt(jsonObjPedido.get(clientesDir.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(clientesDir.COLUMNA_CODCLIENTE).toString(), Integer.parseInt(jsonObjPedido.get(clientesDir.COLUMNA_LINDIRCLI).toString())).getCodSuDirCli().equals(""))
									order.setC_BPartner_Location_ID(MBPartnerLocation.getForBPartner(getCtx(), order.getC_BPartner_ID(),get_TrxName())[0].getC_BPartner_Location_ID());
								else
									order.setC_BPartner_Location_ID(Integer.parseInt(clientesDir.apiGetClienteLDir(Integer.parseInt(jsonObjPedido.get(clientesDir.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(clientesDir.COLUMNA_CODCLIENTE).toString(), Integer.parseInt(jsonObjPedido.get(clientesDir.COLUMNA_LINDIRCLI).toString())).getCodSuDirCli()));
								order.setSalesRep_ID(salesRepByBPartner(jsonObjPedido.get(I_iPedidos.COLUMNA_CODAGENTE).toString()));
								order.setC_PaymentTerm_ID(paymentTermByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODFORMAPAGO).toString()).getC_PaymentTerm_ID());
								order.setC_Currency_ID(228);
								order.setTotalLines(new BigDecimal(jsonObjPedido.get(I_iPedidos.COLUMNA_TOTNETOPED).toString()));
								order.setGrandTotal(new BigDecimal(jsonObjPedido.get(I_iPedidos.COLUMNA_TOTPED).toString()));
								order.setDescription(jsonObjPedido.get(I_iPedidos.COLUMNA_OBSPEDIDO).toString());
								order.setIsSOTrx(true);
								order.setM_PriceList_ID(1000000); // Venta
								order.setM_Warehouse_ID(1000001); // Lampa
								order.setDocStatus(X_C_Order.DOCSTATUS_Drafted);
								order.set_CustomColumn("firma2", "Y"); // comercial
								order.set_CustomColumn("firma3", "N"); // finanzas
								order.set_CustomColumn("mediocompra", "InaCatalog");
								if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("3")) { // Nota Pre-Venta
									// 1000048: Orden de Pre-Venta
									order.setC_DocType_ID(1000048);
									order.setC_DocTypeTarget_ID(1000048);
									order.set_CustomColumn("CODCATALOGOINA", getCodCatalogo(Integer.parseInt(jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(), jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
									order.set_CustomColumn("CODTARIFAINA", getCodTarifa(Integer.parseInt(jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(), jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
								} else if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("1")) { // Nota Venta Normal
									// 1000030: Orden de Venta
									order.setC_DocType_ID(1000030);
									order.setC_DocTypeTarget_ID(1000030);
									order.set_CustomColumn("CODCATALOGOINA", getCodCatalogo(Integer.parseInt(jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(), jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
									order.set_CustomColumn("CODTARIFAINA", getCodTarifa(Integer.parseInt(jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(), jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
								} else if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("2")) { // Nota de Venta 72 Hora
									// 1000030: Orden de Venta
									order.setC_DocType_ID(1000030);
									order.setC_DocTypeTarget_ID(1000030);
									order.set_CustomColumn("PAGO72HORAS", "Y");
									order.set_CustomColumn("CODCATALOGOINA", getCodCatalogo(Integer.parseInt(jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(), jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
									order.set_CustomColumn("CODTARIFAINA", getCodTarifa(Integer.parseInt(jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString()), jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString(), jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString()));
								}
								order.set_CustomColumn("VENTAINVIERNO", "N");
								order.setDocumentNo(null);
								if (!order.save())
									comun.registrarLog("iPedidos", 500, "Error al insertar pedido " + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + " - " + nroPedido, "", "", "");

								List<IPedidosLinsModel> listaLinsPedidos = new ArrayList<IPedidosLinsModel>();
								JSONArray jsonArrayLine = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/Api/iPedidosLins?empresa=" + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + "&nomipad="	+ jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + "&codpedido="	+ jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO));
								if (jsonArrayLine != null && jsonArrayLine.size() > 0 && !jsonArrayLine.get(0).toString().equals("[]")) {
									for (int j = 0; j < jsonArrayLine.size(); j++) {
										IPedidosLinsModel lins = new IPedidosLinsModel();
										JSONObject jsonObjLine = (JSONObject) jsonArrayLine.get(j);
										lins.setLinPedido(Integer.parseInt(jsonObjLine.get(I_iPedidosLins.COLUMNA_LINPEDIDO).toString()));
										lins.setCodArticulo(jsonObjLine.get(I_iPedidosLins.COLUMNA_CODARTICULO).toString());
										lins.setDesLinPed(jsonObjLine.get(I_iPedidosLins.COLUMNA_DESLINPED).toString());
										lins.setCanLinPed(Double.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_CANLINPED).toString()));
										lins.setPreLinPed(Double.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_PRELINPED).toString()));
										lins.setTpcDto01(Double.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_TPCDTO01).toString()));
										lins.setTpcDto02(Double.valueOf(jsonObjLine.get(I_iPedidosLins.COLUMNA_TPCDTO02).toString()));
										listaLinsPedidos.add(lins);
									}
								}

								Set<String> hasSet = new HashSet<String>();
								for (IPedidosLinsModel lin : listaLinsPedidos) {
									hasSet.add(lin.getCodArticulo());
								}

								// Se juntan productos duplicados
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
								
								// 3 Nota Pre-Venta: no valida stock ni reserva fisica.
								if (jsonObjPedido.get(I_iPedidos.COLUMNA_CODTIPOVENTA).toString().equals("3")) { // Nota Pre-Venta
									for (IPedidosLinsModel lin : listaFinal) {
										MOrderLine orderLine = new MOrderLine(order);
										MProduct product = productByValue(lin.getCodArticulo());
										orderLine.setAD_Org_ID(m_AD_Org_ID);
										orderLine.setLine(lin.getLinPedido());
										orderLine.setM_Product_ID(product.getM_Product_ID());
										orderLine.setDescription(lin.getObsLinPed());
										orderLine.setQtyEntered(new BigDecimal(lin.getCanLinPed()));
										orderLine.setQtyOrdered(new BigDecimal(lin.getCanLinPed()));
										BigDecimal priceList = new BigDecimal(lin.getPreLinPed());
										BigDecimal desc1 = new BigDecimal(lin.getTpcDto01());
										BigDecimal desc2 = new BigDecimal(lin.getTpcDto02());
										BigDecimal precioConDesc1 = priceList.subtract(priceList.multiply(desc1.divide(new BigDecimal(100))));
										BigDecimal precioFinal = precioConDesc1.subtract(precioConDesc1.multiply(desc2.divide(new BigDecimal(100))));
										orderLine.setPriceActual(precioFinal);
										orderLine.setPriceEntered(precioFinal);
										orderLine.setPriceList(priceList);
										if (!orderLine.save())
											comun.registrarLog("iPedidosLins", 500, "Error al insertar linea " + lin.getLinPedido() + " pedido " + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + " - " + nroPedido, "", "", "");
									}
								} else {
									for (IPedidosLinsModel lin : listaFinal) {
										MProduct product = productByValue(lin.getCodArticulo());
										StringBuffer s_sql = new StringBuffer();
										// C_DocType_id = 1000111 Reserva Fisica
										s_sql.append("SELECT rl.M_RequisitionLine_ID, rl.QtyReserved")
												.append(" FROM M_Requisition r")
												.append(" JOIN M_RequisitionLine rl ON rl.M_Requisition_ID = r.M_Requisition_ID")
												.append(" WHERE r.C_BPartner_ID = ")
												.append(bPartnerByValue(jsonObjPedido.get(I_iPedidos.COLUMNA_CODCLIENTE).toString()).getC_BPartner_ID())
												.append(" AND r.C_DocType_id = 1000111")
												.append(" AND r.DocStatus IN ('CO','CL')")
												.append(" AND rl.M_Product_ID = ").append(product.getM_Product_ID())
												.append(" AND rl.QtyReserved > 0 ")
												.append(" ORDER BY rl.Created");

										PreparedStatement pst = DB.prepareStatement(s_sql.toString(), get_TrxName());
										ResultSet rs = pst.executeQuery();
										BigDecimal cantPedido = new BigDecimal(lin.getCanLinPed());
										Map<Integer, BigDecimal> mapRequisitionLine = new HashMap();
										boolean tieneReserva = false;
										while (rs.next()) {
											tieneReserva = true;
											if (cantPedido.compareTo(new BigDecimal(rs.getInt("QtyReserved"))) <= 0) {
												mapRequisitionLine.put(rs.getInt("M_RequisitionLine_ID"), cantPedido);
												cantPedido = cantPedido.subtract(new BigDecimal(rs.getInt("QtyReserved")));
												break;
											} else
											// Si la cantidad del pedido es mayor a la reserva fisica, se debe abrir la linea de la orden
											{
												mapRequisitionLine.put(rs.getInt("M_RequisitionLine_ID"), cantPedido);
												cantPedido = cantPedido.subtract(new BigDecimal(rs.getInt("QtyReserved")));
											}
										}
										rs.close();
										pst.close();
										if (tieneReserva) {
											if (cantPedido.compareTo(BigDecimal.ZERO) > 0) {
												mapRequisitionLine.put(null, cantPedido);
											}

											for (Map.Entry<Integer, BigDecimal> entry : mapRequisitionLine.entrySet()) {
												MOrderLine orderLine = new MOrderLine(order);
												orderLine.setAD_Org_ID(m_AD_Org_ID);
												orderLine.setLine(lin.getLinPedido());
												orderLine.setM_Product_ID(product.getM_Product_ID());
												orderLine.setDescription(lin.getObsLinPed());
												
												// M_WareHouse_ID 1000010: Abastecimiento
												// M_WareHouse_ID 1000001: Lampa
												BigDecimal disponible = null;
												String sql = "SELECT qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001) as disponible "
														+ "FROM M_product p " + "WHERE  p.m_product_ID="
														+ product.getM_Product_ID();
												PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
												ResultSet rsps = pstmtps.executeQuery();
												if (rsps.next()) {
													disponible = rsps.getBigDecimal("disponible");
												}
												rsps.close();
												pstmtps.close();
												if (entry.getKey() == null) {
													if (disponible.compareTo(entry.getValue()) >= 0) {
														orderLine.setQtyEntered(entry.getValue());
														orderLine.setQtyOrdered(entry.getValue());
													} else if (disponible.compareTo(BigDecimal.ZERO) == 1 && disponible.compareTo(entry.getValue()) == -1) {
														orderLine.setQtyEntered(disponible);
														orderLine.setQtyOrdered(disponible);
														orderLine.set_CustomColumn("DEMAND", entry.getValue());
													} else if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
														orderLine.setQtyEntered(new BigDecimal(0));
														orderLine.setQtyOrdered(new BigDecimal(0));
														orderLine.set_CustomColumn("DEMAND", entry.getValue());
														orderLine.set_CustomColumn("NOTPRINT", "Y");
													}
												} else {
													orderLine.set_CustomColumn("M_RequisitionLine_ID", entry.getKey());
													orderLine.setQtyEntered(entry.getValue());
													orderLine.setQtyOrdered(entry.getValue());
												}
												BigDecimal priceList = new BigDecimal(lin.getPreLinPed());
												BigDecimal desc1 = new BigDecimal(lin.getTpcDto01());
												BigDecimal desc2 = new BigDecimal(lin.getTpcDto02());
												BigDecimal precioConDesc1 = priceList.subtract(priceList.multiply(desc1.divide(new BigDecimal(100))));
												BigDecimal precioFinal = precioConDesc1.subtract(precioConDesc1.multiply(desc2.divide(new BigDecimal(100))));
												orderLine.setPriceActual(precioFinal);
												orderLine.setPriceEntered(precioFinal);
												orderLine.setPriceList(priceList);
												if (!orderLine.save())
													comun.registrarLog("iPedidosLins", 500, "Error al insertar linea " + lin.getLinPedido() + " pedido " + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + " - " + nroPedido, "", "", "");
											}
										} else {
											MOrderLine orderLine = new MOrderLine(order);
											orderLine.setAD_Org_ID(m_AD_Org_ID);
											orderLine.setLine(lin.getLinPedido());
											orderLine.setM_Product_ID(product.getM_Product_ID());
											orderLine.setDescription(lin.getObsLinPed());
											// M_WareHouse_ID 1000010: Abastecimiento
											// M_WareHouse_ID 1000001: Lampa
											BigDecimal disponible = null;
											String sql = "SELECT qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001) as disponible "
													+ "FROM M_product p " + "WHERE  p.m_product_ID="
													+ product.getM_Product_ID();
											PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
											ResultSet rsps = pstmtps.executeQuery();
											if (rsps.next()) {
												disponible = rsps.getBigDecimal("disponible");
											}
											rsps.close();
											pstmtps.close();
											if (disponible.compareTo(new BigDecimal(lin.getCanLinPed())) >= 0) {
												orderLine.setQtyEntered(new BigDecimal(lin.getCanLinPed()));
												orderLine.setQtyOrdered(new BigDecimal(lin.getCanLinPed()));
											} else if (disponible.compareTo(BigDecimal.ZERO) == 1 && disponible.compareTo(new BigDecimal(lin.getCanLinPed())) == -1) {
												orderLine.setQtyEntered(disponible);
												orderLine.setQtyOrdered(disponible);
												orderLine.set_CustomColumn("DEMAND", new BigDecimal(lin.getCanLinPed()));
											} else if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
												orderLine.setQtyEntered(new BigDecimal(0));
												orderLine.setQtyOrdered(new BigDecimal(0));
												orderLine.set_CustomColumn("DEMAND", new BigDecimal(lin.getCanLinPed()));
												orderLine.set_CustomColumn("NOTPRINT", "Y");
											}
											BigDecimal priceList = new BigDecimal(lin.getPreLinPed());
											BigDecimal desc1 = new BigDecimal(lin.getTpcDto01());
											BigDecimal desc2 = new BigDecimal(lin.getTpcDto02());
											BigDecimal precioConDesc1 = priceList.subtract(priceList.multiply(desc1.divide(new BigDecimal(100))));
											BigDecimal precioFinal = precioConDesc1.subtract(precioConDesc1.multiply(desc2.divide(new BigDecimal(100))));
											orderLine.setPriceActual(precioFinal);
											orderLine.setPriceEntered(precioFinal);
											orderLine.setPriceList(priceList);
											if (!orderLine.save())
												comun.registrarLog("iPedidosLins", 500, "Error al insertar linea " + lin.getLinPedido() + " pedido " + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA) + " - " + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD) + " - " + nroPedido, "", "", "");
										}
									}
								}
								// Definir Lista de precios
								/** Si M_Product.catalogo
								 *  Es Preventa Liquidacion --> m_pricelist_id 1000014
     								Es Preventa Normal --> m_pricelist_id 1000000
     								Es Liquidacion --> m_pricelist_id 1000014
     								Es Normal --> m_pricelist_id 1000000
     								Es Marcas Propias -- > lista de precios 1000000
								 */
								String catalogo = "";
								int MPriceListID = 0;
								for (MOrderLine line : order.getLines()) {
									if (catalogo.equals("") && getCatalogoProduct(line.getM_Product_ID()) != null) {
										catalogo = getCatalogoProduct(line.getM_Product_ID());
									}
									if (catalogo.equals("Preventa Liquidacion") || catalogo.equals("Liquidacion")) {
										MPriceListID = 1000014;
										break;
									}
									if (catalogo.equals("Preventa Normal") || catalogo.equals("Normal") || catalogo.equals("Marcas Propias")) {
										MPriceListID = 1000000;
									}
									
								}
								if (MPriceListID != 0) {
									order.setM_PriceList_ID(MPriceListID);
									order.saveEx();
								}
								
								// Completar Documento
								boolean completarOrden = true;
								catalogo = "";
								for (MOrderLine line : order.getLines()) {
									if (line.get_Value("DEMAND") != null && new BigDecimal(line.get_Value("DEMAND").toString()).compareTo(BigDecimal.ZERO) > 0) {
										completarOrden = false;
										break;
									}
									if (catalogo.equals("") && getCatalogoProduct(line.getM_Product_ID()) != null) {
										catalogo = getCatalogoProduct(line.getM_Product_ID());
									}
									if (!catalogo.equals(getCatalogoProduct(line.getM_Product_ID()))) {
										completarOrden = false;
										break;
									}
									
								}
								if (completarOrden)
									order.processIt(DocAction.ACTION_Complete);
							}
						}
						// Deja pedido con flaExpPedido=1
						HttpClient httpClient = new HttpClient();
						String url = "http://190.215.113.91/InaCatalogAPI/api/iPedidos?empresa=" + jsonObjPedido.get(I_iPedidos.COLUMNA_CODEMPRESA).toString() + "&nomipad=" + jsonObjPedido.get(I_iPedidos.COLUMNA_NOMIPAD).toString() + "&codpedido=" + jsonObjPedido.get(I_iPedidos.COLUMNA_CODPEDIDO).toString() + "&flaexppedido=1";
						PutMethod putMethod = new PutMethod(url.replaceAll(" ", "%20"));
						try {
							httpClient.executeMethod(putMethod);
						} catch (HttpException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
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
		
	private void guardarRegistroPedidoReqLine(String nroPedido, Integer requisitionLineID) {
		try {
			String sql = "INSERT INTO ov_pedido_requisitionline (nro_pedido, m_requisitionline_id) VALUES (?, ?)";
			PreparedStatement pst = DB.prepareStatement(sql, get_TrxName());
			pst.setString(1, nroPedido);
			pst.setInt(2, requisitionLineID);
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
	
	private Integer salesRepByBPartner(String value) {
		Integer salesRepID = null;
		StringBuffer sqlStmt = new StringBuffer();
		sqlStmt.append("select u.ad_user_id")
			.append(" from c_bpartner bp")
			.append(" join ad_user u on u.c_bpartner_id = bp.c_bpartner_id")
			.append(" where bp.ad_client_id = ?")
			.append(" and bp.value = ?");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			int param = 1;
			pstmt = DB.prepareStatement(sqlStmt.toString(), null);
			pstmt.setInt(param++, m_AD_Client_ID);
			pstmt.setString(param++, value);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				salesRepID = rs.getInt("ad_user_id");
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Could not retrieve BPartners", ex);
		} finally {
			DB.close(rs, pstmt);
		}
		return salesRepID;
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

	public void insertarClientes() throws Exception {
		System.out.println("Insertar Clientes nuevos en ADempiere");
		for (IClientesModel cliente : clientes.clientesNuevos()) {
			MBPartner bp = new MBPartner(getCtx(), 0, get_TrxName());
			bp.setAD_Org_ID(m_AD_Org_ID);
			bp.setValue(cliente.getCodCliente());
			bp.setName(cliente.getNomCliente().isEmpty()
					? cliente.getCodCliente().toString()
					: cliente.getNomCliente());
			bp.setC_BP_Group_ID(1000005); // 1000005: Clientes
			bp.setIsCustomer(true);
			String codCliente = cliente.getCodCliente();
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
						if (!bpLoc.save())
							comun.registrarLog("iClientesLDirs", 500, "Error al insertar direccion cliente " + cliente.getCodCliente() + " direccion " + jsonObjDir.get(I_iClientesLDirs.COLUMNA_NOMDIRCLI).toString(), "", "", "");
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
						if (!user.save())
							comun.registrarLog("iClientesLContactos", 500, "Error al insertar contacto cliente " + cliente.getCodCliente() + " contacto " + jsonObjContacto.get(I_iClientesLContactos.COLUMNA_NOMCONTACTCLI).toString(), "", "", "");
					}
				}
				
				// Actualiza Cliente importado
				cliente.setFlaExpCliente("1");
				clientes.apiPutCliente(cliente);
			} else {
				comun.registrarLog("iClientes", 500, "Error al insertar cliente " + cliente.getCodCliente(), "", "", "");
			}
		}
	}
	
	private String getCatalogoProduct(int MProductID) {
		String catalogo = null;
		try {
			PreparedStatement pst = DB.prepareStatement("SELECT catalogo FROM M_Product WHERE M_Product_ID = ?", get_TrxName());
			pst.setInt(1, MProductID);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				catalogo = rs.getString("catalogo");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return catalogo;
	}

}
