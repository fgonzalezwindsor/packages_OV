package org.openvia.process;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.openvia.inacatalog.iagentes.IAgentesImp;
import org.openvia.inacatalog.iagentes.I_iAgentes;
import org.openvia.inacatalog.itarifaslins.ITarifasLinsModel;

public class LimpiaInaCatalogPedCentralsCobros extends SvrProcess {
	
	I_iAgentes apiAgentes = new IAgentesImp();	
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
			else if (name.equals("DeleteOldImported"))
			;//	m_deleteOldImported = "Y".equals(para[i].getParameter());
			else if (name.equals("DocAction"))
		;//		m_docAction = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare

	@Override
	protected String doIt() throws Exception {
		String ret = "";
		if (limpiarInaCatalog()) {
			ret = "Tablas InaCatalog limpiadas exitosamente";
		} else {
			ret = "Error al limpiar InaCatalog";
		}
		return ret;
	}
	
	class ConexioDBInaCatalog {
		Connection conn;
		public ConexioDBInaCatalog() {
			try {
				String connectionUrl =
		                "jdbc:sqlserver://190.215.113.91:1433;"
		                        + "database=inaSAM;"
		                        + "user=Windsor;"
		                        + "password=Windsor;"
		                        + "loginTimeout=30;";
				conn = DriverManager.getConnection(connectionUrl);
				System.out.println("Conectado.");
			} catch (SQLException ex) {
				System.out.println("Error en Conexion DB InaCatalog. " + ex.toString());
			}
		}
	}
	
	private boolean limpiarInaCatalog() throws SQLException {
		PreparedStatement pst = null;
		ConexioDBInaCatalog conexion = new ConexioDBInaCatalog();
		
		// delete de tablas
		pst = conexion.conn.prepareStatement("DELETE FROM iPedidosCentralLin WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iPedidosCentral WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iCobros WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		return true;
	}

}
