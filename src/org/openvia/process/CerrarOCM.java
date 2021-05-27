package org.openvia.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MOrder;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class CerrarOCM extends SvrProcess {
	
	private int 	p_C_Order_ID = 0;
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 1000000;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 1000000;
	
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
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		p_C_Order_ID = getRecord_ID();
	}	//	prepare

	@Override
	protected String doIt() throws Exception {
		MOrder order = new MOrder(Env.getCtx(), p_C_Order_ID, null);
		if ((Boolean)order.get_Value("ov_ocmcerrada"))
			throw new AdempiereException("Orden de Compra Madre Cerrada.");
		String ret = "";
		log.info("C_Order_ID=" + p_C_Order_ID);
		if (p_C_Order_ID == 0)
			throw new IllegalArgumentException("No OCM");

		// Mover recibos a bodega abastecimiento
		String sql = "SELECT io.M_InOut_ID"
				+ " FROM OV_Llegada ll, M_InOut io"
				+ " WHERE ll.OV_Llegada_ID = io.OV_Llegada_ID"
				+ " AND ll.C_Order_ID = ?"
				+ " AND io.DocStatus = 'CO'";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, p_C_Order_ID);
			rs = pstmt.executeQuery();		
			while(rs.next()){
				MInOut out = new MInOut(getCtx(), rs.getInt("M_InOut_ID"), get_TrxName());
				out.setM_Warehouse_ID(1000010); // Abastecimiento
				String sqlUpd = "UPDATE M_Locator"
						+ " SET M_WareHouse_ID = 1000010"
						+ " WHERE M_Locator_ID IN (SELECT M_Locator_ID FROM M_InOutLine WHERE M_InOut_ID = " + rs.getInt("M_InOut_ID") + " GROUP BY M_Locator_ID)";
				DB.executeUpdate(sqlUpd, get_TrxName());
				out.save();
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, sql, e);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//
			
		//MOrder order = new MOrder(Env.getCtx(), p_C_Order_ID, null);
		order.set_CustomColumn("ov_ocmcerrada", "Y");
		if (!order.save())
			throw new AdempiereException("Error al Cerrar Orden de Compra Madre.");
		else 
			ret = "Orden de Compra Madre cerrada exitosamente.";
		return ret;
	}
}
