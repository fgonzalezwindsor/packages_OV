package org.openvia.inacatalog.itarifaslins;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openvia.inacatalog.Common;


public class ITarifasLinsImp extends Common implements I_iTarifasLins {

	@Override
	public ITarifasLinsModel apiGetTarifaLin(Integer empresa, String codTarifa, String codMagnitud, String codArticulo, Double canMinima) {
		JSONArray jsonArray = null;
		ITarifasLinsModel tarifaLin = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iTarifasLins?empresa=" + empresa + "&codtarifa=" + codTarifa + "&codmagnitud=" + codMagnitud + "&codarticulo=" + codArticulo + "&canminima=" + canMinima);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	tarifaLin = new ITarifasLinsModel();
		    	tarifaLin.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	tarifaLin.setCodTarifa(jsonObject.get(COLUMNA_CODTARIFA).toString());
		    	tarifaLin.setCodMagnitud(jsonObject.get(COLUMNA_CODMAGNITUD).toString());
		    	tarifaLin.setCodArticulo(jsonObject.get(COLUMNA_CODARTICULO).toString());
		    	tarifaLin.setCanMinima(Double.valueOf(jsonObject.get(COLUMNA_CANMINIMA).toString()));
		    	tarifaLin.setPreArticulo(Double.valueOf(jsonObject.get(COLUMNA_PREARTICULO).toString()));
		    	tarifaLin.setFlaPreMagnitud(jsonObject.get(COLUMNA_FLAPREMAGNITUD).toString());
		    	tarifaLin.setTpcDto01Def(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO01DEF).toString()));
		    	tarifaLin.setTpcDto02Def(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO02DEF).toString()));
		    	tarifaLin.setTpcDto01Max(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO01MAX).toString()));
		    	tarifaLin.setTpcDto02Max(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO02MAX).toString()));
		    	tarifaLin.setPuntosSinDto(Double.valueOf(jsonObject.get(COLUMNA_PUNTOSSINDTO).toString()));
		    	tarifaLin.setPuntosConDto(Double.valueOf(jsonObject.get(COLUMNA_PUNTOSCONDTO).toString()));
		    	tarifaLin.setFlaPuntosUnitarios(jsonObject.get(COLUMNA_FLAPUNTOSUNITARIOS).toString());
		    }
		}
		return tarifaLin;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostTarifaLin(ITarifasLinsModel itar) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iTarifasLins";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(itar.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODTARIFA, stringNotNull(itar.getCodTarifa()));
			jsonObj.put(COLUMNA_CODMAGNITUD, stringNotNull(itar.getCodMagnitud()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(itar.getCodArticulo()));
			jsonObj.put(COLUMNA_CANMINIMA, doubleNotNull(itar.getCanMinima()));
			jsonObj.put(COLUMNA_PREARTICULO, doubleNotNull(itar.getPreArticulo()));
			jsonObj.put(COLUMNA_FLAPREMAGNITUD, stringNotNull(itar.getFlaPreMagnitud()));
			jsonObj.put(COLUMNA_TPCDTO01DEF, doubleNotNull(itar.getTpcDto01Def()));
			jsonObj.put(COLUMNA_TPCDTO02DEF, doubleNotNull(itar.getTpcDto02Def()));
			jsonObj.put(COLUMNA_TPCDTO01MAX, doubleNotNull(itar.getTpcDto01Max()));
			jsonObj.put(COLUMNA_TPCDTO02MAX, doubleNotNull(itar.getTpcDto02Max()));
			jsonObj.put(COLUMNA_PUNTOSSINDTO, doubleNotNull(itar.getPuntosSinDto()));
			jsonObj.put(COLUMNA_PUNTOSCONDTO, doubleNotNull(itar.getPuntosConDto()));
			jsonObj.put(COLUMNA_FLAPUNTOSUNITARIOS, stringNotNull(itar.getFlaPuntosUnitarios()));
			
			
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
			registrarLog("iTarifasLins", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPutTarifaLin(ITarifasLinsModel itar) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iTarifasLins?empresa=" + itar.getCodEmpresa() + "&codtarifa=" + itar.getCodTarifa() + "&codmagnitud=" + itar.getCodMagnitud() + "&codarticulo=" + itar.getCodArticulo() + "&canminima=" + itar.getCanMinima();
		PutMethod putMethod = new PutMethod(url.replaceAll(" ", "%20"));

		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(itar.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODTARIFA, stringNotNull(itar.getCodTarifa()));
			jsonObj.put(COLUMNA_CODMAGNITUD, stringNotNull(itar.getCodMagnitud()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(itar.getCodArticulo()));
			jsonObj.put(COLUMNA_CANMINIMA, doubleNotNull(itar.getCanMinima()));
			jsonObj.put(COLUMNA_PREARTICULO, doubleNotNull(itar.getPreArticulo()));
			jsonObj.put(COLUMNA_FLAPREMAGNITUD, stringNotNull(itar.getFlaPreMagnitud()));
			jsonObj.put(COLUMNA_TPCDTO01DEF, doubleNotNull(itar.getTpcDto01Def()));
			jsonObj.put(COLUMNA_TPCDTO02DEF, doubleNotNull(itar.getTpcDto02Def()));
			jsonObj.put(COLUMNA_TPCDTO01MAX, doubleNotNull(itar.getTpcDto01Max()));
			jsonObj.put(COLUMNA_TPCDTO02MAX, doubleNotNull(itar.getTpcDto02Max()));
			jsonObj.put(COLUMNA_PUNTOSSINDTO, doubleNotNull(itar.getPuntosSinDto()));
			jsonObj.put(COLUMNA_PUNTOSCONDTO, doubleNotNull(itar.getPuntosConDto()));
			jsonObj.put(COLUMNA_FLAPUNTOSUNITARIOS, stringNotNull(itar.getFlaPuntosUnitarios()));
			
			
			requestEntity = new StringRequestEntity(jsonObj.toString().replaceAll("\\\\", ""), "application/json", "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		putMethod.setRequestEntity(requestEntity);
		
		try {
			httpClient.executeMethod(putMethod);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (putMethod.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
			ret = true;
		} else {
			registrarLog("iTarifasLins", putMethod.getStatusCode(), putMethod.getStatusText(), requestEntity.getContent(), url, "PUT");
			System.out.println("Codigo Status " + putMethod.getStatusCode());
		}
		
		return ret;
	}
	
}
