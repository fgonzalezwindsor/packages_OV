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
import org.openvia.inacatalog.iagenteslcats.IAgentesLCatsImp;
import org.openvia.inacatalog.iagenteslcats.IAgentesLCatsModel;
import org.openvia.inacatalog.iagenteslcats.I_iAgentesLCats;
import org.openvia.inacatalog.iagentesltars.IAgentesLTarsImp;
import org.openvia.inacatalog.iagentesltars.IAgentesLTarsModel;
import org.openvia.inacatalog.iagentesltars.I_iAgentesLTars;

public class ApiIAgentesLCats extends SvrProcess {
	
	I_iAgentesLCats apiAgentesLCats = new IAgentesLCatsImp();	
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
		for (IAgentesLCatsModel iagenlcat : listarAgentesLCats()) {
			// Buscar si AgenteLCat existe
			if (apiAgentesLCats.apiGetAgenteLCat(iagenlcat.getCodEmpresa(), iagenlcat.getCodAgente(), iagenlcat.getCodCatalogo()) == null) {
				// Crear AgenteLCat
				System.out.println("Crear AgenteLCat: " + iagenlcat.getCodAgente() + " " + iagenlcat.getCodCatalogo());
				if (apiAgentesLCats.apiPostAgenteLCat(iagenlcat))
					ret = "AgenteLCat insertado correctamente";
				else 
					ret = "Error al insertar AgenteLCat";
			} else {
				System.out.println("AgenteLCat ya existe: " + iagenlcat.getCodAgente() + " " + iagenlcat.getCodCatalogo());
			}
		}
	
		return ret;
	}
	
	private List<IAgentesLCatsModel> listarAgentesLCats() {
		List<IAgentesLCatsModel> lista = new ArrayList<IAgentesLCatsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IAgentesLCat", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IAgentesLCatsModel agen = new IAgentesLCatsModel();
				agen.setCodEmpresa(rs.getInt("codEmpresa"));
				agen.setCodAgente(rs.getString("codAgente"));
				agen.setCodCatalogo(rs.getString("codCatalogo"));
				
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
