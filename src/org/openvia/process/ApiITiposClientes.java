package org.openvia.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.openvia.inacatalog.iagentes.IAgentesImp;
import org.openvia.inacatalog.iagentes.IAgentesModel;
import org.openvia.inacatalog.iagentes.I_iAgentes;
import org.openvia.inacatalog.itarifas.ITarifasImp;
import org.openvia.inacatalog.itarifas.ITarifasModel;
import org.openvia.inacatalog.itarifas.I_iTarifas;
import org.openvia.inacatalog.itiposclientes.ITiposClientesImp;
import org.openvia.inacatalog.itiposclientes.ITiposClientesModel;
import org.openvia.inacatalog.itiposclientes.I_iTiposClientes;
import org.openvia.inacatalog.izonas.IZonasImp;
import org.openvia.inacatalog.izonas.IZonasModel;
import org.openvia.inacatalog.izonas.I_iZonas;

public class ApiITiposClientes extends SvrProcess {
	
	I_iTiposClientes apiTiposClientes = new ITiposClientesImp();	
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
		for (ITiposClientesModel tipoCliente : listarTiposClientes()) {
			// Buscar si tipo cliente existe
			if (apiTiposClientes.apiGetTipoCliente(tipoCliente.getCodEmpresa(), tipoCliente.getCodTipoCliente()) == null) {
				// Crear Tipo Cliente
				System.out.println("Crear Tipo Cliente: " + tipoCliente.getCodTipoCliente());
				if (apiTiposClientes.apiPostTipoCliente(tipoCliente))
					ret = "Tipo Cliente insertada correctamente";
				else 
					ret = "Error al insertar Tipo Cliente";
			} else {
				System.out.println("Tipo Cliente ya existe: " + tipoCliente.getCodTipoCliente());
			}
		}
	
		return ret;
	}
	
	private List<ITiposClientesModel> listarTiposClientes() {
		List<ITiposClientesModel> lista = new ArrayList<ITiposClientesModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM ITiposCliente", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				ITiposClientesModel tipoCliente = new ITiposClientesModel();
				tipoCliente.setCodEmpresa(rs.getInt("codEmpresa"));
				tipoCliente.setCodTipoCliente(rs.getString("codTipoCliente"));
				tipoCliente.setDesTipoCliente(rs.getString("desTipoCliente"));
				
				lista.add(tipoCliente);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
