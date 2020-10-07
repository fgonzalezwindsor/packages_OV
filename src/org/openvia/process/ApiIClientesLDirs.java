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
import org.openvia.inacatalog.iclientesldirs.IClientesLDirsImp;
import org.openvia.inacatalog.iclientesldirs.IClientesLDirsModel;
import org.openvia.inacatalog.iclientesldirs.I_iClientesLDirs;

public class ApiIClientesLDirs extends SvrProcess {
	
	I_iClientesLDirs apiClientesLDirs = new IClientesLDirsImp();	
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
		for (IClientesLDirsModel clientDir : listarClientesLDirs()) {
			// Buscar si ClienteLDir existe
			if (apiClientesLDirs.apiGetClienteLDir(clientDir.getCodEmpresa(), clientDir.getCodCliente(), clientDir.getLinDirCli()) == null) {
				// Crear ClienteLDir
				System.out.println("Crear ClienteLDir: " + clientDir.getCodCliente() + " " + clientDir.getLinDirCli());
				if (apiClientesLDirs.apiPostClienteLDir(clientDir))
					ret = "ClienteLDir insertado correctamente";
				else 
					ret = "Error al insertar ClienteLDir";
			} else {
				System.out.println("ClienteLDir ya existe: " + clientDir.getCodCliente() + " " + clientDir.getLinDirCli());
			}
		}
	
		return ret;
	}
	
	private List<IClientesLDirsModel> listarClientesLDirs() {
		List<IClientesLDirsModel> lista = new ArrayList<IClientesLDirsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IClientesLDir", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IClientesLDirsModel clienteLDir = new IClientesLDirsModel();
				clienteLDir.setCodEmpresa(rs.getInt("codEmpresa"));
				clienteLDir.setCodCliente(rs.getString("codCliente"));
				clienteLDir.setLinDirCli(rs.getInt("linDirCli"));
				clienteLDir.setNomDirCli(rs.getString("nomDirCli"));
				clienteLDir.setRsoDirCli(rs.getString("rsoDirCli"));
				clienteLDir.setDatCalleDirCli(rs.getString("datCalleDirCli"));
				clienteLDir.setCodPostalDirCli(rs.getString("codPostalDirCli"));
				clienteLDir.setDatPoblacionDirCli(rs.getString("datPoblacionDirCli"));
				clienteLDir.setDatProvinciaDirCli(rs.getString("datProvinciaDirCli"));
				clienteLDir.setDatPaisDirCli(rs.getString("datPaisDirCli"));
				clienteLDir.setDatContactoDirCli(rs.getString("datContactoDirCli"));
				clienteLDir.setDatTelefonoDirCli(rs.getString("datTelefonoDirCli"));
				clienteLDir.setDatFaxDirCli(rs.getString("datFaxDirCli"));
				clienteLDir.setDatEmailDirCli(rs.getString("datEmailDirCli"));
				clienteLDir.setHipWebDirCli(rs.getString("hipWebDirCli"));
				clienteLDir.setCodSuDirCli(rs.getString("codSuDirCli"));
				clienteLDir.setValLatitud(rs.getDouble("valLatitud"));
				clienteLDir.setValLongitud(rs.getDouble("valLongitud"));
				clienteLDir.setDatTelMovilDirCli(rs.getString("datTelMovilDirCli"));
				clienteLDir.setCodAgente(rs.getString("codAgente"));
				clienteLDir.setFlaNvoDirCli(rs.getInt("flaNvoDirCli"));
				
				lista.add(clienteLDir);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
