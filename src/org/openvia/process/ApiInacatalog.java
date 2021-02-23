package org.openvia.process;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.compiere.process.SvrProcess;

public class ApiInacatalog extends SvrProcess {
	// Insertar Clientes desde inacatalog
	ReadInaCatalog readInacatalog = new ReadInaCatalog();
	LimpiaInaCatalog limpiar = new LimpiaInaCatalog();
	ApiICatalogos iCatalogos = new ApiICatalogos();
	ApiIAgentes iAgentes = new ApiIAgentes();
	ApiIAgentesLCats iAgentesLCats = new ApiIAgentesLCats();
	ApiIAgentesLTars iAgentesLTars = new ApiIAgentesLTars();
	ApiIFamilias iFamilias = new ApiIFamilias();
	ApiIZonas iZonas = new ApiIZonas();
	ApiISectores iSectores = new ApiISectores();
	ApiITiposClientes iTiposClientes = new ApiITiposClientes();
	ApiIFormasPagoes iFormasPagoses = new ApiIFormasPagoes();
	ApiIClientes iClientes = new ApiIClientes();
	ApiIClientesLDirs iClientesLDirs = new ApiIClientesLDirs();
	ApiIClientesLContactos iClientesLContactos = new ApiIClientesLContactos();
	ApiITarifas iTarifas = new ApiITarifas();
	ApiIArticulos iArticulos = new ApiIArticulos();
	ApiIArticulosLTiposClientes iArticulosLTiposClientes = new ApiIArticulosLTiposClientes();
	ApiIArticulosTyCs iArticulosTyCs = new ApiIArticulosTyCs();
	ApiIArticulosLFams iArticulosLFams = new ApiIArticulosLFams();
	ApiITarifasLins iTarifasLins = new ApiITarifasLins();
	//ApiICobros iCobros = new ApiICobros();
	//ApiIPedidosCentrals iPedidosCentrals = new ApiIPedidosCentrals();
	//ApiIPedidosCentralLins iPedidosCentralLins = new ApiIPedidosCentralLins();
	
	ConexioDBInaCatalog connInacatalog = new ConexioDBInaCatalog();
	ConexioDBInaCatalogWind connInacatalogWind = new ConexioDBInaCatalogWind();

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String doIt() throws Exception {
		readInacatalog.insertarClientes();
		
		String semaforo = null;
		Connection conn = connInacatalog.openConection();
		Connection connWind = connInacatalogWind.openConection();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		PreparedStatement pst = conn.prepareStatement("SELECT datValor FROM Windsor_Semaforo WHERE codParametro = 'SemaforoInaCatalog'");
		ResultSet rs = pst.executeQuery();
		if (rs.next()) {
			semaforo = rs.getString("datValor");
		}
		
		if (semaforo.substring(0, semaforo.indexOf(":")).equals("Verde")) {
			// Actualizamos a Rojo
			pst = conn.prepareStatement("UPDATE Windsor_Semaforo SET datValor = 'Rojo:Windsor-" + format.format(new Date()) + "' WHERE codParametro = 'SemaforoInaCatalog'");
			pst.execute();
			pst.close();
			connInacatalog.closeConection(conn);
			
			// Comienza migracion Inacatalog
			System.out.println("Inicio limpiar...");
			limpiar.doIt();
			System.out.println("Fin limpiar");
			
			System.out.println("Inicio iCatalogos...");
			iCatalogos.doIt();
			System.out.println("Fin iCatalogos");
			
			System.out.println("Inicio iAgentes...");
			iAgentes.doIt();
			System.out.println("Fin iAgentes");
			
			System.out.println("Inicio iAgentesLCats...");
			iAgentesLCats.doIt();
			System.out.println("Fin iAgentesLCats");
			
			System.out.println("Inicio iTarifas...");
			iTarifas.doIt();
			System.out.println("Fin iTarifas");
			
			System.out.println("Inicio iAgentesLTars...");
			iAgentesLTars.doIt();
			System.out.println("Fin iAgentesLTars");
			
			System.out.println("Inicio iFamilias...");
			iFamilias.doIt();
			System.out.println("Fin iFamilias");
			
			System.out.println("Inicio iZonas...");
			iZonas.doIt();
			System.out.println("Fin iZonas");
			
			System.out.println("Inicio iSectores...");
			iSectores.doIt();
			System.out.println("Fin iSectores");
			
			System.out.println("Inicio iTiposClientes...");
			iTiposClientes.doIt();
			System.out.println("Fin iTiposClientes");
			
			System.out.println("Inicio iFormasPagoses...");
			iFormasPagoses.doIt();
			System.out.println("Fin iFormasPagoses");
			
			System.out.println("Inicio iClientes...");
			iClientes.doIt();
			System.out.println("Fin iClientes");
			
			System.out.println("Inicio iClientesLDirs...");
			iClientesLDirs.doIt();
			System.out.println("Fin iClientesLDirs");
			
			System.out.println("Inicio iClientesLContactos...");
			iClientesLContactos.doIt();
			System.out.println("Fin iClientesLContactos");
			
			System.out.println("Inicio iArticulos...");
			iArticulos.doIt();
			System.out.println("Fin iArticulos");
			
			System.out.println("Inicio iArticulosLTiposClientes...");
			iArticulosLTiposClientes.doIt();
			System.out.println("Fin iArticulosLTiposClientes");
			
			System.out.println("Inicio iArticulosTyCs...");
			iArticulosTyCs.doIt();
			System.out.println("Fin iArticulosTyCs");
			
			System.out.println("Inicio iArticulosLFams...");
			iArticulosLFams.doIt();
			System.out.println("Fin iArticulosLFams");
			
			System.out.println("Inicio iTarifasLins...");
			iTarifasLins.doIt();
			System.out.println("Fin iTarifasLins");
			
//			System.out.println("Inicio iCobros...");
//			iCobros.doIt();
//			System.out.println("Fin iCobros");
//			
//			System.out.println("Inicio iPedidosCentrals...");
//			iPedidosCentrals.doIt();
//			System.out.println("Fin iPedidosCentrals");
//			
//			System.out.println("Inicio iPedidosCentralLins...");
//			iPedidosCentralLins.doIt();
//			System.out.println("Fin iPedidosCentralLins");
			
			// Fin migracion Inacatalog
			connWind = connInacatalogWind.openConection();
			pst = connWind.prepareStatement("EXEC SPI_PreciosLiquidacion");
			pst.execute();
			pst.close();
			connInacatalogWind.closeConection(connWind);
			
			conn = connInacatalog.openConection();
			pst = conn.prepareStatement("UPDATE Windsor_Semaforo SET datValor = 'Verde:Windsor-" + format.format(new Date()) + "' WHERE codParametro = 'SemaforoInaCatalog'");
			pst.execute();
			pst.close();
			connInacatalog.closeConection(conn);
		}
		
		return "Inacatalog Importados";
	}
	
	class ConexioDBInaCatalog {
		private Connection openConection() throws Exception {
			Connection conn = null;
			conn = DriverManager.getConnection("jdbc:sqlserver://190.215.113.91:1433;database=inaSAM;user=Windsor;password=Windsor;loginTimeout=30;"); 
			return conn;
		}
		private void closeConection(Connection conn) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class ConexioDBInaCatalogWind {
		private Connection openConection() throws Exception {
			Connection conn = null;
			conn = DriverManager.getConnection("jdbc:sqlserver://190.215.113.91:1433;database=inaWINDSOR;user=Windsor;password=Windsor;loginTimeout=30;"); 
			return conn;
		}
		private void closeConection(Connection conn) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
