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
		StringBuffer sqlDoctosPreventa = new StringBuffer("SELECT Count(*) FROM OV_Documentos_preventa WHERE C_Order_origen_ID = " + order_ID);
		int doctosPreventa = DB.getSQLValue(get_TrxName(), sqlDoctosPreventa.toString());
		if (doctosPreventa == 0) {
			PreparedStatement pstPre = DB.prepareStatement("SELECT OV_Prereserva_ID FROM OV_Prereserva WHERE C_Order_ID = " + order_ID + " Order By C_DocType_ID", get_TrxName());
			ResultSet res = pstPre.executeQuery();
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
							order.saveEx();
							for (MPrereservaLine prereservaLine : prereserva.getLines()) {
								MOrderLine orderLine = new MOrderLine(order);
								orderLine.setM_Product_ID(prereservaLine.getM_Product_ID());
								orderLine.setQtyEntered(prereservaLine.getQty());
								orderLine.setQtyOrdered(prereservaLine.getQty());
								orderLine.setPriceEntered(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
								orderLine.setPriceActual(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
								orderLine.setPriceList(new BigDecimal(prereservaLine.get_Value("PriceList").toString()));
								orderLine.set_CustomColumn("discount2", prereservaLine.get_Value("discount2"));
								orderLine.set_CustomColumn("discount3", prereservaLine.get_Value("discount3"));
								orderLine.set_CustomColumn("discount4", prereservaLine.get_Value("discount4"));
								orderLine.set_CustomColumn("discount5", prereservaLine.get_Value("discount5"));
								orderLine.setLineNetAmt();
								orderLine.saveEx();
								
								/*
								StringBuffer s_sql = new StringBuffer();
								// C_DocType_id = 1000111 Reserva Fisica
								s_sql.append("SELECT rl.M_RequisitionLine_ID, rl.QtyReserved")
										.append(" FROM M_Requisition r")
										.append(" JOIN M_RequisitionLine rl ON rl.M_Requisition_ID = r.M_Requisition_ID")
										.append(" WHERE r.C_BPartner_ID = ").append(prereserva.getC_BPartner_ID())
										.append(" AND r.C_DocType_id = 1000111")
										.append(" AND r.DocStatus IN ('CO','CL')")
										.append(" AND rl.M_Product_ID = ").append(prereservaLine.getM_Product_ID())
										.append(" AND rl.QtyReserved > 0 ")
										.append(" ORDER BY rl.Created");

								PreparedStatement pst = DB.prepareStatement(s_sql.toString(), get_TrxName());
								ResultSet rs = pst.executeQuery();
								BigDecimal cantPedido = prereservaLine.getQty();
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
										int ol_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(233,'N') from c_charge where c_charge_ID=1000010"));
										StringBuffer sqlInsert = new StringBuffer("INSERT INTO C_OrderLine (AD_Client_ID, AD_Org_ID, Created, CreatedBy, Updated,")
												.append(" UpdatedBy, IsActive, DateOrdered, C_OrderLine_ID, C_Order_ID, C_UOM_ID, M_Product_ID, QtyEntered, QtyOrdered, PriceEntered,")
												.append(" PriceActual, PriceList, discount2, discount3, discount4, discount5, LineNetAmt, M_RequisitionLine_ID, Line,")
												.append(" M_WareHouse_ID, C_Currency_ID, C_Tax_ID)")
												.append(" VALUES (1000000,1000000,sysdate,100,sysdate,100,'Y',sysdate,").append(ol_id)
												.append(",").append(order.getC_Order_ID())
												.append(",").append(prereservaLine.getC_UOM_ID())
												.append(",").append(prereservaLine.getM_Product_ID())
												.append(",").append(entry.getValue())
												.append(",").append(entry.getValue())
												/*.append(",").append(prereservaLine.getQty())
												.append(",").append(prereservaLine.getQty())/
												.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()))
												.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()))
												.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceList").toString()))
												.append(",").append(prereservaLine.get_Value("discount2"))
												.append(",").append(prereservaLine.get_Value("discount3"))
												.append(",").append(prereservaLine.get_Value("discount4"))
												.append(",").append(prereservaLine.get_Value("discount5"))
												.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()).multiply(entry.getValue()))
												//.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()).multiply(prereservaLine.getQty()))
												.append(",").append(entry.getKey())
												.append(",").append(prereservaLine.getLine())
												.append(",").append(prereserva.getM_Warehouse_ID())
												.append(",").append("228")
												.append(",").append("1000000")
												.append(")");
										DB.executeUpdate(sqlInsert.toString(), get_TrxName());
										commitEx();
										
										// Actualiza Reserva Fisica
										StringBuffer sql = new StringBuffer("UPDATE M_RequisitionLine")
												.append(" SET QtyUsed = QtyUsed + ").append(entry.getValue()).append(",")
												.append(" QtyReserved = QtyReserved - ").append(entry.getValue())
												.append(" WHERE M_RequisitionLine_ID = ").append(entry.getKey());
										DB.executeUpdate(sql.toString(), get_TrxName());
										commitEx();
										/*StringBuffer sql = new StringBuffer("UPDATE M_RequisitionLine")
												.append(" SET QtyUsed = QtyUsed + ").append(prereservaLine.getQty()).append(",")
												.append(" QtyReserved = QtyReserved - ").append(prereservaLine.getQty())
												.append(" WHERE M_RequisitionLine_ID = ").append(entry.getKey());
										DB.executeUpdate(sql.toString(), get_TrxName());
										commitEx();/
										
										/*MOrderLine orderLine = new MOrderLine(order);
										orderLine.setM_Product_ID(prereservaLine.getM_Product_ID());
										orderLine.setQtyEntered(prereservaLine.getQty());
										orderLine.setQtyOrdered(prereservaLine.getQty());
										orderLine.setPriceEntered(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
										orderLine.setPriceActual(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
										orderLine.setPriceList(new BigDecimal(prereservaLine.get_Value("PriceList").toString()));
										orderLine.set_CustomColumn("discount2", prereservaLine.get_Value("discount2"));
										orderLine.set_CustomColumn("discount3", prereservaLine.get_Value("discount3"));
										orderLine.set_CustomColumn("discount4", prereservaLine.get_Value("discount4"));
										orderLine.set_CustomColumn("discount5", prereservaLine.get_Value("discount5"));
										orderLine.setLineNetAmt();
										if (entry.getKey() == null) {
											orderLine.set_CustomColumn("M_RequisitionLine_ID", entry.getKey());
											// Actualiza Reserva Fisica
											StringBuffer sql = new StringBuffer("UPDATE M_Requisition")
													.append(" SET QtyUsed = QtyUsed + ").append(prereservaLine.getQty()).append(",")
													.append(" QtyReserved = QtyReserved - ").append(prereservaLine.getQty())
													.append(" WHERE M_RequisitionLine_ID = ").append(entry.getKey());
											DB.executeUpdate(sql.toString(), get_TrxName());
											commitEx();
										}
										orderLine.saveEx();/
									}
								} else {
									int ol_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(233,'N') from c_charge where c_charge_ID=1000010"));
									StringBuffer sqlInsert = new StringBuffer("INSERT INTO C_OrderLine (AD_Client_ID, AD_Org_ID, Created, CreatedBy, Updated,")
											.append(" UpdatedBy, IsActive, DateOrdered, C_OrderLine_ID, C_Order_ID, C_UOM_ID, M_Product_ID, QtyEntered, QtyOrdered, PriceEntered,")
											.append(" PriceActual, PriceList, discount2, discount3, discount4, discount5, LineNetAmt, Line,")
											.append(" M_WareHouse_ID, C_Currency_ID, C_Tax_ID)")
											.append(" VALUES (1000000,1000000,sysdate,100,sysdate,100,'Y',sysdate,").append(ol_id)
											.append(",").append(order.getC_Order_ID())
											.append(",").append(prereservaLine.getC_UOM_ID())
											.append(",").append(prereservaLine.getM_Product_ID())
											.append(",").append(prereservaLine.getQty())
											.append(",").append(prereservaLine.getQty())
											.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()))
											.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()))
											.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceList").toString()))
											.append(",").append(prereservaLine.get_Value("discount2"))
											.append(",").append(prereservaLine.get_Value("discount3"))
											.append(",").append(prereservaLine.get_Value("discount4"))
											.append(",").append(prereservaLine.get_Value("discount5"))
											.append(",").append(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()).multiply(prereservaLine.getQty()))
											.append(",").append(prereservaLine.getLine())
											.append(",").append(prereserva.getM_Warehouse_ID())
											.append(",").append("228")
											.append(",").append("1000000")
											.append(")");
									DB.executeUpdate(sqlInsert.toString(), get_TrxName());
									commitEx();
									
									/*
									MOrderLine orderLine = new MOrderLine(order);
									orderLine.setM_Product_ID(prereservaLine.getM_Product_ID());
									orderLine.setQtyEntered(prereservaLine.getQty());
									orderLine.setQtyOrdered(prereservaLine.getQty());
									orderLine.setPriceEntered(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
									orderLine.setPriceActual(new BigDecimal(prereservaLine.get_Value("PriceEntered").toString()));
									orderLine.setPriceList(new BigDecimal(prereservaLine.get_Value("PriceList").toString()));
									orderLine.set_CustomColumn("discount2", prereservaLine.get_Value("discount2"));
									orderLine.set_CustomColumn("discount3", prereservaLine.get_Value("discount3"));
									orderLine.set_CustomColumn("discount4", prereservaLine.get_Value("discount4"));
									orderLine.set_CustomColumn("discount5", prereservaLine.get_Value("discount5"));
									orderLine.setLineNetAmt();
									orderLine.saveEx();
									/
								}
								*/
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
								List<MRequisition> listaRequisition = new ArrayList<MRequisition>();
								StringBuffer sql = new StringBuffer();
								sql.append("SELECT r.M_Requisition_ID"
										+ " FROM M_Requisition r, M_RequisitionLine rl"
										+ " WHERE r.M_Requisition_ID = rl.M_Requisition_ID"
										+ " AND r.C_BPartner_ID = ?"
										+ " AND rl.Liberada = 'N'"
										+ " GROUP BY r.M_Requisition_ID");
								PreparedStatement pst = DB.prepareStatement(sql.toString(), get_TrxName());
								pst.setObject(1, prereserva.getC_BPartner_ID());
								ResultSet rs = pst.executeQuery();
								while (rs.next()) {
									listaRequisition.add(new MRequisition(Env.getCtx(), rs.getInt("M_Requisition_ID"), get_TrxName()));
								}
								
								if (listaRequisition.size() > 0) {
									List<MRequisitionLine> listaReqLine = new ArrayList<MRequisitionLine>();
									for (MRequisition req : listaRequisition) {
										for (MRequisitionLine reqLine : req.getLines()) {
											if (!reqLine.get_Value("liberada").equals("Y"))
												listaReqLine.add(reqLine);
										}
									}
									int i = 10;
									for (MRequisitionLine reqLineEnd : listaReqLine) {
										int reqLineID = getRequisitionLineByProduct(reqLineEnd.getM_Product_ID(), newRequisition.getM_Requisition_ID());
										if (reqLineID != -1) {
											MRequisitionLine newRequisitionLine = new MRequisitionLine(Env.getCtx(), reqLineID, get_TrxName());
											BigDecimal qty = reqLineEnd.getQty();
											BigDecimal qtyReserved = new BigDecimal(reqLineEnd.get_ValueAsInt("QtyReserved"));
											BigDecimal qtyUsed = new BigDecimal(reqLineEnd.get_ValueAsInt("QtyUsed"));
											/*
											DB.executeUpdate("UPDATE M_RequisitionLine SET Qty = Qty + "+qty+", QtyReserved = QtyReserved + " + qtyReserved + ", qtyused = qtyused + "+qtyUsed+
													" WHERE M_RequisitionLine_ID = "+newRequisitionLine.getM_RequisitionLine_ID(), get_TrxName());
											*/
											DB.executeUpdate("UPDATE M_RequisitionLine SET Qty = Qty + "+qtyReserved+", qtyused = 0 "+
													" WHERE M_RequisitionLine_ID = "+newRequisitionLine.getM_RequisitionLine_ID(), get_TrxName());
											commitEx();
										} else {
											int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(920,'N') from c_charge where c_charge_ID=1000010"));
											BigDecimal qty = reqLineEnd.getQty();
											BigDecimal qtyReserved = new BigDecimal(reqLineEnd.get_ValueAsInt("QtyReserved"));
											BigDecimal qtyUsed = new BigDecimal(reqLineEnd.get_ValueAsInt("QtyUsed"));
					                        /*
											StringBuffer sqlInsert = new StringBuffer ("INSERT"
					                        		+ " INTO M_RequisitionLine (M_Requisition_ID, M_RequisitionLine_ID, Line, M_Product_ID, Qty,"
					                        		+ " QtyReserved, QtyUsed, AD_Client_ID, AD_Org_ID, Created, CreatedBy, IsActive, Updated, UpdatedBy, C_UOM_ID)"
					                        		+ " VALUES ("+newRequisition.getM_Requisition_ID()+","+rl_id+","+ i +","+reqLineEnd.getM_Product_ID()+","+qty+","
					                        		+ qtyReserved+","+qtyUsed+",1000000,1000000,sysdate,100,'Y', sysdate, 100,"+reqLineEnd.getC_UOM_ID()+")");
					                        */
											StringBuffer sqlInsert = new StringBuffer ("INSERT"
					                        		+ " INTO M_RequisitionLine (M_Requisition_ID, M_RequisitionLine_ID, Line, M_Product_ID, Qty,"
					                        		+ " QtyReserved, QtyUsed, AD_Client_ID, AD_Org_ID, Created, CreatedBy, IsActive, Updated, UpdatedBy, C_UOM_ID)"
					                        		+ " VALUES ("+newRequisition.getM_Requisition_ID()+","+rl_id+","+ i +","+reqLineEnd.getM_Product_ID()+","+qtyReserved+","
					                        		+ 0+","+0+",1000000,1000000,sysdate,100,'Y', sysdate, 100,"+reqLineEnd.getC_UOM_ID()+")");
					                        DB.executeUpdate(sqlInsert.toString(), get_TrxName());
					                        commitEx();
										}
										
				                        i = i+10;
				                        
										/*MRequisitionLine newRequisitionLine = new MRequisitionLine(newRequisition);
										newRequisitionLine.setM_Product_ID(reqLineEnd.getM_Product_ID());
										newRequisitionLine.setC_UOM_ID(reqLineEnd.getC_UOM_ID());
										newRequisitionLine.setQty(reqLineEnd.getQty());
										newRequisitionLine.set_CustomColumn("QtyReserved", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyReserved")));
										newRequisitionLine.set_CustomColumn("QtyUsed", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyUsed")));
										if (!newRequisitionLine.save())
											log.log(Level.SEVERE, "", "2 - No se pudo guardar linea...");
										*/
										// se guardan registros anteriores (QtyReserved y QtyUsed)
										log.log(Level.SEVERE, "", "3 - Guardando cantidad reservada...");
										reqLineEnd.set_CustomColumn("oldqtyreserved", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyReserved")));
										reqLineEnd.set_CustomColumn("oldqtyused", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyUsed")));
										if (!reqLineEnd.save())
											log.log(Level.SEVERE, "", "4 - No se pudo guardar cantidad reservada...");
										
										// se cierra reserva fisica anterior
										log.log(Level.SEVERE, "", "5 - Cerrar Reserva Fisica...");
										DB.executeUpdate("UPDATE M_RequisitionLine SET Qty = QtyUsed, QtyReserved = 0, liberada = 'Y' " +
											" WHERE M_RequisitionLine_ID = "+reqLineEnd.getM_RequisitionLine_ID(), get_TrxName());
										commitEx();
									}
								}
								
								// Inserta nuevas lineas
								for (MPrereservaLine preLine : prereserva.getLines()) {
									int reqLineID = getRequisitionLineByProduct(preLine.getM_Product_ID(), newRequisition.getM_Requisition_ID());
									if (reqLineID != -1) {
										MRequisitionLine newRequisitionLine = new MRequisitionLine(Env.getCtx(), reqLineID, get_TrxName());
										BigDecimal qty = newRequisitionLine.getQty().add(preLine.getQty());
										//BigDecimal qtyReserved = new BigDecimal(newRequisitionLine.get_ValueAsInt("Qtyreserved")).add(preLine.getQty());
										BigDecimal qtyUsed = new BigDecimal(newRequisitionLine.get_ValueAsInt("QtyUsed"));
										StringBuffer sqlUpdate = new StringBuffer("UPDATE M_RequisitionLine"
												+ " SET Qty = " + qty + ","  //"+qty+","
												//+ "     Qtyreserved = "+qtyReserved+","
												+ "     QtyUsed = 0" //+qtyUsed
												+ " WHERE M_RequisitionLine_ID = "+reqLineID);
										DB.executeUpdate(sqlUpdate.toString(), get_TrxName());
				                        commitEx();
										
										/*
										newRequisitionLine.setQty(newRequisitionLine.getQty().add(preLine.getQty()));
										newRequisitionLine.set_CustomColumn("Qtyreserved", qtyReserved);
										newRequisitionLine.set_CustomColumn("QtyUsed", qtyUsed);
										if (!newRequisitionLine.save())
											log.log(Level.SEVERE, "", "1 - No se pudo guardar linea...");
										*/
									} else {
										int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(920,'N') from c_charge where c_charge_ID=1000010"));
										int line = DB.getSQLValue(get_TrxName(), "SELECT max(line) FROM M_RequisitionLine WHERE M_Requisition_ID = "+newRequisition.getM_Requisition_ID());
										StringBuffer sqlInsert = new StringBuffer ("INSERT"
				                        		+ " INTO M_RequisitionLine (M_Requisition_ID, M_RequisitionLine_ID, Line, M_Product_ID, Qty,"
				                        		+ " QtyReserved, QtyUsed, AD_Client_ID, AD_Org_ID, Created, CreatedBy, IsActive, Updated, UpdatedBy, C_UOM_ID)"
				                        		+ " VALUES ("+newRequisition.getM_Requisition_ID()+","+rl_id+","+ (line+10) +","+preLine.getM_Product_ID()+","+preLine.getQty()+","
				                        		+ 0+",0,1000000,1000000,sysdate,100,'Y', sysdate, 100,"+preLine.getC_UOM_ID()+")");
				                        DB.executeUpdate(sqlInsert.toString(), get_TrxName());
				                        commitEx();
										/*
										MRequisitionLine newRequisitionLine = new MRequisitionLine(newRequisition);
										newRequisitionLine.setM_Product_ID(preLine.getM_Product_ID());
										newRequisitionLine.setC_UOM_ID(preLine.getC_UOM_ID());
										newRequisitionLine.setQty(preLine.getQty());
										newRequisitionLine.set_CustomColumn("QtyReserved", preLine.getQty());
										if (!newRequisitionLine.save())
											log.log(Level.SEVERE, "", "2 - No se pudo guardar linea...");
										*/
									}
								}
								
							}
							// Guarda registro
							log.log(Level.SEVERE, "", "guardar registro ov_documentos_preventa...");
							insertDocumentosPreventa(prereserva.getOV_Prereserva_ID(), newRequisition.getM_Requisition_ID(), order_ID, "R");
							
							newRequisition.setDocAction("CO");
							if (newRequisition.processIt("CO"))
								newRequisition.save();
							
						}
						// 1000573: PreVenta - MultiRut
						else if (prereserva.getC_DocType_ID() == 1000573) {
							MRequisition req = new MRequisition(getCtx(), prereserva.getM_MRequisition_ID(), get_TrxName());
							for (MPrereservaLine preLine : prereserva.getLines()) {
								boolean existeProducto = false;
								for (MRequisitionLine reqLine : req.getLines()) {
									if (preLine.getM_Product_ID() == reqLine.getM_Product_ID()) {
										StringBuffer sql = new StringBuffer("UPDATE M_RequisitionLine"
												+ " SET QtyReserved = " + new BigDecimal(reqLine.get_Value("QtyReserved").toString()).add(preLine.getQty()) + ","
												+ " 	Qty = " + reqLine.getQty().add(preLine.getQty())
												+ " WHERE M_RequisitionLine_ID = " + reqLine.getM_RequisitionLine_ID());
										/*reqLine.setQty(reqLine.getQty().add(preLine.getQty()));
										reqLine.set_CustomColumn("QtyReserved", new BigDecimal(reqLine.get_Value("QtyReserved").toString()).add(preLine.getQty()));
										reqLine.saveEx();*/
										DB.executeUpdate(sql.toString(), get_TrxName());
										commitEx();
										existeProducto = true;
										break;
									}
								}
								if (!existeProducto) {
									int rl_id = Integer.parseInt(DB.getSQLValueString(null, "Select NEXTIDFUNC(920,'N') from c_charge where c_charge_ID=1000010"));
			                        StringBuffer sql = new StringBuffer ("Insert "
			                        		+ "into m_requisitionline (m_requisition_ID,m_requisitionline_id,line,m_product_ID,qty,qtyreserved,ad_Client_ID,ad_org_ID,created,createdby,isactive,updated,updatedby,QTYUSED,C_UOM_ID)"
			                        		+ " values("+req.getM_Requisition_ID()+","+rl_id+",10,"+preLine.getM_Product_ID()+","+preLine.getQty()+","+preLine.getQty()+",1000000,1000000,sysdate,100,'Y', sysdate, 100,0,"+preLine.getC_UOM_ID()+")");
			                        DB.executeUpdate(sql.toString(), get_TrxName());
			                        commitEx();
			                               
									/*MRequisitionLine reqLine = new MRequisitionLine(req);
									reqLine.setM_Product_ID(preLine.getM_Product_ID());
									reqLine.setQty(preLine.getQty());
									reqLine.set_CustomColumn("QtyReserved", preLine.getQty());
									reqLine.saveEx();*/
								}
							}
							// Guarda registro
							log.log(Level.SEVERE, "", "guardar registro ov_documentos_preventa...");
							insertDocumentosPreventa(prereserva.getOV_Prereserva_ID(), req.getM_Requisition_ID(), order_ID, "R");
						}
//					}
			}
		}
//		System.out.println("Ordenes... " + listaPrereservas.length);
		
		return "Preventas Procesadas";
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
