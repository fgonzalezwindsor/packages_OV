package org.openvia.process;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.compiere.process.SvrProcess;

public class ApiPedCentralsCobros extends SvrProcess {
	ApiICobros iCobros = new ApiICobros();
	ApiIPedidosCentrals iPedidosCentrals = new ApiIPedidosCentrals();
	ApiIPedidosCentralLins iPedidosCentralLins = new ApiIPedidosCentralLins();
	
	ConexioDBInaCatalog connInacatalog = new ConexioDBInaCatalog();

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String doIt() throws Exception {		
		String semaforo = null;
		Connection conn = connInacatalog.openConection();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		PreparedStatement pst = conn.prepareStatement("SELECT datValor FROM iParametros WHERE codParametro = 'SemaforoInaCatalog'");
		ResultSet rs = pst.executeQuery();
		if (rs.next()) {
			semaforo = rs.getString("datValor");
		}
		
		if (semaforo.substring(0, semaforo.indexOf(":")).equals("Verde")) {
			// Actualizamos a Rojo
			pst = conn.prepareStatement("UPDATE iParametros SET datValor = 'Rojo:Windsor-" + format.format(new Date()) + "' WHERE codParametro = 'SemaforoInaCatalog'");
			pst.execute();
			pst.close();
			connInacatalog.closeConection(conn);
			
			// Comienza migracion Inacatalog
			System.out.println("Inicio iCobros...");
			iCobros.doIt();
			System.out.println("Fin iCobros");
			
			System.out.println("Inicio iPedidosCentrals...");
			iPedidosCentrals.doIt();
			System.out.println("Fin iPedidosCentrals");
			
			System.out.println("Inicio iPedidosCentralLins...");
			iPedidosCentralLins.doIt();
			System.out.println("Fin iPedidosCentralLins");
			
			conn = connInacatalog.openConection();
			pst = conn.prepareStatement("UPDATE iParametros SET datValor = 'Verde:Windsor-" + format.format(new Date()) + "' WHERE codParametro = 'SemaforoInaCatalog'");
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

}
