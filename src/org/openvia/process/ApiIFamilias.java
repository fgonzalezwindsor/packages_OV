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
import org.openvia.inacatalog.ifamilias.IFamiliasImp;
import org.openvia.inacatalog.ifamilias.IFamiliasModel;
import org.openvia.inacatalog.ifamilias.I_iFamilias;

public class ApiIFamilias extends SvrProcess {
	
	I_iFamilias apiFamilias =  new IFamiliasImp();
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
		for (IFamiliasModel ifam : listarFamilias()) {
			// Buscar si catalogo existe
			if (apiFamilias.apiGetFamilia(ifam.getCodEmpresa(), ifam.getCodCatalogo(), ifam.getCodFamilia(), ifam.getCodSubFamilia()) == null) {
				// Crear Familia
				System.out.println("Crear Familia: " + ifam.getCodFamilia());
				if (apiFamilias.apiPostFamilia(ifam))
					ret = "Familia insertado correctamente";
				else 
					ret = "Error al insertar Familia";
			} else {
				System.out.println("Familia ya existe: " + ifam.getCodFamilia());
			}
		}
	
		return ret;
	}
	
	private List<IFamiliasModel> listarFamilias() {
		List<IFamiliasModel> lista = new ArrayList<IFamiliasModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IFamilias", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IFamiliasModel fam = new IFamiliasModel();
				fam.setCodEmpresa(rs.getInt("codEmpresa"));
				fam.setCodCatalogo(rs.getString("codCatalogo"));
				fam.setCodFamilia(rs.getInt("codFamilia"));
				fam.setCodSubFamilia(rs.getInt("codSubFamilia"));
				fam.setDesFamilia(rs.getString("desFamilia"));
				fam.setNomIcoFamilia(rs.getString("nomIcoFamilia"));
				fam.setOrdFamilia(rs.getInt("ordFamilia"));
				fam.setFlaIcoModificado(rs.getString("flaIcoModificado"));
				fam.setObsFamilia(rs.getString("obsFamilia"));
				fam.setNomImagenFam(rs.getString("obsFamilia"));
				fam.setFlaImgModificado(rs.getString("flaImgModificado"));
				
				lista.add(fam);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
