package org.openvia.process;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
	
	int inOut_ID = 0;

	protected void prepare() {
		inOut_ID = getRecord_ID();
	}

	protected String doIt() throws Exception {
		MInOut inout = new MInOut(Env.getCtx(), inOut_ID, get_TrxName());
		int[] listaPrereservas = MPrereserva.getAllIDs("OV_Prereserva", "C_Order_ID="+inout.getC_Order_ID(), get_TrxName());
		for (int id : listaPrereservas) {
			MPrereserva prereserva = new MPrereserva(getCtx(), id, get_TrxName());
			// 1000571: PreVenta - Nota de Venta
			if (prereserva.getC_DocType_ID() == 1000571) {
				MOrder order = new MOrder(getCtx(), 0, get_TrxName());
				order.setC_BPartner_ID(prereserva.getC_BPartner_ID());
				order.setC_BPartner_Location_ID(prereserva.getC_BPartner_Location_ID());
				order.setC_DocType_ID(1000030); // 1000030: Orden de Venta
				order.setC_DocTypeTarget_ID(1000030);
				order.setIsSOTrx(true);
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
			}
			// 1000572: PreVenta - Reserva Física
			else if (prereserva.getC_DocType_ID() == 1000572) {
				/*StringBuffer sql = new StringBuffer();
				sql.append("SELECT * ");
				sql.append(" FROM M_Requisition");
				sql.append(" WHERE C_BPartner_ID=" + prereserva.getC_BPartner_ID());
				sql.append(" AND C_DocType_id = 1000111");
				sql.append(" AND DocStatus IN ('CO','CL')");
				sql.append(" ORDER BY Created DESC");
				PreparedStatement pstmt = null;
				pstmt = DB.prepareStatement (sql.toString(), get_TrxName());
				ResultSet rs = pstmt.executeQuery ();*/
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
			}
			// 1000573: PreVenta - MultiRut
			else if (prereserva.getC_DocType_ID() == 1000573) {
				MRequisition req = new MRequisition(getCtx(), prereserva.getM_MRequisition_ID(), get_TrxName());
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
			}
		}
		
		return "Preventas Procesadas";
	}

}
