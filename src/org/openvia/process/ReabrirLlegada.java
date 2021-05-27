package org.openvia.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.compiere.model.MInOut;
import org.compiere.model.MLlegada;
import org.compiere.model.MOrder;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class ReabrirLlegada extends SvrProcess {
	
	private int 	p_OV_Llegada_ID = 0;
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
		p_OV_Llegada_ID = getRecord_ID();
	}	//	prepare

	@Override
	protected String doIt() throws Exception {
		String ret = "";
		log.info("OV_Llegada_ID=" + p_OV_Llegada_ID);
		if (p_OV_Llegada_ID == 0)
			throw new IllegalArgumentException("No Llegada");

		// Se valida que llegada no tenga recepciones
		String sql = "select * "
				+ "from m_inout "
				+" where ov_llegada_id = ? ";
				//+ "and docstatus='CO'";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, p_OV_Llegada_ID);
			rs = pstmt.executeQuery();		
			if (rs.next()){
				return "No se puede reabrir Llegada por recepciones asociadas.";
			} else {
				// Reabrir
				log.info("reabrir llegada");
				MLlegada llegada = new MLlegada(getCtx(), p_OV_Llegada_ID, null);
				llegada.setDocStatus("DR");
				llegada.setDocAction("CO");
				llegada.setProcessed(false);
				if (llegada.save())
					DB.executeUpdate("UPDATE OV_LlegadaLine SET Processed = 'N' WHERE OV_Llegada_ID = " + p_OV_Llegada_ID, get_TrxName());
				else
					return "Error al reabrir Llegada.";
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, sql, e);
		} finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		
		return "Llegada editable.";
	}
}
