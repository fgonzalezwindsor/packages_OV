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

public class ApiIArticulosLFams extends SvrProcess {
	
	I_iArticulosLFams apiArticulosLFams = new IArticulosLFamsImp();	
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
		for (IArticulosLFamsModel iartLFam : listarArticulosLFams()) {
			// Buscar si ArticuloLFams existe
			if (apiArticulosLFams.apiGetArticuloLFam(iartLFam.getCodEmpresa(), iartLFam.getCodArticulo(), iartLFam.getCodCatalogo(), iartLFam.getCodFamilia(), iartLFam.getCodSubFamilia()) == null) {
				// Crear ArticuloLFam
				System.out.println("Crear ArticuloLFam: " + iartLFam.getCodArticulo() + " " + iartLFam.getCodCatalogo() + " " + iartLFam.getCodFamilia() + " " + iartLFam.getCodSubFamilia());
				if (apiArticulosLFams.apiPostArticuloLFam(iartLFam))
					ret = "ArticuloLFam insertado correctamente";
				else 
					ret = "Error al insertar ArticuloLFam";
			} else {
				System.out.println("Actualiza Articulo: " + iartLFam.getCodArticulo() + " " + iartLFam.getCodCatalogo() + " " + iartLFam.getCodFamilia() + " " + iartLFam.getCodSubFamilia());
				if (apiArticulosLFams.apiPutArticuloLFam(iartLFam))
					ret = "ArticuloLFam actualizado";
				else 
					ret = "Error al actualizar ArticuloLFam";
			}
		}
	
		return ret;
	}
	
	private List<IArticulosLFamsModel> listarArticulosLFams() {
		List<IArticulosLFamsModel> lista = new ArrayList<IArticulosLFamsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IArticulosLFam", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IArticulosLFamsModel art = new IArticulosLFamsModel();
				art.setCodEmpresa(rs.getInt("codEmpresa"));
				art.setCodArticulo(rs.getString("codArticulo"));
				art.setCodCatalogo(rs.getString("codCatalogo"));
				art.setCodFamilia(rs.getInt("codFamilia"));
				art.setCodSubFamilia(rs.getInt("codSubFamilia"));
				art.setOrdArticulo(rs.getInt("ordArticulo"));
				
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
