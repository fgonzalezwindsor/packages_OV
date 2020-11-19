package org.openvia.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MInOut;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPrereserva;
import org.compiere.model.MPrereservaLine;
import org.compiere.model.MRequisition;
import org.compiere.model.MRequisitionLine;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

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
		int[] listaPrereservas = MPrereserva.getAllIDs("OV_Prereserva", "C_Order_ID="+order_ID, get_TrxName());
		System.out.println("Ordenes... " + listaPrereservas.length);
		StringBuffer sqlDoctosPreventa = new StringBuffer("SELECT Count(*) FROM OV_Documentos_preventa WHERE C_Order_origen_ID = " + order_ID);
		int doctosPreventa = DB.getSQLValue(get_TrxName(), sqlDoctosPreventa.toString());
		if (doctosPreventa == 0) {
			for (int id : listaPrereservas) {
				MPrereserva prereserva = new MPrereserva(getCtx(), id, get_TrxName());
				// 1000571: PreVenta - Nota de Venta
				if (prereserva.getC_DocType_ID() == 1000571) {
					MOrder order = new MOrder(getCtx(), 0, get_TrxName());
					order.setC_BPartner_ID(prereserva.getC_BPartner_ID());
//					order.setC_BPartner_Location_ID(prereserva.getC_BPartner_Location_ID());
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
					order.saveEx();
					for (MPrereservaLine prereservaLine : prereserva.getLines()) {
						MOrderLine orderLine = new MOrderLine(order);
						orderLine.setM_Product_ID(prereservaLine.getM_Product_ID());
						orderLine.setQtyEntered(prereservaLine.getQty());
						orderLine.setQtyOrdered(prereservaLine.getQty());
						orderLine.setPriceEntered(prereservaLine.getPriceActual());
						orderLine.setLineNetAmt();
						orderLine.saveEx();
					}
					// Guarda registro
					log.log(Level.SEVERE, "", "guardar registro ov_documentos_preventa...");
					insertDocumentosPreventa(prereserva.getOV_Prereserva_ID(), order.getC_Order_ID(), order_ID, "O");
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
					if (newRequisition.save()) {
						for (MPrereservaLine preLine : prereserva.getLines()) {
							List<MRequisition> listaRequisition = new ArrayList<MRequisition>();
							StringBuffer sql = new StringBuffer();
							sql.append("SELECT r.M_Requisition_ID"
									+ " FROM M_Requisition r, M_RequisitionLine rl"
									+ " WHERE r.M_Requisition_ID = rl.M_Requisition_ID"
									+ " AND r.C_BPartner_ID = ?"
									+ " AND rl.M_Product_ID = ?");
							PreparedStatement pst = DB.prepareStatement(sql.toString(), get_TrxName());
							pst.setObject(1, prereserva.getC_BPartner_ID());
							pst.setObject(2, preLine.getM_Product_ID());
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
								for (MRequisitionLine reqLineEnd : listaReqLine) {
									if (preLine.getM_Product_ID() == reqLineEnd.getM_Product_ID()) {
										int mRequisitionLineID = getRequisitionLineByProduct(reqLineEnd.getM_Product_ID(), newRequisition.getM_Requisition_ID());
										if (mRequisitionLineID != -1) {
											MRequisitionLine newRequisitionLine = new MRequisitionLine(Env.getCtx(), mRequisitionLineID, get_TrxName());
											newRequisitionLine.setQty(newRequisitionLine.getQty().add(preLine.getQty()));
											newRequisitionLine.set_CustomColumn("Qtyreserved", new BigDecimal(newRequisitionLine.get_ValueAsInt("Qtyreserved")).add(preLine.getQty()));
											newRequisitionLine.set_CustomColumn("QtyUsed", new BigDecimal(newRequisitionLine.get_ValueAsInt("QtyUsed")));
											if (!newRequisitionLine.save())
												log.log(Level.SEVERE, "", "1 - No se pudo guardar linea...");
										} else {
											MRequisitionLine newRequisitionLine = new MRequisitionLine(newRequisition);
											newRequisitionLine.setM_Product_ID(preLine.getM_Product_ID());
											newRequisitionLine.setC_UOM_ID(preLine.getC_UOM_ID());
											newRequisitionLine.setQty(reqLineEnd.getQty().add(preLine.getQty()));
											newRequisitionLine.set_CustomColumn("QtyReserved", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyReserved")).add(preLine.getQty()));
											newRequisitionLine.set_CustomColumn("QtyUsed", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyUsed")));
											if (!newRequisitionLine.save())
												log.log(Level.SEVERE, "", "2 - No se pudo guardar linea...");
										}
										// se cierra reserva fisica anterior
										log.log(Level.SEVERE, "", "3 - Guardando cantidad reservada...");
										reqLineEnd.set_CustomColumn("oldqtyreserved", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyReserved")));
										if (!reqLineEnd.save())
											log.log(Level.SEVERE, "", "4 - No se pudo guardar cantidad reservada...");
										
										log.log(Level.SEVERE, "", "5 - Cerrar Reserva Fisica...");
										DB.executeUpdate("UPDATE M_RequisitionLine SET QtyUsed = Qty, oldqtyreserved = QtyReserved, QtyReserved = 0, liberada = 'Y' " +
												" WHERE M_RequisitionLine_ID = "+reqLineEnd.getM_RequisitionLine_ID(), get_TrxName());
										
										/*reqLineEnd.set_CustomColumn("LIBERADA", "Y");
										reqLineEnd.set_CustomColumn("QtyUsed", reqLineEnd.getQty());
										reqLineEnd.set_CustomColumn("oldqtyreserved", new BigDecimal(reqLineEnd.get_ValueAsInt("QtyReserved")));
										reqLineEnd.set_CustomColumn("QtyReserved", BigDecimal.ZERO);
										if (!reqLineEnd.save())
											log.log(Level.SEVERE, "", "4 - No se pudo cerrar linea...");
										*/
									}
								}
							} else {
								MRequisitionLine reqLine = new MRequisitionLine(newRequisition);
								reqLine.setM_Product_ID(preLine.getM_Product_ID());
								reqLine.setQty(preLine.getQty());
								reqLine.set_CustomColumn("QtyReserved", preLine.getQty());
								if (!reqLine.save())
									log.log(Level.SEVERE, "", "6 - No se pudo guardar linea...");
							}
						}
					}
					/*
					newRequisition.setDocAction("CO");
					if (newRequisition.processIt("CO"))
						newRequisition.save();
					*/
					// Guarda registro
					log.log(Level.SEVERE, "", "guardar registro ov_documentos_preventa...");
					insertDocumentosPreventa(prereserva.getOV_Prereserva_ID(), newRequisition.getM_Requisition_ID(), order_ID, "R");
					
					/*
					MRequisition req = new MRequisition(getCtx(), prereserva.getM_Requisition_ID(), get_TrxName());
					for (MPrereservaLine preLine : prereserva.getLines()) {
						boolean existeProducto = false;
						for (MRequisitionLine reqLine : req.getLines()) {
							if (preLine.getM_Product_ID() == reqLine.getM_Product_ID()) {
								reqLine.setQty(reqLine.getQty().add(preLine.getQty()));
								reqLine.set_CustomColumn("QtyReserved", new BigDecimal(reqLine.get_Value("QtyReserved").toString()).add(preLine.getQty()));
								reqLine.saveEx();
								existeProducto = true;
								break;
							}
						}
						if (!existeProducto) {
							MRequisitionLine reqLine = new MRequisitionLine(req);
							reqLine.setM_Product_ID(preLine.getM_Product_ID());
							reqLine.setQty(preLine.getQty());
							reqLine.set_CustomColumn("QtyReserved", preLine.getQty());
							reqLine.saveEx();
						}
						
					}
					*/
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
			}
		}
		
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
