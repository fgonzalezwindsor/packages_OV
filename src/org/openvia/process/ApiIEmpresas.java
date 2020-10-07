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
import org.openvia.inacatalog.iempresas.IEmpresasImp;
import org.openvia.inacatalog.iempresas.IEmpresasModel;
import org.openvia.inacatalog.iempresas.I_iEmpresas;

public class ApiIEmpresas extends SvrProcess {
	
	I_iEmpresas apiEmpresas = new IEmpresasImp();	
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
		for (IEmpresasModel iemp : listarAgentes()) {
			// Buscar si catalogo existe
			if (apiEmpresas.apiGetEmpresa(iemp.getCodEmpresa()) == null) {
				// Crear Empresa
				System.out.println("Crear Empresa: " + iemp.getCodEmpresa());
				if (apiEmpresas.apiPostEmpresa(iemp))
					ret = "Empresa insertado correctamente";
				else 
					ret = "Error al insertar Empresa";
			} else {
				System.out.println("Empresa ya existe: " + iemp.getCodEmpresa());
			}
		}
	
		return ret;
	}
	
	private List<IEmpresasModel> listarAgentes() {
		List<IEmpresasModel> lista = new ArrayList<IEmpresasModel>();
		PreparedStatement pst = DB.prepareStatement("SELECT * FROM IEmpresas", get_TrxName());
		ResultSet rs;
		try {
			rs = pst.executeQuery();
			while (rs.next()) {
				IEmpresasModel emp = new IEmpresasModel();
				emp.setCodEmpresa(rs.getInt("codEmpresa"));
				emp.setNomEmpresa(rs.getString("nomEmpresa"));
				emp.setRsoEmpresa(rs.getString("rsoEmpresa"));
				emp.setCifEmpresa(rs.getString("cifEmpresa"));
				emp.setDatCalleEmpresa(rs.getString("datCalleEmpresa"));
				emp.setCodPostalEmpresa(rs.getString("codPostalEmpresa"));
				emp.setDatPoblacionEmpresa(rs.getString("datPoblacionEmpresa"));
				emp.setDatProvinciaEmpresa(rs.getString("datProvinciaEmpresa"));
				emp.setDatPaisEmpresa(rs.getString("datPaisEmpresa"));
				emp.setDatTelefonoEmpresa(rs.getString("datTelefonoEmpresa"));
				emp.setDatFaxEmpresa(rs.getString("datFaxEmpresa"));
				emp.setDatEmailEmpresa(rs.getString("datEmailEmpresa"));
				emp.setHipWebEmpresa(rs.getString("hipWebEmpresa"));
				emp.setDatColetillaPedido(rs.getString("datColetillaPedido"));
				emp.setFlaEmpSuministradora(rs.getString("flaEmpSuministradora"));
				emp.setFlaImgModificada(rs.getString("flaImgModificada"));
				
				lista.add(emp);
			}
			
			pst.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	

}
