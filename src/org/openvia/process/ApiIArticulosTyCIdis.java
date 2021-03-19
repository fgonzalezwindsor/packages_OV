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
import org.openvia.inacatalog.iarticulostycidis.IArticulosTyCIdisImp;
import org.openvia.inacatalog.iarticulostycidis.IArticulosTyCIdisModel;
import org.openvia.inacatalog.iarticulostycidis.I_iArticulosTyCIdis;
import org.openvia.inacatalog.iarticulostycs.IArticulosTyCsImp;
import org.openvia.inacatalog.iarticulostycs.IArticulosTyCsModel;
import org.openvia.inacatalog.iarticulostycs.I_iArticulosTyCs;

public class ApiIArticulosTyCIdis extends SvrProcess {
	
	I_iArticulosTyCIdis apiArticulosTyCIdis = new IArticulosTyCIdisImp();	
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
		for (IArticulosTyCIdisModel iartTyCIdi : listarArticulosTyCIdis()) {
			// Buscar si ArticuloTyCIdi existe
			if (apiArticulosTyCIdis.apiGetArticuloTyCIdi(iartTyCIdi.getCodEmpresa(), iartTyCIdi.getCodModeloTyC(), iartTyCIdi.getCodColor(), iartTyCIdi.getCodTalla(), iartTyCIdi.getCodIdiomaDestino()) == null) {
				// Crear ArticuloTyCIdi
				System.out.println("Crear Articulo TyCIdi: " + iartTyCIdi.getCodModeloTyC());
				if (apiArticulosTyCIdis.apiPostArticuloTyCIdi(iartTyCIdi))
					ret = "Artitulo TyCIdi insertado correctamente";
				else 
					ret = "Error al insertar Articulo TyCIdi";
			} else {
				System.out.println("Actualiza ArticuloTyCIdi: " + iartTyCIdi.getCodModeloTyC());
				if (apiArticulosTyCIdis.apiPutArticuloTyCIdi(iartTyCIdi))
					ret = "ArticuloTyCIdi actualizado";
				else
					ret = "Error al actualizar ArticuloTyCIdi";
			}
		}
	
		return ret;
	}
	
	private List<IArticulosTyCIdisModel> listarArticulosTyCIdis() {
		List<IArticulosTyCIdisModel> lista = new ArrayList<IArticulosTyCIdisModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IARTICULOSTYCIDI", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IArticulosTyCIdisModel artTyCIdi = new IArticulosTyCIdisModel();
				artTyCIdi.setCodEmpresa(rs.getInt("codEmpresa"));
				artTyCIdi.setCodArticulo(rs.getString("codArticulo"));
				artTyCIdi.setCodModeloTyC(rs.getString("codModeloTyC"));
				artTyCIdi.setCodIdiomaDestino(rs.getString("codIdiomaDestino"));
				artTyCIdi.setCodColor(rs.getString("codColor"));
				artTyCIdi.setDesColor(rs.getString("desColor"));
				artTyCIdi.setCodTalla(rs.getString("codTalla"));
				artTyCIdi.setDesTalla(rs.getString("desTalla"));
				artTyCIdi.setDesArticulo(rs.getString("desArticulo" ));
				artTyCIdi.setDatMedidas(rs.getString("datMedidas"));
				artTyCIdi.setDatPeso(rs.getString("datPeso"));
				artTyCIdi.setDatVolumen(rs.getString("datVolumen"));
				artTyCIdi.setDatFechaEntradaPrevista(rs.getString("datFechaEntradaPrevista"));
				
				lista.add(artTyCIdi);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
