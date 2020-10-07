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
import org.openvia.inacatalog.ipedidoscentrallins.IPedidosCentralLinsImp;
import org.openvia.inacatalog.ipedidoscentrallins.IPedidosCentralLinsModel;
import org.openvia.inacatalog.ipedidoscentrallins.I_iPedidosCentralLins;
import org.openvia.inacatalog.ipedidoscentrals.IPedidosCentralsImp;
import org.openvia.inacatalog.ipedidoscentrals.IPedidosCentralsModel;
import org.openvia.inacatalog.ipedidoscentrals.I_iPedidosCentrals;

public class ApiIPedidosCentralLins extends SvrProcess {
	
	I_iPedidosCentralLins apiPedidosCentralLins = new IPedidosCentralLinsImp();	
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
		for (IPedidosCentralLinsModel ipedLin : listarPedidosCentralLins()) {
			// Buscar si pedido existe
			if (apiPedidosCentralLins.apiGetPedidoCentralLin(ipedLin.getCodEmpresa(), ipedLin.getCodPedido(), ipedLin.getLinPedido()) == null) {
				// Crear PedidoLin
				System.out.println("Crear PedidoLin: " + ipedLin.getCodPedido() + " Linea " + ipedLin.getLinPedido());
				if (apiPedidosCentralLins.apiPostPedidoCentralLin(ipedLin))
					ret = "PedidoLin insertado correctamente";
				else 
					ret = "Error al insertar PedidoLin";
			} else {
				System.out.println("PedidoLin ya existe: " + ipedLin.getCodPedido() + " Linea " + ipedLin.getLinPedido());
			}
		}
	
		return ret;
	}
	
	private List<IPedidosCentralLinsModel> listarPedidosCentralLins() {
		List<IPedidosCentralLinsModel> lista = new ArrayList<IPedidosCentralLinsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IPedidosCentralLin", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IPedidosCentralLinsModel pedLin = new IPedidosCentralLinsModel();
				pedLin.setCodEmpresa(rs.getInt("codEmpresa"));
				pedLin.setCodPedido(rs.getString("codPedido"));
				pedLin.setLinPedido(rs.getInt("linPedido"));
				pedLin.setCodArticulo(rs.getString("codArticulo"));
				pedLin.setDesLinPed(rs.getString("desLinPed"));
				pedLin.setCodMagnitud(rs.getString("codMagnitud"));
				pedLin.setCanLinPed(rs.getDouble("canLinPed"));
				pedLin.setCanIndicada(rs.getDouble("canIndicada"));
				pedLin.setTpcDto01(rs.getDouble("tpcDto01"));
				pedLin.setTpcDto02(rs.getDouble("tpcDto02"));
				pedLin.setPreLinPed(rs.getDouble("preLinPed"));
				pedLin.setImpBaseImponibleLinPed(rs.getDouble("impBaseImponibleLinPed"));
				pedLin.setCodCatalogo(rs.getString("codCatalogo"));
				pedLin.setCodFamilia(rs.getInt("codFamilia"));
				pedLin.setCodSubFamilia(rs.getInt("codSubFamilia"));
				pedLin.setCanLinPedPte(rs.getDouble("canLinPedPte"));
				
				lista.add(pedLin);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
