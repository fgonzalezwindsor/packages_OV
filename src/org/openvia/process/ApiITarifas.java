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

public class ApiITarifas extends SvrProcess {
	
	I_iTarifas apiTarifas = new ITarifasImp();	
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
		for (ITarifasModel itar : listarTarifas()) {
			// Buscar si catalogo existe
			if (apiTarifas.apiGetTarifa(itar.getCodEmpresa(), itar.getCodTarifa()) == null) {
				// Crear Agente
				System.out.println("Crear Tarifa: " + itar.getCodTarifa());
				if (apiTarifas.apiPostTarifa(itar))
					ret = "Tarifa insertada correctamente";
				else 
					ret = "Error al insertar Tarifa";
			} else {
				System.out.println("Tarifa ya existe: " + itar.getCodTarifa());
			}
		}
	
		return ret;
	}
	
	private List<ITarifasModel> listarTarifas() {
		List<ITarifasModel> lista = new ArrayList<ITarifasModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM ITarifas", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				ITarifasModel tar = new ITarifasModel();
				tar.setCodEmpresa(rs.getInt("codEmpresa"));
				tar.setCodTarifa(rs.getString("codTarifa"));
				tar.setDesTarifa(rs.getString("desTarifa"));
				tar.setCodIncoterm(rs.getString("codIncoterm"));
				tar.setFlaIVAIncluido(rs.getString("flaIVAIncluido"));
				tar.setCodMoneda(rs.getString("codMoneda"));
				
				lista.add(tar);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
