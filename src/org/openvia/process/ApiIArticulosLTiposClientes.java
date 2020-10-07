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
import org.openvia.inacatalog.iarticulos.IArticulosImp;
import org.openvia.inacatalog.iarticulos.IArticulosModel;
import org.openvia.inacatalog.iarticulos.I_iArticulos;
import org.openvia.inacatalog.iarticuloslfams.IArticulosLFamsImp;
import org.openvia.inacatalog.iarticuloslfams.IArticulosLFamsModel;
import org.openvia.inacatalog.iarticuloslfams.I_iArticulosLFams;
import org.openvia.inacatalog.iarticulosltiposclientes.IArticulosLTiposClientesImp;
import org.openvia.inacatalog.iarticulosltiposclientes.IArticulosLTiposClientesModel;
import org.openvia.inacatalog.iarticulosltiposclientes.I_iArticulosLTiposClientes;

public class ApiIArticulosLTiposClientes extends SvrProcess {
	
	I_iArticulosLTiposClientes apiArticulosLTiposClientes = new IArticulosLTiposClientesImp();	
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
		for (IArticulosLTiposClientesModel iartLTipoArticulo : listarArticulosLTiposClientes()) {
			// Buscar si ArticuloLFams existe
			if (apiArticulosLTiposClientes.apiGetArticuloLTipoCliente(iartLTipoArticulo.getCodEmpresa(), iartLTipoArticulo.getCodArticulo(), iartLTipoArticulo.getCodTipoCliente()) == null) {
				// Crear ArticuloLFam
				System.out.println("Crear ArticuloLTipoArticulo: " + iartLTipoArticulo.getCodArticulo() + " " + iartLTipoArticulo.getCodTipoCliente());
				if (apiArticulosLTiposClientes.apiPostArticuloLTipoCliente(iartLTipoArticulo))
					ret = "ArticuloLFam insertado correctamente";
				else 
					ret = "Error al insertar ArticuloLTipoArticulo";
			} else {
				System.out.println("ArticuloLTipoArticulo ya existe: " + iartLTipoArticulo.getCodArticulo());
			}
		}
	
		return ret;
	}
	
	private List<IArticulosLTiposClientesModel> listarArticulosLTiposClientes() {
		List<IArticulosLTiposClientesModel> lista = new ArrayList<IArticulosLTiposClientesModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IArticulosLTiposCliente", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IArticulosLTiposClientesModel art = new IArticulosLTiposClientesModel();
				art.setCodEmpresa(rs.getInt("codEmpresa"));
				art.setCodArticulo(rs.getString("codArticulo"));
				art.setCodTipoCliente(rs.getString("codTipoCliente"));
				
				lista.add(art);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
