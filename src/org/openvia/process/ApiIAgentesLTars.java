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
import org.openvia.inacatalog.iagentesltars.IAgentesLTarsImp;
import org.openvia.inacatalog.iagentesltars.IAgentesLTarsModel;
import org.openvia.inacatalog.iagentesltars.I_iAgentesLTars;

public class ApiIAgentesLTars extends SvrProcess {
	
	I_iAgentesLTars apiAgentesLTars = new IAgentesLTarsImp();	
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
		for (IAgentesLTarsModel iagenltar : listarAgentesLTars()) {
			// Buscar si AgenteLTar existe
			if (apiAgentesLTars.apiGetAgenteLTar(iagenltar.getCodEmpresa(), iagenltar.getCodAgente(), iagenltar.getCodTarifa()) == null) {
				// Crear AgenteLTar
				System.out.println("Crear AgenteLTar: " + iagenltar.getCodAgente() + " " + iagenltar.getCodTarifa());
				if (apiAgentesLTars.apiPostAgenteLTar(iagenltar))
					ret = "AgenteLTar insertado correctamente";
				else 
					ret = "Error al insertar AgenteLTar";
			} else {
				System.out.println("AgenteLTar ya existe: " + iagenltar.getCodAgente() + " " + iagenltar.getCodTarifa());
			}
		}
	
		return ret;
	}
	
	private List<IAgentesLTarsModel> listarAgentesLTars() {
		List<IAgentesLTarsModel> lista = new ArrayList<IAgentesLTarsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IAgentesLTar", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IAgentesLTarsModel agen = new IAgentesLTarsModel();
				agen.setCodEmpresa(rs.getInt("codEmpresa"));
				agen.setCodAgente(rs.getString("codAgente"));
				agen.setCodTarifa(rs.getString("codTarifa"));
				
				lista.add(agen);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
