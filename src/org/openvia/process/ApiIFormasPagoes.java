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
import org.openvia.inacatalog.iformaspagoes.IFormasPagoesImp;
import org.openvia.inacatalog.iformaspagoes.IFormasPagoesModel;
import org.openvia.inacatalog.iformaspagoes.I_iFormasPagoes;
import org.openvia.inacatalog.itarifas.ITarifasImp;
import org.openvia.inacatalog.itarifas.ITarifasModel;
import org.openvia.inacatalog.itarifas.I_iTarifas;

public class ApiIFormasPagoes extends SvrProcess {
	
	I_iFormasPagoes apiFormasPagoes = new IFormasPagoesImp();	
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
		for (IFormasPagoesModel formPago : listarFormasPagoses()) {
			// Buscar si FormaPago existe
			if (apiFormasPagoes.apiGetFormaPago(formPago.getCodEmpresa(), formPago.getCodFormaPago()) == null) {
				// Crear FormaPago
				System.out.println("Crear FormaPagoes: " + formPago.getCodFormaPago());
				if (apiFormasPagoes.apiPostTarifa(formPago))
					ret = "FormaPagoes insertada correctamente";
				else 
					ret = "Error al insertar FormaPagoes";
			} else {
				System.out.println("FormaPagoes ya existe: " + formPago.getCodFormaPago());
			}
		}
	
		return ret;
	}
	
	private List<IFormasPagoesModel> listarFormasPagoses() {
		List<IFormasPagoesModel> lista = new ArrayList<IFormasPagoesModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IFormasDePago", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IFormasPagoesModel formPago = new IFormasPagoesModel();
				formPago.setCodEmpresa(rs.getInt("codEmpresa"));
				formPago.setCodFormaPago(rs.getString("codFormaPago"));
				formPago.setDesFormaPago(rs.getString("desFormaPago"));
				
				lista.add(formPago);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
