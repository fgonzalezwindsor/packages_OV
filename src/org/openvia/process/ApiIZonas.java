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
import org.openvia.inacatalog.itarifas.ITarifasImp;
import org.openvia.inacatalog.itarifas.ITarifasModel;
import org.openvia.inacatalog.itarifas.I_iTarifas;
import org.openvia.inacatalog.izonas.IZonasImp;
import org.openvia.inacatalog.izonas.IZonasModel;
import org.openvia.inacatalog.izonas.I_iZonas;

public class ApiIZonas extends SvrProcess {
	
	I_iZonas apiZonas = new IZonasImp();	
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
		for (IZonasModel zona : listarZonas()) {
			// Buscar si zona existe
			if (apiZonas.apiGetZona(zona.getCodEmpresa(), zona.getCodZona()) == null) {
				// Crear Zona
				System.out.println("Crear Zona: " + zona.getCodZona());
				if (apiZonas.apiPostZona(zona))
					ret = "Zona insertada correctamente";
				else 
					ret = "Error al insertar Zona";
			} else {
				System.out.println("Zona ya existe: " + zona.getCodZona());
			}
		}
	
		return ret;
	}
	
	private List<IZonasModel> listarZonas() {
		List<IZonasModel> lista = new ArrayList<IZonasModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IZonas", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IZonasModel zona = new IZonasModel();
				zona.setCodEmpresa(rs.getInt("codEmpresa"));
				zona.setCodZona(rs.getString("codZona"));
				zona.setDesZona(rs.getString("desZona"));
				
				lista.add(zona);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
