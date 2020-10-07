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
import org.openvia.inacatalog.iarticulostycs.IArticulosTyCsImp;
import org.openvia.inacatalog.iarticulostycs.IArticulosTyCsModel;
import org.openvia.inacatalog.iarticulostycs.I_iArticulosTyCs;

public class ApiIArticulosTyCs extends SvrProcess {
	
	I_iArticulosTyCs apiArticulosTyCs = new IArticulosTyCsImp();	
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
		for (IArticulosTyCsModel iartTyC : listarArticulosTyCs()) {
			// Buscar si catalogo existe
			if (apiArticulosTyCs.apiGetArticuloTyC(iartTyC.getCodEmpresa(), iartTyC.getCodModeloTyC(), iartTyC.getCodColor(), iartTyC.getCodTalla()) == null) {
				// Crear Agente
				System.out.println("Crear Articulo TyC: " + iartTyC.getCodModeloTyC());
				if (apiArticulosTyCs.apiPostArticuloTyC(iartTyC))
					ret = "Artitulo TyC insertado correctamente";
				else 
					ret = "Error al insertar Articulo TyC";
			} else {
				System.out.println("Actualiza ArticuloTyC: " + iartTyC.getCodModeloTyC());
				if (apiArticulosTyCs.apiPutArticuloTyC(iartTyC))
					ret = "ArticuloTyC actualizado";
				else
					ret = "Error al actualizar ArticuloTyC";
			}
		}
	
		return ret;
	}
	
	private List<IArticulosTyCsModel> listarArticulosTyCs() {
		List<IArticulosTyCsModel> lista = new ArrayList<IArticulosTyCsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM iArticuloTyC", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IArticulosTyCsModel artTyC = new IArticulosTyCsModel();
				artTyC.setCodEmpresa(rs.getInt("codEmpresa"));
				artTyC.setCodArticulo(rs.getString("codArticulo"));
				artTyC.setCodModeloTyC(rs.getString("codModeloTyC"));
				artTyC.setCodColor(rs.getString("codColor"));
				artTyC.setDesColor(rs.getString("desColor"));
				artTyC.setCodTalla(rs.getString("codTalla"));
				artTyC.setDesTalla(rs.getString("desTalla"));
				artTyC.setDesArticulo(rs.getString("desArticulo" ));
				artTyC.setCodEAN13(rs.getString("codEAN13" ));
				artTyC.setDatMedidas(rs.getString("datMedidas"));
				artTyC.setDatPeso(rs.getString("datPeso"));
				artTyC.setDatVolumen(rs.getString("datVolumen"));
				artTyC.setValMinVenta(rs.getDouble("valMinVenta"));
				artTyC.setValUniXCaja(rs.getDouble("valUniXCaja"));
				artTyC.setValUniXPalet(rs.getDouble("valUniXPalet"));
				artTyC.setValUniIncSencillo(rs.getDouble("valUniIncSencillo"));
				artTyC.setStoDisponible(rs.getDouble("stoDisponible"));
				artTyC.setStoPteRecibir(rs.getDouble("stoPteRecibir"));
				artTyC.setDatFechaEntradaPrevista(rs.getString("datFechaEntradaPrevista"));
				artTyC.setOrdTalla(rs.getInt("ordTalla"));
				artTyC.setOrdColor(rs.getInt("ordColor"));
				artTyC.setPreArticuloGen(rs.getDouble("preArticuloGen"));
				artTyC.setCodSurtido(rs.getString("codSurtido"));
				artTyC.setFlaNoAplicarDtoPP(rs.getString("flaNoAplicarDtoPP"));
				artTyC.setDatMarcas(rs.getString("datMarcas"));
				artTyC.setPrePuntos(rs.getDouble("prePuntos"));
				artTyC.setFlaMuestra(rs.getString("flaMuestra"));
				
				lista.add(artTyC);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
