package org.openvia.inacatalog.iformaspagoes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openvia.inacatalog.Common;


public class IFormasPagoesImp extends Common implements I_iFormasPagoes {

	@Override
	public IFormasPagoesModel apiGetFormaPago(Integer empresa, String codFormaPago) {
		JSONArray jsonArray = null;
		IFormasPagoesModel formaPago = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iFormasPagoes?empresa=" + empresa + "&codformapago=" + codFormaPago);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	formaPago = new IFormasPagoesModel();
		    	formaPago.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	formaPago.setCodFormaPago(jsonObject.get(COLUMNA_CODFORMAPAGO).toString());
		    	formaPago.setDesFormaPago(jsonObject.get(COLUMNA_DESFORMAPAGO).toString());
		    }
		}
		return formaPago;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostTarifa(IFormasPagoesModel formPago) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iFormasPagoes";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(formPago.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODFORMAPAGO, stringNotNull(formPago.getCodFormaPago()));
			jsonObj.put(COLUMNA_DESFORMAPAGO, stringNotNull(formPago.getDesFormaPago()));
			
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
			registrarLog("iFormasPagoes", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
