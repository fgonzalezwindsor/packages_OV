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
import org.openvia.inacatalog.itarifaslins.ITarifasLinsImp;
import org.openvia.inacatalog.itarifaslins.ITarifasLinsModel;
import org.openvia.inacatalog.itarifaslins.I_iTarifasLins;

public class ApiITarifasLins extends SvrProcess {
	
	I_iTarifasLins apiTarifasLins = new ITarifasLinsImp();	
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
		for (ITarifasLinsModel itarLin : listarTarifasLins()) {
			// Buscar si tarifalin existe
			if (apiTarifasLins.apiGetTarifaLin(itarLin.getCodEmpresa(), itarLin.getCodTarifa(), itarLin.getCodMagnitud(), itarLin.getCodArticulo(), itarLin.getCanMinima()) == null) {
				// Crear TarifaLin
				System.out.println("Crear TarifaLin: " + itarLin.getCodTarifa() + " " + itarLin.getCodMagnitud() + " " + itarLin.getCodArticulo() + " " + itarLin.getCanMinima());
				if (apiTarifasLins.apiPostTarifaLin(itarLin))
					ret = "TarifaLin insertada correctamente";
				else 
					ret = "Error al insertar TarifaLin";
			} else {
				System.out.println("Actualiza TarifaLin codArticulo: " + itarLin.getCodArticulo() + " codTarifa: " + itarLin.getCodTarifa());
				if (apiTarifasLins.apiPutTarifaLin(itarLin))
					ret = "TarifaLin actualizada";
				else
					ret = "Error al actualizar TarifaLin";
			}
		}
	
		return ret;
	}
	
	private List<ITarifasLinsModel> listarTarifasLins() {
		List<ITarifasLinsModel> lista = new ArrayList<ITarifasLinsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM ITarifasLin", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				ITarifasLinsModel tarLin = new ITarifasLinsModel();
				tarLin.setCodEmpresa(rs.getInt("codEmpresa"));
				tarLin.setCodTarifa(rs.getString("codTarifa"));
				tarLin.setCodMagnitud(rs.getString("codMagnitud"));
				tarLin.setCodArticulo(rs.getString("codArticulo"));
				tarLin.setCanMinima(rs.getDouble("canMinima"));
				tarLin.setPreArticulo(rs.getDouble("preArticulo"));
				tarLin.setFlaPreMagnitud(rs.getString("flaPreMagnitud"));
				tarLin.setTpcDto01Def(rs.getDouble("tpcDto01Def"));
				tarLin.setTpcDto02Def(rs.getDouble("tpcDto02Def"));
				tarLin.setTpcDto01Max(rs.getDouble("tpcDto01Max"));
				tarLin.setTpcDto02Max(rs.getDouble("tpcDto02Max"));
				tarLin.setPuntosSinDto(rs.getDouble("tpcDto02Max"));
				tarLin.setPuntosConDto(rs.getDouble("PuntosConDto"));
				tarLin.setFlaPuntosUnitarios(rs.getString("flaPuntosUnitarios"));
				
				lista.add(tarLin);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
