package org.openvia.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MInOut;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPrereserva;
import org.compiere.model.MPrereservaLine;
import org.compiere.model.MProduct;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.openvia.inacatalog.I_iPedidos;

public class ProcesarPrereserva extends SvrProcess {
	
	/*
	int inOut_ID = 0;
	protected void prepare() {
		inOut_ID = getRecord_ID();
	}
	*/
	int order_ID = 0;
	protected void prepare() {
		order_ID = getRecord_ID();
	}

	protected String doIt() throws Exception {
		//MInOut inout = new MInOut(Env.getCtx(), inOut_ID, get_TrxName());
//		int[] listaPrereservas = MPrereserva.getAllIDs("OV_Prereserva", "C_Order_ID="+order_ID, get_TrxName());
		try {
//		StringBuffer sqlDoctosPreventa = new StringBuffer("SELECT Count(*) FROM OV_Documentos_preventa WHERE C_Order_origen_ID = " + order_ID);
//		int doctosPreventa = DB.getSQLValue(get_TrxName(), sqlDoctosPreventa.toString());
		
		StringBuffer sqlPrevNoProcesados = new StringBuffer("SELECT Count(*) FROM OV_Prereserva p WHERE p.C_Order_ID = " + order_ID + " AND p.DocStatus = 'CO' AND p.ov_prereserva_id NOT IN (SELECT ov_prereserva_id FROM OV_Documentos_preventa WHERE ov_prereserva_id = p.ov_prereserva_id)");
		int prevNoProcesados = DB.getSQLValue(get_TrxName(), sqlPrevNoProcesados.toString());
		if (prevNoProcesados > 0) {
			PreparedStatement pstPre = DB.prepareStatement("SELECT p.OV_Prereserva_ID FROM OV_Prereserva p, C_DocType t WHERE p.C_DocType_ID = t.C_DocType_ID AND p.C_Order_ID = " + order_ID + " AND p.DocStatus = 'CO' AND p.ov_prereserva_id NOT IN (SELECT ov_prereserva_id FROM OV_Documentos_preventa WHERE ov_prereserva_id = p.ov_prereserva_id) Order By t.ov_orden ASC, p.datedoc", get_TrxName());
//			PreparedStatement pstPre = DB.prepareStatement("SELECT p.OV_Prereserva_ID FROM OV_Prereserva p, C_DocType t WHERE p.C_DocType_ID = t.C_DocType_ID AND p.C_Order_ID = " + order_ID + " AND p.DocStatus = 'CO' Order By t.ov_orden ASC", get_TrxName());
			ResultSet res = pstPre.executeQuery();
			Map<String, String> mapError = new HashMap<String, String>();
			while (res.next()) {
//					for (int id : listaPrereservas) {
						MPrereserva prereserva = new MPrereserva(getCtx(), res.getInt(1), get_TrxName());
						// 1000571: PreVenta - Nota de Venta
						if (prereserva.getC_DocType_ID() == 1000571) {
							MOrder order = new MOrder(getCtx(), 0, get_TrxName());
							order.setC_BPartner_ID(prereserva.getC_BPartner_ID());
//							order.setC_BPartner_Location_ID(prereserva.getC_BPartner_Location_ID());
							order.setC_DocType_ID(1000030); // 1000030: Orden de Venta
							order.setC_DocTypeTarget_ID(1000030);
							order.setIsSOTrx(true);
							order.setSalesRep_ID(prereserva.getSalesRep_ID());
							order.set_CustomColumn("FOLIONVVENDOR", prereserva.get_Value("FOLIO_VENDEDOR"));
							order.setDescription(prereserva.getDescription());
							order.setC_BPartner_Location_ID(prereserva.get_ValueAsInt("c_bpartner_location_ent_id"));
							order.setBill_Location_ID(prereserva.get_ValueAsInt("c_bpartner_location_fact_id"));
							order.set_CustomColumn("OperadorLogistico", prereserva.get_Value("OperadorLogistico"));
							order.set_CustomColumn("M_OperadorLogistico_ID", prereserva.get_Value("M_OperadorLogistico_ID"));
							order.set_CustomColumn("M_OPERADORLOGISTICOLINE_ID", prereserva.get_Value("M_OPERADORLOGISTICOLINE_ID"));
							order.set_CustomColumn("VentaInvierno", "N");
							order.set_CustomColumn("ov_prereserva_id", prereserva.getOV_Prereserva_ID());
							order.set_CustomColumn("FIRMA2", "Y");
							order.set_CustomColumn("FIRMA3", "N");
							order.setDeliveryRule("O");
							order.set_CustomColumn("FIRMA1", "Y");
							order.setC_PaymentTerm_ID(MBPartner.get(Env.getCtx(), prereserva.getC_BPartner_ID()).getC_PaymentTerm_ID());
							order.setPaymentRule(MBPartner.get(Env.getCtx(), prereserva.getC_BPartner_ID()).getPaymentRule());
							order.saveEx();
							List<String> listErrorLinea = new ArrayList<String>();
							for (MPrereservaLine prereservaLine : prereserva.getLines()) {
								if (prereservaLine.getC_OrderLine_ID() != 0) {
									MOrderLine orderLine = new MOrderLine(order);
									BigDecimal disponible = null;
									String sql = "SELECT " + //qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001)"+
											" COALESCE ( "+
										      "         (SELECT SUM (s.qtyonhand) "+
										       "           FROM rv_storage s "+
										        "         WHERE     s.M_Product_ID = p.m_product_id "+
										         "              AND s.m_warehouse_id IN (1000001, 1000010) "+
										          "             AND s.isactive = 'Y'), "+
										           "    0) "+
										          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
										         "         FROM C_orderline ol2      "+
										          "             INNER JOIN C_Order o2  "+
										           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
										            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
										             "          AND o2.m_warehouse_id = 1000001 "+
										              "         AND o2.saldada <> 'Y' "+
										               "        AND o2.docstatus IN ('IP', 'CO', 'CL') "+
										                "       AND o2.issotrx = 'Y' "+
										                 "      AND o2.c_doctypetarget_ID NOT IN "+
										                  "            (1000110, 1000048, 1000568)) "+
										            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
										             "     FROM M_Requisitionline rl     "+
										              "         INNER JOIN M_Requisition r  "+
										               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
										                " WHERE     rl.M_Product_ID = p.m_product_id  "+
										                 "      AND r.m_warehouse_id = 1000001  "+
										                  "     AND r.docstatus IN ('CO', 'CL')  "+
										                   "    AND r.issotrx = 'Y'))  "+
											" as disponible "
											+ "FROM M_product p " 
											+ "WHERE p.m_product_ID = "	+ prereservaLine.getM_Product_ID();
									PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
									ResultSet rsps = pstmtps.executeQuery();
									if (rsps.next()) {
										disponible = rsps.getBigDecimal("disponible");
									}
									rsps.close();
									pstmtps.close();
									
									// Guardar disponible en linea de preventa
									prereservaLine.set_CustomColumn("DISPONIBLE", disponible);
									prereservaLine.save();
									
									if (disponible.compareTo(prereservaLine.getQty()) >= 0) {
										orderLine.setQtyEntered(prereservaLine.getQty());
										orderLine.setQtyOrdered(prereservaLine.getQty());
									} else if (disponible.compareTo(BigDecimal.ZERO) == 1 && disponible.compareTo(prereservaLine.getQty()) == -1) {
										orderLine.setQtyEntered(disponible);
										orderLine.setQtyOrdered(disponible);
										orderLine.set_CustomColumn("DEMAND", prereservaLine.getQty());
									} else if (disponible.compareTo(BigDecimal.ZERO) <= 0) {
										orderLine.setQtyEntered(new BigDecimal(0));
										orderLine.setQtyOrdered(new BigDecimal(0));
										orderLine.set_CustomColumn("DEMAND", prereservaLine.getQty());
										orderLine.set_CustomColumn("NOTPRINT", "Y");
									}
									orderLine.setM_Product_ID(prereservaLine.getM_Product_ID());
									orderLine.setPriceEntered(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
									orderLine.setPriceActual(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
									orderLine.setPriceList(new BigDecimal(prereservaLine.get_Value("PriceList").toString()));
									orderLine.set_CustomColumn("discount2", prereservaLine.get_Value("discount2"));
									orderLine.set_CustomColumn("discount3", prereservaLine.get_Value("discount3"));
									orderLine.set_CustomColumn("discount4", prereservaLine.get_Value("discount4"));
									orderLine.set_CustomColumn("discount5", prereservaLine.get_Value("discount5"));
									orderLine.setLineNetAmt();
									if (!orderLine.save()) {
										// Generar Aviso
										MProduct prod = new MProduct(getCtx(), orderLine.getM_Product_ID(), get_TrxName());
										listErrorLinea.add("\n");
										listErrorLinea.add("Linea de documento: " + orderLine.getLine() + " - Producto: " + prod.getValue() + " - Cant.:" + orderLine.getQtyEntered() + " - Disponible: " + disponible);
									}
								}
							}
							if (listErrorLinea.size() > 0) {
								String asunto = "Problema al inserta linea desde Preventa de transito " + new Timestamp(System.currentTimeMillis());
								StringBuffer cuerpo = new StringBuffer();
								cuerpo.append(order.getC_DocType().getName() + " " + order.getDocumentNo());
								for (String error : listErrorLinea) {
									cuerpo.append(error);
								}
								mapError.put(asunto, cuerpo.toString());
							}
							// Guarda registro
							log.log(Level.SEVERE, "", "guardar registro ov_documentos_preventa...");
							insertDocumentosPreventa(prereserva.getOV_Prereserva_ID(), order.getC_Order_ID(), order_ID, "O");
						
							order.setDocAction("CO");
							if (order.processIt("CO"))
								order.save();
							
						}
						// 1000572: PreVenta - Reserva Física
						else if (prereserva.getC_DocType_ID() == 1000572) {
							// Buscar reservas fisicas de Cliente/Producto
							System.out.println("Lineas... " + prereserva.getLines().length);
							log.log(Level.SEVERE, "", "Lineas... " + prereserva.getLines().length);
							
							// Buscar reservas fisicas de Cliente, Vendedor y Producto
							List<MRequisitionLine> listReqLine = new ArrayList<MRequisitionLine>();
							for (MPrereservaLine preLine : prereserva.getLines()) {
								StringBuffer sql = new StringBuffer();
								sql.append("SELECT rl.M_RequisitionLine_ID"
										+ " FROM M_Requisition r, M_RequisitionLine rl"
										+ " WHERE r.M_Requisition_ID = rl.M_Requisition_ID"
										+ " AND r.C_BPartner_ID = ?"
										+ " AND r.AD_User_ID = ?"
										+ " AND rl.M_Product_ID = ?"
										+ " AND rl.Liberada = 'N'"
										+ " GROUP BY rl.M_RequisitionLine_ID");
								PreparedStatement pst = DB.prepareStatement(sql.toString(), get_TrxName());
								pst.setObject(1, prereserva.getC_BPartner_ID());
								pst.setObject(2, prereserva.getSalesRep_ID());
								pst.setObject(3, preLine.getM_Product_ID());
								ResultSet rs = pst.executeQuery();
								while (rs.next()) {
									listReqLine.add(new MRequisitionLine(Env.getCtx(), rs.getInt("M_RequisitionLine_ID"), get_TrxName()));
								}
								rs.close();
								pst.close();
							}
							
							// Crear Nueva Reserva Fisica
							System.out.println("Crear Reserva Fisica...");
							log.log(Level.SEVERE, "", "Crear Reserva Fisica...");
							MRequisition newRequisition = new MRequisition(Env.getCtx(), 0, get_TrxName());
							newRequisition.setC_DocType_ID(1000111); // 1000111: Reserva Fisica
							newRequisition.set_CustomColumn("C_BPartner_ID", prereserva.getC_BPartner_ID());
							newRequisition.set_CustomColumn("C_BPartner_Location_ID", prereserva.getC_BPartner_Location_ID());
							newRequisition.setPriorityRule("5");
							newRequisition.setDateDoc(prereserva.getDateDoc());
							newRequisition.setDateRequired(prereserva.getDateRequired());
							newRequisition.setM_Warehouse_ID(prereserva.getM_Warehouse_ID());
							newRequisition.setM_PriceList_ID(prereserva.getM_PriceList_ID());
							newRequisition.setAD_User_ID(prereserva.getSalesRep_ID());
							newRequisition.set_CustomColumn("IsSotrx", "Y");
							newRequisition.set_CustomColumn("OVERWRITEREQUISITION", prereserva.get_Value("OVERWRITEREQUISITION"));
							newRequisition.set_CustomColumn("ov_prereserva_id", prereserva.getOV_Prereserva_ID());
							if (newRequisition.save()) {
								int i = 10;
								List<String> listErrorLinea = new ArrayList<String>();
								for (MPrereservaLine preLine : prereserva.getLines()) {
									int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(920,'N') from c_charge where c_charge_ID=1000010"));
									List<MRequisitionLine> listLineReq = buscarReqLineProd(listReqLine, preLine.getM_Product_ID());
									if (listLineReq == null) {
										StringBuffer sqlInsert = new StringBuffer ("INSERT"
				                        		+ " INTO M_RequisitionLine (M_Requisition_ID, M_RequisitionLine_ID, Line, M_Product_ID, Qty,"
				                        		+ " QtyReserved, QtyUsed, AD_Client_ID, AD_Org_ID, Created, CreatedBy, IsActive, Updated, UpdatedBy, C_UOM_ID, PriceActual)"
				                        		+ " VALUES ("+newRequisition.getM_Requisition_ID()+","+rl_id+","+ i +","+preLine.getM_Product_ID()+","+preLine.getQty()+","
				                        		+ preLine.getQty()+",0,1000000,1000000,sysdate,100,'Y', sysdate, 100,"+preLine.getC_UOM_ID()+","+new BigDecimal(preLine.get_ValueAsInt("PriceEntered"))+")");
				                        if (DB.executeUpdate(sqlInsert.toString(), get_TrxName()) == -1) {
				                        	// Generar Aviso
				                        	BigDecimal disponible = null;
											String sql = "SELECT " + //qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001)"+
													" COALESCE ( "+
												      "         (SELECT SUM (s.qtyonhand) "+
												       "           FROM rv_storage s "+
												        "         WHERE     s.M_Product_ID = p.m_product_id "+
												         "              AND s.m_warehouse_id IN (1000001, 1000010) "+
												          "             AND s.isactive = 'Y'), "+
												           "    0) "+
												          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
												         "         FROM C_orderline ol2      "+
												          "             INNER JOIN C_Order o2  "+
												           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
												            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
												             "          AND o2.m_warehouse_id = 1000001 "+
												              "         AND o2.saldada <> 'Y' "+
												               "        AND o2.docstatus IN ('IP', 'CO', 'CL') "+
												                "       AND o2.issotrx = 'Y' "+
												                 "      AND o2.c_doctypetarget_ID NOT IN "+
												                  "            (1000110, 1000048, 1000568)) "+
												            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
												             "     FROM M_Requisitionline rl     "+
												              "         INNER JOIN M_Requisition r  "+
												               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
												                " WHERE     rl.M_Product_ID = p.m_product_id  "+
												                 "      AND r.m_warehouse_id = 1000001  "+
												                  "     AND r.docstatus IN ('CO', 'CL')  "+
												                   "    AND r.issotrx = 'Y'))  "+
													" as disponible "
													+ "FROM M_product p " 
													+ "WHERE p.m_product_ID = "	+	+ preLine.getM_Product_ID();
											PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
											ResultSet rsps = pstmtps.executeQuery();
											if (rsps.next()) {
												disponible = rsps.getBigDecimal("disponible");
											}
											
											// Guardar disponible en linea de preventa
											preLine.set_CustomColumn("DISPONIBLE", disponible);
											preLine.save();
											
											listErrorLinea.add("\n");
											MProduct prod = new MProduct(getCtx(), preLine.getM_Product_ID(), get_TrxName());
											listErrorLinea.add("Linea de documento: " + i + " - Producto: " + prod.getValue() + " - Cant.:" + preLine.getQty() + " - Disponible: " + disponible);
				                        }
				                        commitEx();
									} else {
										BigDecimal qty = BigDecimal.ZERO;
										BigDecimal qtyUsed = BigDecimal.ZERO;
										for (MRequisitionLine reqLine : listLineReq) {
											qty = qty.add(reqLine.getQty());
											qtyUsed = qtyUsed.add(new BigDecimal(reqLine.get_ValueAsInt("QtyUsed")));
										}
										BigDecimal newQty = qty.subtract(qtyUsed);
										newQty = newQty.add(preLine.getQty());
										StringBuffer sqlInsert = new StringBuffer ("INSERT"
				                        		+ " INTO M_RequisitionLine (M_Requisition_ID, M_RequisitionLine_ID, Line, M_Product_ID, Qty,"
				                        		+ " QtyReserved, QtyUsed, AD_Client_ID, AD_Org_ID, Created, CreatedBy, IsActive, Updated, UpdatedBy, C_UOM_ID, PriceActual)"
				                        		+ " VALUES ("+newRequisition.getM_Requisition_ID()+","+rl_id+","+ i +","+preLine.getM_Product_ID()+","+newQty+","
				                        		+ newQty+",0,1000000,1000000,sysdate,100,'Y', sysdate, 100,"+preLine.getC_UOM_ID()+","+new BigDecimal(preLine.get_ValueAsInt("PriceEntered"))+")");
				                        if (DB.executeUpdate(sqlInsert.toString(), get_TrxName()) == -1) {
				                        	// Generar Aviso
				                        	BigDecimal disponible = null;
											String sql = "SELECT " + //qtyavailableofb(p.m_product_ID,1000010) + qtyavailableofb(p.m_product_ID,1000001)"+
													" COALESCE ( "+
												      "         (SELECT SUM (s.qtyonhand) "+
												       "           FROM rv_storage s "+
												        "         WHERE     s.M_Product_ID = p.m_product_id "+
												         "              AND s.m_warehouse_id IN (1000001, 1000010) "+
												          "             AND s.isactive = 'Y'), "+
												           "    0) "+
												          " - (  (SELECT COALESCE (SUM (ol2.qtyreserved), 0)      "+
												         "         FROM C_orderline ol2      "+
												          "             INNER JOIN C_Order o2  "+
												           "               ON (ol2.C_ORDER_ID = o2.c_order_ID)  "+
												            "     WHERE     ol2.M_Product_ID = p.m_product_id "+
												             "          AND o2.m_warehouse_id = 1000001 "+
												              "         AND o2.saldada <> 'Y' "+
												               "        AND o2.docstatus IN ('IP', 'CO', 'CL') "+
												                "       AND o2.issotrx = 'Y' "+
												                 "      AND o2.c_doctypetarget_ID NOT IN "+
												                  "            (1000110, 1000048, 1000568)) "+
												            " + (SELECT COALESCE (SUM (rl.qtyreserved), 0)     "+
												             "     FROM M_Requisitionline rl     "+
												              "         INNER JOIN M_Requisition r  "+
												               "           ON (rl.M_Requisition_ID = r.M_Requisition_ID)  "+
												                " WHERE     rl.M_Product_ID = p.m_product_id  "+
												                 "      AND r.m_warehouse_id = 1000001  "+
												                  "     AND r.docstatus IN ('CO', 'CL')  "+
												                   "    AND r.issotrx = 'Y'))  "+
													" as disponible "
													+ "FROM M_product p " 
													+ "WHERE p.m_product_ID = "		+ preLine.getM_Product_ID();
											PreparedStatement pstmtps = DB.prepareStatement(sql, get_TrxName());
											ResultSet rsps = pstmtps.executeQuery();
											if (rsps.next()) {
												disponible = rsps.getBigDecimal("disponible");
											}
											
											// Guardar disponible en linea de preventa
											preLine.set_CustomColumn("DISPONIBLE", disponible);
											preLine.save();
											
											listErrorLinea.add("\n");
											MProduct prod = new MProduct(getCtx(), preLine.getM_Product_ID(), get_TrxName());
											listErrorLinea.add("Linea de documento: " + i + " - Producto: " + prod.getValue() + " - Cant.:" + newQty + " - Disponible: " + disponible);
				                        }
				                        commitEx();
									}
			                        commitEx();
			                        i += 10;
			                        // Liberar lineas
			                        if (listLineReq != null) {
			                        	for (MRequisitionLine reqLine : listLineReq) {
				                        	log.log(Level.SEVERE, "", "3 - Guardando cantidad reservada...");
				                        	reqLine.set_CustomColumn("oldqtyreserved", new BigDecimal(reqLine.get_ValueAsInt("QtyReserved")));
											reqLine.set_CustomColumn("oldqtyused", new BigDecimal(reqLine.get_ValueAsInt("QtyUsed")));
											reqLine.set_CustomColumn("oldqty", new BigDecimal(reqLine.get_ValueAsInt("Qty")));
											if (!reqLine.save())
												log.log(Level.SEVERE, "", "4 - No se pudo guardar cantidad reservada...");
											
											// se cierra reserva fisica anterior
											log.log(Level.SEVERE, "", "5 - Cerrar Reserva Fisica...");
											DB.executeUpdate("UPDATE M_RequisitionLine "
													+ " SET Qty = QtyUsed, QtyReserved = 0, liberada = 'Y', ov_requisitionline_id = "+ rl_id +", Description = 'La actual reserva fisica fue reemplazada por una nueva reserva física con el Nro " + newRequisition.getDocumentNo() + "' "
													+ " WHERE M_RequisitionLine_ID = "+reqLine.getM_RequisitionLine_ID(), get_TrxName());
											commitEx();
				                        }
			                        }
								}
								if (listErrorLinea.size() > 0) {
									String asunto = "Problema al inserta linea desde Preventa de transito " + new Timestamp(System.currentTimeMillis());
									StringBuffer cuerpo = new StringBuffer();
									cuerpo.append(newRequisition.getC_DocType().getName() + " " + newRequisition.getDocumentNo());
									for (String error : listErrorLinea) {
										cuerpo.append(error);
									}
									mapError.put(asunto, cuerpo.toString());
								}
							}

							// Guarda registro
							log.log(Level.SEVERE, "", "guardar registro ov_documentos_preventa...");
							insertDocumentosPreventa(prereserva.getOV_Prereserva_ID(), newRequisition.getM_Requisition_ID(), order_ID, "R");
							
							newRequisition.setDocAction("CO");
							if (newRequisition.processIt("CO")) {
								newRequisition.save();
							} else {
								// Enviar aviso
								MClient M_Client = new MClient(getCtx(),get_TrxName());
								String correoTo = "raranda@comten.cl";
								EMail email = M_Client.createEMail(correoTo, "Problema al Completar documento desde Preventa de transito", newRequisition.getC_DocType().getName() + " " + newRequisition.getDocumentNo(), true);
								EMail.SENT_OK.equals(email.send());
							}
							
						}
						// 1000573: PreVenta - MultiRut
						else if (prereserva.getC_DocType_ID() == 1000573) {
							MRequisition req = new MRequisition(getCtx(), prereserva.getM_MRequisition_ID(), get_TrxName());
							for (MPrereservaLine preLine : prereserva.getLines()) {
								int M_RequisitionLine_ID = DB.getSQLValue(get_TrxName(), "SELECT MAX(M_RequisitionLine_ID) FROM M_RequisitionLine WHERE liberada = 'N' AND M_Requisition_ID = "+ req.getM_Requisition_ID() +" AND M_Product_ID = " + preLine.getM_Product_ID());
								if (M_RequisitionLine_ID > 0) {
									StringBuffer sql = new StringBuffer("UPDATE M_RequisitionLine"
											+ " SET QtyReserved = QtyReserved + " + preLine.getQty() + ","
											+ " 	Qty = Qty + " + preLine.getQty()
											+ " WHERE M_RequisitionLine_ID = " + M_RequisitionLine_ID);
									DB.executeUpdate(sql.toString(), get_TrxName());
									commitEx();
								} else {
									int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(920,'N') from c_charge where c_charge_ID=1000010"));
			                        StringBuffer sql = new StringBuffer ("Insert "
			                        		+ "into m_requisitionline (m_requisition_ID,m_requisitionline_id,line,m_product_ID,qty,qtyreserved,ad_Client_ID,ad_org_ID,created,createdby,isactive,updated,updatedby,QTYUSED,C_UOM_ID)"
			                        		+ " values("+req.getM_Requisition_ID()+","+rl_id+",10,"+preLine.getM_Product_ID()+","+preLine.getQty()+","+preLine.getQty()+",1000000,1000000,sysdate,100,'Y', sysdate, 100,0,"+preLine.getC_UOM_ID()+")");
			                        DB.executeUpdate(sql.toString(), get_TrxName());
			                        commitEx();
								}
							}
							// Guarda registro
							log.log(Level.SEVERE, "", "guardar registro ov_documentos_preventa...");
							insertDocumentosPreventa(prereserva.getOV_Prereserva_ID(), req.getM_Requisition_ID(), order_ID, "R");
						}
//					}
			}
			if (mapError.size() > 0) {
				StringBuffer cuerpo = new StringBuffer();
				for (String mensaje : mapError.keySet()) {
					cuerpo.append(mensaje);
					cuerpo.append(mapError.get(mensaje));
				}
				MClient M_Client = new MClient(getCtx(),get_TrxName());
				String correoTo = "raranda@comten.cl";
				EMail email = M_Client.createEMail(correoTo, "Problema al inserta linea desde Preventa de transito ", cuerpo.toString(), true);
				EMail.SENT_OK.equals(email.send());
				
				EMail email2 = M_Client.createEMail("icastroruz@gmail.com", "Problema al inserta linea desde Preventa de transito ", cuerpo.toString(), true);
				EMail.SENT_OK.equals(email2.send());
			}
			// Enviar Aviso
		}
		
		} catch (Exception e) {
			log.log(Level.SEVERE, "", "Error al procesar preventa "+ e.getMessage());
		}
//		System.out.println("Ordenes... " + listaPrereservas.length);
		
		return "Preventas Procesadas";
	}
	
	private List<MRequisitionLine> buscarReqLineProd(List<MRequisitionLine> listReqLine, int m_Product_ID) {
		List<MRequisitionLine> listRet = new ArrayList<MRequisitionLine>();
		for (MRequisitionLine reqLine : listReqLine) {
			if (reqLine.getM_Product_ID() == m_Product_ID)
				listRet.add(reqLine);
		}
		return listRet.size()<1?null:listRet;
	}

	private int getRequisitionLineByProduct(int mProductID, int mRequisitionID) {
		int mRequisitionLineID = DB.getSQLValue(get_TrxName(), "SELECT M_RequisitionLine_ID"
				+ " FROM M_RequisitionLine"
				+ " WHERE M_Requisition_ID=" + mRequisitionID
				+ " AND M_Product_ID=" + mProductID);
		return mRequisitionLineID;
	}
	
	private void insertDocumentosPreventa(int preventaID, int documentoID, int orderOrigenID, String tipo) {
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO ov_documentos_preventa"
					+ " (ov_prereserva_id, c_order_origen_id, c_order_id, m_requisition_id )"
					+ " VALUES (?, ?, ?, ?)");
			PreparedStatement pst = DB.prepareStatement(sql.toString(), get_TrxName());
		
			pst.setObject(1, preventaID);
			pst.setObject(2, orderOrigenID);
			pst.setObject(3, tipo.equals("O")?documentoID:null);
			pst.setObject(4, tipo.equals("R")?documentoID:null);
			
			pst.execute();
			
			commitEx();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
