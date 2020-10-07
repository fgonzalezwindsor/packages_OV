package org.openvia.inacatalog.icatalogos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openvia.inacatalog.Common;


public class ICatalogosImp extends Common implements I_iCatalogos {

	@Override
	public ICatalogosModel apiGetCatalogo(Integer empresa, String codCatalogo) {
		JSONArray jsonArray = null;
		ICatalogosModel catalogo = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iCatalogos?empresa=" + empresa + "&codcatalogo=" + codCatalogo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	catalogo = new ICatalogosModel();
		    	catalogo.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	catalogo.setCodCatalogo(jsonObject.get(COLUMNA_CODCATALOGO).toString());
		    	catalogo.setDesCatalogo(jsonObject.get(COLUMNA_DESCATALOGO).toString());
		    	catalogo.setObsCatalogo(jsonObject.get(COLUMNA_OBSCATALOGO).toString());
		    	catalogo.setNomImagenCat(jsonObject.get(COLUMNA_NOMIMAGENCAT).toString());
		    	catalogo.setNomIconoCat(jsonObject.get(COLUMNA_NOMICONOCAT).toString());
		    	catalogo.setFlaIcoModificado(jsonObject.get(COLUMNA_FLAICOMODIFICADO).toString());
		    	catalogo.setFlaImgModificado(jsonObject.get(COLUMNA_FLAIMGMODIFICADO).toString());
		    	catalogo.setOrdCatalogo(Integer.parseInt(jsonObject.get(COLUMNA_ORDCATALOGO).toString()));
		    }
		}
		return catalogo;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostCatalogo(ICatalogosModel icat) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iCatalogos";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(icat.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODCATALOGO, stringNotNull(icat.getCodCatalogo()));
			jsonObj.put(COLUMNA_DESCATALOGO, stringNotNull(icat.getDesCatalogo()));
			jsonObj.put(COLUMNA_OBSCATALOGO, stringNotNull(icat.getObsCatalogo()));
			jsonObj.put(COLUMNA_NOMIMAGENCAT, stringNotNull(icat.getNomImagenCat()));
			jsonObj.put(COLUMNA_NOMICONOCAT, stringNotNull(icat.getNomIconoCat()));
			jsonObj.put(COLUMNA_FLAICOMODIFICADO, stringNotNull(icat.getFlaIcoModificado()));
			jsonObj.put(COLUMNA_FLAIMGMODIFICADO, stringNotNull(icat.getFlaImgModificado()));
			jsonObj.put(COLUMNA_ORDCATALOGO, integerNotNull(icat.getOrdCatalogo()));

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
			registrarLog("iCatalogos", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
