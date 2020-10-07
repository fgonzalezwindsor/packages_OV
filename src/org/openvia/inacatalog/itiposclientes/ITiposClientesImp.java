package org.openvia.inacatalog.itiposclientes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openvia.inacatalog.Common;


public class ITiposClientesImp extends Common implements I_iTiposClientes {

	@Override
	public ITiposClientesModel apiGetTipoCliente(Integer empresa, String codTipoCliente) {
		JSONArray jsonArray = null;
		ITiposClientesModel tipoCliente = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iTiposClientes?empresa=" + empresa + "&codTipoCliente=" + codTipoCliente);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	tipoCliente = new ITiposClientesModel();
		    	tipoCliente.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	tipoCliente.setCodTipoCliente(jsonObject.get(COLUMNA_CODTIPOCLIENTE).toString());
		    	tipoCliente.setDesTipoCliente(jsonObject.get(COLUMNA_DESTIPOCLIENTE).toString());
		    }
		}
		return tipoCliente;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostTipoCliente(ITiposClientesModel tipoCliente) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iTiposClientes";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(tipoCliente.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODTIPOCLIENTE, stringNotNull(tipoCliente.getCodTipoCliente()));
			jsonObj.put(COLUMNA_DESTIPOCLIENTE, stringNotNull(tipoCliente.getDesTipoCliente()));
			
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
			registrarLog("iTiposClientes", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
