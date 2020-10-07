
package org.openvia.inacatalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.compiere.util.DB;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.compiere.process.SvrProcess;

public class Common {
	
	public JSONArray readJsonArrayFromUrl(String url) {
		InputStream is = null;
		JSONArray jsonArray = new JSONArray();

		try {
			JSONParser jsonParser = new JSONParser();
			is = new URL(url.replaceAll(" ", "%20")).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			Object obj = jsonParser.parse(rd);
			jsonArray.add(obj);
//			jsonArray = (JSONArray) obj;
		} catch (Exception e) {
//			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonArray;
	}
	
	public String stringNotNull(String str) {
		if (str==null)
			return "";
		else
			return str;
	}
	
	public Double doubleNotNull(Double dbl) {
		if (dbl==null)
			return new Double(0);
		else
			return dbl;
	}
	
	public Integer integerNotNull(Integer intg) {
		if (intg == null) {
			return 0;
		} else {
			return intg;
		}
	}
	
	
	public void registrarLog(String tabla, int statusCode, String statusText, String body, String url, String metodo) {
		try {
			PreparedStatement pst = DB.prepareStatement("INSERT "
					+ "INTO ov_loginacatalog (OV_LOGINACATALOG_ID, TABLA, STATUS_CODE, STATUS_TEXT, IC_BODY, IC_URL, METODO) "
					+ "VALUES (SQ_OV_LOGINACATALOG.nextval, '" + tabla + "', '" + statusCode + "', '" + statusText + "', '" + body + "', '" + url + "', '" + metodo + "')", null);
			pst.execute();
			
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
