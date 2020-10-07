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
import org.openvia.inacatalog.icatalogos.ICatalogosImp;
import org.openvia.inacatalog.icatalogos.ICatalogosModel;
import org.openvia.inacatalog.icatalogos.I_iCatalogos;

public class ApiICatalogos extends SvrProcess {
	
	I_iCatalogos apiCatalogos = new ICatalogosImp();

	/** Client to be imported to */
	private int m_AD_Client_ID = 1000000;
	/** Organization to be imported to */
	private int m_AD_Org_ID = 1000000;

	/**
	 * Prepare - e.g., get Parameters.
	 */
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = 1000000;
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = 1000000;
			else if (name.equals("DeleteOldImported"))
				;// m_deleteOldImported = "Y".equals(para[i].getParameter());
			else if (name.equals("DocAction"))
				;// m_docAction = (String)para[i].getParameter();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	} // prepare

	@Override
	protected String doIt() {
		String ret = "";
		for (ICatalogosModel icat : listarCatalogos()) {
			// Buscar si catalogo existe
			if (apiCatalogos.apiGetCatalogo(icat.getCodEmpresa(), icat.getCodCatalogo()) == null) {
				// Crear Catalogo
				System.out.println("Crear Catalogo: " + icat.getCodCatalogo());
				if (apiCatalogos.apiPostCatalogo(icat))
					ret = "Catalogo insertado correctamente";
				else 
					ret = "Error al insertar Catalogo";
			} else {
				System.out.println("Catalogo ya existe: " + icat.getCodCatalogo());
			}
		}
	
		return ret;
	}
	
	private List<ICatalogosModel> listarCatalogos() {
		List<ICatalogosModel> lista = new ArrayList<ICatalogosModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM ICatalogos", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				ICatalogosModel cat = new ICatalogosModel();
				cat.setCodEmpresa(rs.getInt("codEmpresa"));
				cat.setCodCatalogo(rs.getString("codCatalogo"));
				cat.setDesCatalogo(rs.getString("desCatalogo"));
				cat.setObsCatalogo(rs.getString("obsCatalogo"));
				cat.setNomImagenCat(rs.getString("nomImagenCat"));
				cat.setNomIconoCat(rs.getString("nomIconoCat"));
				cat.setFlaIcoModificado(rs.getString("flaIcoModificado"));
				cat.setFlaImgModificado(rs.getString("flaImgModificado"));
				cat.setOrdCatalogo(rs.getInt("ordCatalogo"));
				
				lista.add(cat);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}

}
