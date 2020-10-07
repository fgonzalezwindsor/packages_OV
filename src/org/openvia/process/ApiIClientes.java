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
import org.openvia.inacatalog.iclientes.IClientesImp;
import org.openvia.inacatalog.iclientes.IClientesModel;
import org.openvia.inacatalog.iclientes.I_iClientes;

public class ApiIClientes extends SvrProcess {
	
	I_iClientes apiClientes = new IClientesImp();	
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
		for (IClientesModel cli : listarClientes()) {
			// Buscar si cliente existe
			if (apiClientes.apiGetCliente(cli.getCodEmpresa(), cli.getCodCliente()) == null) {
				// Crear Cliente
				System.out.println("Crear Cliente: " + cli.getCodCliente());
				if (apiClientes.apiPostCliente(cli))
					ret = "Cliente insertado correctamente";
				else 
					ret = "Error al insertar Cliente";
			} else {
				System.out.println("Cliente ya existe: " + cli.getCodCliente());
			}
		}
	
		return ret;
	}
	
	private List<IClientesModel> listarClientes() {
		List<IClientesModel> lista = new ArrayList<IClientesModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IClientes", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IClientesModel cli = new IClientesModel();
				cli.setCodEmpresa(rs.getInt("codEmpresa"));
				cli.setCodCliente(rs.getString("codCliente"));
				cli.setNomCliente(rs.getString("nomCliente"));
				cli.setRsoCliente(rs.getString("rsoCliente"));
				cli.setCifCliente(rs.getString("cifCliente"));
				cli.setCodZona(rs.getString("codZona"));
				cli.setCodAgente(rs.getString("codAgente"));
				cli.setCodTipoCliente(rs.getString("codTipoCliente"));
				cli.setTipIVA(rs.getString("tipIVA"));
				cli.setTpcDto01(rs.getDouble("tpcDto01"));
				cli.setTpcDto02(rs.getDouble("tpcDto02"));
				cli.setTpcDtoPp(rs.getDouble("tpcDtoPp"));
				cli.setCodFormaPago(rs.getString("codFormaPago"));
				cli.setFlaNvoCliente(rs.getString("flaNvoCliente"));
				cli.setFlaExpCliente(rs.getString("flaExpCliente"));
				cli.setCodTarifa(rs.getString("codTarifa"));
				cli.setCodGrupoPreciosCliente(rs.getString("codGrupoPreciosCliente"));
				cli.setFlaObsoleto(rs.getString("flaObsoleto"));
				cli.setImpPendienteRiesgo(rs.getDouble("impPendienteRiesgo"));
				cli.setImpVencidoRiesgo(rs.getDouble("impVencidoRiesgo"));
				cli.setImpImpagadoRiesgo(rs.getDouble("impImpagadoRiesgo"));
				cli.setImpCoberturaRiesgo(rs.getDouble("impCoberturaRiesgo"));
				cli.setFlaBloqueaClienteRiesgo(rs.getString("flaBloqueaClienteRiesgo"));
				cli.setCodMonedaRiesgo(rs.getString("codMonedaRiesgo"));
				cli.setCodIdioma(rs.getString("codIdioma"));
				cli.setDatIBAN(rs.getString("datIBAN"));
				cli.setCodSector(rs.getString("codSector"));
				cli.setDatBlog(rs.getString("datBlog"));
				cli.setImpFacturacion(rs.getDouble("impFacturacion"));
				cli.setObsClienteNoEdi(rs.getString("obsClienteNoEdi"));
				cli.setObsClienteEdi(rs.getString("obsClienteEdi"));
				cli.setCustom1(rs.getString("Custom1"));
				cli.setCustom2(rs.getString("Custom2"));
				cli.setCustom3(rs.getString("Custom3"));
				cli.setCustom4(rs.getString("Custom4"));
				cli.setCustom5(rs.getString("Custom5"));
				cli.setFecAltaCliente(rs.getString("fecAltaCliente"));
				cli.setCodMonedaRiesgo(rs.getString("codMonedaRiesgo"));
				
				lista.add(cli);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
