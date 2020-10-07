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

public class ApiIArticulos extends SvrProcess {
	
	I_iArticulos apiArticulos = new IArticulosImp();	
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
		for (IArticulosModel iart : listarArticulos()) {
			// Buscar si catalogo existe
			if (apiArticulos.apiGetArticulo(iart.getCodEmpresa(), iart.getCodArticulo()) == null) {
				// Crear Articulo
				System.out.println("Crear Articulo: " + iart.getCodArticulo());
				if (apiArticulos.apiPostArticulo(iart))
					ret = "Articulo insertado correctamente";
				else 
					ret = "Error al insertar Articulo";
			} else {
				System.out.println("Actualiza Articulo: " + iart.getCodArticulo());
				if (apiArticulos.apiPutArticulo(iart))
					ret = "Articulo actualizado";
				else 
					ret = "Error al actualizar Articulo";
			}
		}
	
		return ret;
	}
	
	private List<IArticulosModel> listarArticulos() {
		List<IArticulosModel> lista = new ArrayList<IArticulosModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IArticulo", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IArticulosModel art = new IArticulosModel();
				art.setCodEmpresa(rs.getInt("codEmpresa"));
				art.setCodArticulo(rs.getString("codArticulo"));
				art.setDesArticulo(rs.getString("desArticulo"));
				art.setCodEAN13(rs.getString("codEAN13"));
				art.setDatMedidas(rs.getString("datMedidas"));
				art.setDatPeso(rs.getString("datPeso"));
				art.setDatVolumen(rs.getString("datVolumen"));
				art.setObsArticulo(rs.getString("obsArticulo"));
				art.setHipArticulo(rs.getString("hipArticulo"));
				art.setValMinVenta(rs.getDouble("valMinVenta"));
				art.setValUniXCaja(rs.getDouble("valUniXCaja"));
				art.setValUniXPalet(rs.getDouble("valUniXPalet"));
				art.setValUniIncSencillo(rs.getDouble("valUniIncSencillo"));
				art.setCodTipoArticulo(rs.getString("codTipoArticulo"));
				art.setCodCatalogo(rs.getString("codCatalogo"));
				art.setCodFamilia(rs.getInt("codFamilia"));
				art.setCodSubFamilia(rs.getInt("codSubFamilia"));
				art.setCodGrupoPreciosArticulo(rs.getString("codGrupoPreciosArticulo"));
				art.setTpcIva(rs.getDouble("tpcIva"));
				art.setTpcRe(rs.getDouble("tpcRe"));
				art.setTpcIGIC(rs.getDouble("tpcIGIC"));
				art.setCodModeloTyC(rs.getString("codModeloTyC"));
				art.setDesModeloTyC(rs.getString("desModeloTyC"));
				art.setStoDisponible(rs.getDouble("stoDisponible"));
				art.setStoPteRecibir(rs.getDouble("stoPteRecibir"));
				art.setDatFechaEntradaPrevista(rs.getString("datFechaEntradaPrevista"));
				art.setOrdArticulo(rs.getInt("ordArticulo"));
				art.setPreArticuloGen(rs.getDouble("preArticuloGen"));
				art.setDatNivel1(rs.getString("datNivel1"));
				art.setDatNivel2(rs.getString("datNivel2"));
				art.setCodEmpSuministradora(rs.getInt("codEmpSuministradora"));
				art.setFlaNoAplicarDtoPP(rs.getString("flaNoAplicarDtoPP"));
				art.setDatMarcas(rs.getString("datMarcas"));
				art.setPrePuntos(rs.getDouble("prePuntos"));
				art.setFlaMuestra(rs.getString("flaMuestra"));
				
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
