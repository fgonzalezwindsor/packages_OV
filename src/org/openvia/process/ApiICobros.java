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
import org.openvia.inacatalog.icobros.ICobrosImp;
import org.openvia.inacatalog.icobros.ICobrosModel;
import org.openvia.inacatalog.icobros.I_iCobros;

public class ApiICobros extends SvrProcess {
	
	I_iCobros apiCobros = new ICobrosImp();	
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
		for (ICobrosModel icobr : listarCobros()) {
			// Buscar si cobro existe
			if (apiCobros.apiGetCobro(icobr.getCodEmpresa(), icobr.getCodCliente(), icobr.getCodDocumento()) == null) {
				// Crear Cobro
				System.out.println("Crear Cobro: " + icobr.getCodCliente() + " " + icobr.getCodDocumento());
				if (apiCobros.apiPostCobro(icobr))
					ret = "Cobro insertado correctamente";
				else 
					ret = "Error al insertar Cobro";
			} else {
				System.out.println("Cobro ya existe: " + icobr.getCodCliente() + " " + icobr.getCodDocumento());
			}
		}
	
		return ret;
	}
	
	private List<ICobrosModel> listarCobros() {
		List<ICobrosModel> lista = new ArrayList<ICobrosModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM ICobros", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				ICobrosModel cobro = new ICobrosModel();
				cobro.setCodEmpresa(rs.getInt("codEmpresa"));
				cobro.setCodCliente(rs.getString("codCliente"));
				cobro.setCodDocumento(rs.getString("codDocumento"));
				cobro.setFecDocumento(rs.getString("fecDocumento"));
				cobro.setFecVencimiento(rs.getString("fecVencimiento"));
				cobro.setDatTipoDocumento(rs.getString("datTipoDocumento"));
				cobro.setFlaImpagado(rs.getString("flaImpagado"));
				cobro.setImpPendiente(rs.getDouble("impPendiente"));
				cobro.setCodMoneda(rs.getString("codMoneda"));
				
				lista.add(cobro);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
