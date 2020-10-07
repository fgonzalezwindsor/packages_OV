package org.openvia.inacatalog.icobros;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openvia.inacatalog.Common;

public class ICobrosImp extends Common implements I_iCobros{

	@Override
	public ICobrosModel apiGetCobro(Integer empresa, String codCliente, String codDocumento) {
		JSONArray jsonArray = null;
		ICobrosModel cobro = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iCobros?empresa=" + empresa + "&codcliente=" + codCliente + "&coddocumento=" + codDocumento);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	cobro = new ICobrosModel();
		    	cobro.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	cobro.setCodCliente(jsonObject.get(COLUMNA_CODCLIENTE).toString());
		    	cobro.setCodDocumento(jsonObject.get(COLUMNA_CODDOCUMENTO).toString());
		    	cobro.setFecDocumento(jsonObject.get(COLUMNA_FECDOCUMENTO).toString());
		    	cobro.setFecVencimiento(jsonObject.get(COLUMNA_FECVENCIMIENTO).toString());
		    	cobro.setDatTipoDocumento(jsonObject.get(COLUMNA_DATTIPODOCUMENTO).toString());
		    	cobro.setFlaImpagado(jsonObject.get(COLUMNA_FLAIMPAGADO).toString());
		    	cobro.setImpPendiente(Double.valueOf(jsonObject.get(COLUMNA_IMPPENDIENTE).toString()));
		    	cobro.setCodMoneda(jsonObject.get(COLUMNA_CODMONEDA).toString());
		    }
		}
		return cobro;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostCobro(ICobrosModel cobro) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iCobros";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(cobro.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODCLIENTE, stringNotNull(cobro.getCodCliente()));
			jsonObj.put(COLUMNA_CODDOCUMENTO, stringNotNull(cobro.getCodDocumento()));
			jsonObj.put(COLUMNA_FECDOCUMENTO, stringNotNull(cobro.getFecDocumento()));
			jsonObj.put(COLUMNA_FECVENCIMIENTO, stringNotNull(cobro.getFecVencimiento()));
			jsonObj.put(COLUMNA_DATTIPODOCUMENTO, stringNotNull(cobro.getDatTipoDocumento()));
			jsonObj.put(COLUMNA_FLAIMPAGADO, stringNotNull(cobro.getFlaImpagado()));
			jsonObj.put(COLUMNA_IMPPENDIENTE, doubleNotNull(cobro.getImpPendiente()));
			jsonObj.put(COLUMNA_CODMONEDA, stringNotNull(cobro.getCodMoneda()));

			requestEntity = new StringRequestEntity(jsonObj.toString(), "application/json", "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		postMethod.setRequestEntity(requestEntity);

		try {
			httpClient.executeMethod(postMethod);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (postMethod.getStatusCode() == HttpStatus.SC_CREATED) {
			ret = true;
//			ret = "Registro insertado";
//						        try {
////						        	System.out.println(postMethod.getResponseBodyAsString());
////									String resp = postMethod.getResponseBodyAsString();
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
		} else {
			registrarLog("iCobros", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
			System.out.println("Codigo Status " + postMethod.getStatusCode());
		}
		
		/* else {
			ret = "Error al insertar registro (Status Code: " + postMethod.getStatusCode() + " Status Text: "
					+ postMethod.getStatusText() + " Status Line: " + postMethod.getStatusLine() + ")";
		}*/
		/*
		 * else { for (int i=0; i<jsonArray.size(); i++) { JSONObject jsonObject =
		 * (JSONObject) jsonArray.get(i); jsonObject.get("codCatalogo");
		 * 
		 * System.out.println(jsonObject.get("codCatalogo")); } }
		 */
		return ret;
	}

}
