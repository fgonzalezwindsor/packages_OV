package org.openvia.process;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.model.MInOut;
import org.compiere.model.MLlegada;
import org.compiere.model.MOrder;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.openvia.process.ApiInacatalog.ConexioDBInaCatalog;

public class AvisoSemaforo extends SvrProcess {
	
	private int 	p_OV_Llegada_ID = 0;
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 1000000;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 1000000;
	ConexioDBInaCatalog connInacatalog = new ConexioDBInaCatalog();
	
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
		/*
		 * si semaforo está en rojo por mas de 3 horas se envia aviso
		 */
		Connection conn = connInacatalog.openConection();
		
		return "Llegada editable.";
	}
	
	class ConexioDBInaCatalog {
		private Connection openConection() throws Exception {
			Connection conn = null;
			conn = DriverManager.getConnection("jdbc:sqlserver://190.215.113.91:1433;database=inaSAM;user=Windsor;password=Windsor;loginTimeout=30;"); 
			return conn;
		}
		private void closeConection(Connection conn) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
