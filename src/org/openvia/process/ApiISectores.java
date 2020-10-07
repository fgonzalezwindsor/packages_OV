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
import org.openvia.inacatalog.isectores.ISectoresImp;
import org.openvia.inacatalog.isectores.ISectoresModel;
import org.openvia.inacatalog.isectores.I_iSectores;

public class ApiISectores extends SvrProcess {
	
	I_iSectores apiSectores = new ISectoresImp();	
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
		for (ISectoresModel sect : listarSectores()) {
			// Buscar si Sector existe
			if (apiSectores.apiGetSector(sect.getCodEmpresa(), sect.getCodSector()) == null) {
				// Crear Sector
				System.out.println("Crear Sector: " + sect.getCodSector());
				if (apiSectores.apiPostSector(sect))
					ret = "Sector insertado correctamente";
				else 
					ret = "Error al insertar Sector";
			} else {
				System.out.println("Sector ya existe: " + sect.getCodSector());
			}
		}
	
		return ret;
	}
	
	private List<ISectoresModel> listarSectores() {
		List<ISectoresModel> lista = new ArrayList<ISectoresModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM ISectores", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				ISectoresModel sect = new ISectoresModel();
				sect.setCodEmpresa(rs.getInt("codEmpresa"));
				sect.setCodSector(rs.getString("codSector"));
				sect.setDesSector(rs.getString("desSector"));
				
				lista.add(sect);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
