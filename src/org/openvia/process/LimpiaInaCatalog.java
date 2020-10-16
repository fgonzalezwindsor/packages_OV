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

public class LimpiaInaCatalog extends SvrProcess {
	
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
			if (materializarTablasAD())
				ret += " - Tablas Materializadas en ADempiere";
			else
				ret += " - Error al Materializar en ADempiere";
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
		pst = conexion.conn.prepareStatement("DELETE FROM iSectores WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iClientesLDir WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iClientesLContactos WHERE codEmpresa=1");
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
		pst = conexion.conn.prepareStatement("DELETE FROM iClientes WHERE codEmpresa=1 AND flaNvoCliente=0 AND flaObsoleto=0");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iZonas WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iFormasPago WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iArticulosLTiposCliente WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iTiposCliente WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iTarifasLin WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iAgentesLCat WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iAgentesLIdiomas WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iAgentesLTar WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iTarifas WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iAgentes WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iArticulosTyC WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iArticulosLFam WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iArticulosLAlt WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iArticulos WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iFamilias WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = conexion.conn.prepareStatement("DELETE FROM iCatalogos WHERE codEmpresa=1");
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		return true;
	}
	
	private boolean materializarTablasAD() {
		PreparedStatement pst = null;
		
		pst = DB.prepareStatement("drop table TBI_PRODUCTOSTOCK", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create table TBI_PRODUCTOSTOCK as select * from BI_PRODUCTOSTOCK", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		pst = DB.prepareStatement("create unique index TBIi1_PRODUCTOSTOCK on TBI_PRODUCTOSTOCK(codigo)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		pst = DB.prepareStatement("update TBI_PRODUCTOSTOCK set DISPONIBLE=DISPONIBLE + "
				+ "            (select min(s.DISPONIBLE/b.bomqty) "
				+ "             from m_product_bom b, m_product padre, m_product hijo, TBI_PRODUCTOSTOCK s "
				+ "             where b.m_product_id=padre.m_product_id "
				+ "             and   b.m_productbom_id=hijo.m_product_id "
				+ "             and   hijo.value=s.CODIGO "
				+ "             and   b.bomqty>0 "
				+ "             and   TBI_PRODUCTOSTOCK.CODIGO=padre.value "
				+ "             group by padre.value, hijo.value "
				+ "             having min(s.DISPONIBLE/b.bomqty)>0) "
				+ "where exists(select 1 "
				+ "             from m_product_bom b, m_product padre, m_product hijo, TBI_PRODUCTOSTOCK s "
				+ "             where b.m_product_id=padre.m_product_id "
				+ "             and   b.m_productbom_id=hijo.m_product_id "
				+ "             and   hijo.value=s.CODIGO "
				+ "             and   b.bomqty>0 "
				+ "             and   TBI_PRODUCTOSTOCK.CODIGO=padre.value "
				+ "             group by padre.value, hijo.value "
				+ "             having min(s.DISPONIBLE/b.bomqty)>0)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//
		pst = DB.prepareStatement("drop table TOV_PRODUCTOS_INACATALOG", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create table TOV_PRODUCTOS_INACATALOG as select * from OV_PRODUCTOS_INACATALOG", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create unique index TOVi1_PRODUCTOS_INACATALOG on TOV_PRODUCTOS_INACATALOG(m_product_id)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//
		pst = DB.prepareStatement("drop table Tbi_pagosafecha", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create table Tbi_pagosafecha as select * from bi_pagosafecha", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create unique index Tbii1_pagosafecha on Tbi_pagosafecha(c_bpartner_id)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//
		pst = DB.prepareStatement("drop table Tbi_pagosafecha", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create table Tbi_pagosafecha as select * from bi_pagosafecha", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create unique index Tbii1_pagosafecha on Tbi_pagosafecha(c_bpartner_id)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//
		pst = DB.prepareStatement("drop table Tbi_saldosclientes", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create table Tbi_saldosclientes as select * from bi_saldosclientes", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create unique index Tbii1_saldosclientes on Tbi_saldosclientes(c_bpartner_id)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//
		pst = DB.prepareStatement("drop table tRVCW_INFORMEEMBARQUE2", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create table tRVCW_INFORMEEMBARQUE2 as select * from RVCW_INFORMEEMBARQUE2", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create index tRVCW_INFORMEEMBARQUE2 on tRVCW_INFORMEEMBARQUE2(m_product_id)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//
		pst = DB.prepareStatement("drop table tov_productos_x_recibir", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create table tov_productos_x_recibir as select * from ov_productos_x_recibir", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pst = DB.prepareStatement("create index tovi1_productos_x_recibir on tov_productos_x_recibir(m_product_id)", get_TrxName());
		try {
			pst.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}

}
