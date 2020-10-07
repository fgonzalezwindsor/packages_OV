package org.openvia.inacatalog.itarifas;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openvia.inacatalog.Common;


public class ITarifasImp extends Common implements I_iTarifas {

	@Override
	public ITarifasModel apiGetTarifa(Integer empresa, String codTarifa) {
		JSONArray jsonArray = null;
		ITarifasModel tarifa = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iTarifas?empresa=" + empresa + "&codtarifa=" + codTarifa);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	tarifa = new ITarifasModel();
		    	tarifa.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	tarifa.setCodTarifa(jsonObject.get(COLUMNA_CODTARIFA).toString());
		    	tarifa.setDesTarifa(jsonObject.get(COLUMNA_DESTARIFA).toString());
		    	tarifa.setCodIncoterm(jsonObject.get(COLUMNA_CODINCOTERM).toString());
		    	tarifa.setFlaIVAIncluido(jsonObject.get(COLUMNA_FLAIVAINCLUIDO).toString());
		    	tarifa.setCodMoneda(jsonObject.get(COLUMNA_CODMONEDA).toString());
		    }
		}
		return tarifa;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostTarifa(ITarifasModel itar) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iTarifas";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(itar.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODTARIFA, stringNotNull(itar.getCodTarifa()));
			jsonObj.put(COLUMNA_DESTARIFA, stringNotNull(itar.getDesTarifa()));
			jsonObj.put(COLUMNA_CODINCOTERM, stringNotNull(itar.getCodIncoterm()));
			jsonObj.put(COLUMNA_FLAIVAINCLUIDO, stringNotNull(itar.getFlaIVAIncluido()));
			jsonObj.put(COLUMNA_CODMONEDA, stringNotNull(itar.getCodMoneda()));
			
			requestEntity = new StringRequestEntity(jsonObj.toString(), "application/json", "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		postMethod.setRequestEntity(requestEntity);

		try {
			httpClient.executeMethod(postMethod);
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
			registrarLog("iTarifas", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
