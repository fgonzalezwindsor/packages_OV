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
import org.openvia.inacatalog.ipedidoscentrals.IPedidosCentralsImp;
import org.openvia.inacatalog.ipedidoscentrals.IPedidosCentralsModel;
import org.openvia.inacatalog.ipedidoscentrals.I_iPedidosCentrals;

public class ApiIPedidosCentrals extends SvrProcess {
	
	I_iPedidosCentrals apiPedidosCentrals = new IPedidosCentralsImp();	
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
		for (IPedidosCentralsModel iped : listarPedidosCentrals()) {
			// Buscar si pedido existe
			if (apiPedidosCentrals.apiGetPedidoCentral(iped.getCodEmpresa(), iped.getCodPedido()) == null) {
				// Crear Pedido
				System.out.println("Crear Pedido: " + iped.getCodPedido());
				if (apiPedidosCentrals.apiPostPedidoCentral(iped))
					ret = "Pedido insertado correctamente";
				else 
					ret = "Error al insertar Pedido";
			} else {
				System.out.println("Pedido ya existe: " + iped.getCodPedido());
			}
		}
	
		return ret;
	}
	
	private List<IPedidosCentralsModel> listarPedidosCentrals() {
		List<IPedidosCentralsModel> lista = new ArrayList<IPedidosCentralsModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IPedidosCentral", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IPedidosCentralsModel ped = new IPedidosCentralsModel();
				ped.setCodEmpresa(rs.getInt("codEmpresa"));
				ped.setCodPedido(rs.getString("codPedido"));
				ped.setFecPedido(rs.getString("fecPedido"));
				ped.setCodCliente(rs.getString("codCliente"));
				ped.setLinDirCli(rs.getInt("linDirCli"));
				ped.setCodFormaPago(rs.getString("codFormaPago"));
				ped.setTpcDtoPp(rs.getDouble("tpcDtoPp"));
				ped.setTpcDto03(rs.getDouble("tpcDto03"));
				ped.setCodMoneda(rs.getString("codMoneda"));
				ped.setCodIncoterm(rs.getString("codIncoterm"));
				ped.setTotBrutoPed(rs.getDouble("totBrutoPed"));
				ped.setTotBaseImponiblePed(rs.getDouble("totBaseImponiblePed"));
				ped.setTotIVAPed(rs.getDouble("totIVAPed"));
				ped.setTotREPed(rs.getDouble("totREPed"));
				ped.setDatFechaEntrega(rs.getString("datFechaEntrega"));
				ped.setDatEstadoPedido(rs.getString("datEstadoPedido"));
				
				lista.add(ped);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
