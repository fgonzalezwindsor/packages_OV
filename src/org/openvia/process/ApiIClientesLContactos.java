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
import org.openvia.inacatalog.iclienteslcontactos.IClientesLContactosImp;
import org.openvia.inacatalog.iclienteslcontactos.IClientesLContactosModel;
import org.openvia.inacatalog.iclienteslcontactos.I_iClientesLContactos;

public class ApiIClientesLContactos extends SvrProcess {
	
	I_iClientesLContactos apiClientesLContactos = new IClientesLContactosImp();	
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
		for (IClientesLContactosModel iClienteLContacto : listarClientesLContactos()) {
			// Buscar si catalogo existe
			if (apiClientesLContactos.apiGetClienteLContacto(iClienteLContacto.getCodEmpresa(), iClienteLContacto.getCodCliente(), iClienteLContacto.getLinContactCli()) == null) {
				// Crear ClienteLContacto
				System.out.println("Crear ClienteLContacto: " + iClienteLContacto.getCodCliente() + " " + iClienteLContacto.getLinContactCli());
				if (apiClientesLContactos.apiPostClienteLContacto(iClienteLContacto))
					ret = "ClienteLContacto insertado correctamente";
				else 
					ret = "Error al insertar ClienteLContacto";
			} else {
				System.out.println("ClienteLContacto ya existe: " + iClienteLContacto.getCodCliente() + " " + iClienteLContacto.getLinContactCli());
			}
		}
	
		return ret;
	}
	
	private List<IClientesLContactosModel> listarClientesLContactos() {
		List<IClientesLContactosModel> lista = new ArrayList<IClientesLContactosModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IClientesLContactos", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IClientesLContactosModel clienteLContacto = new IClientesLContactosModel();
				clienteLContacto.setCodEmpresa(rs.getInt("codEmpresa"));
				clienteLContacto.setCodCliente(rs.getString("codCliente"));
				clienteLContacto.setLinContactCli(rs.getInt("linContactCli"));
				clienteLContacto.setNomContactCli(rs.getString("nomContactCli"));
				clienteLContacto.setDatPuestoContactCli(rs.getString("datPuestoContactCli"));
				clienteLContacto.setDatTelefonoContactCli(rs.getString("datTelefonoContactCli"));
				clienteLContacto.setDatEmailContactCli(rs.getString("datEmailContactCli"));
				clienteLContacto.setCustom1ContactCli(rs.getString("Custom1ContactCli"));
				clienteLContacto.setCustom2ContactCli(rs.getString("Custom2ContactCli"));
				clienteLContacto.setCustom3ContactCli(rs.getString("Custom3ContactCli"));
				clienteLContacto.setFlaNvoContactCli(rs.getInt("flaNvoContactCli"));
				
				lista.add(clienteLContacto);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
