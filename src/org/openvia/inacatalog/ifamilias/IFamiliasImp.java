package org.openvia.inacatalog.ifamilias;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openvia.inacatalog.Common;

public class IFamiliasImp extends Common implements I_iFamilias {

	@Override
	public IFamiliasModel apiGetFamilia(Integer empresa, String codCatalogo, Integer codFamilia, Integer codSubFamilia) {
		JSONArray jsonArray = null;
		IFamiliasModel familia = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iFamilias?empresa=" + empresa + "&codcatalogo=" + codCatalogo + "&codfamilia=" + codFamilia + "&codsubfamilia=" + codSubFamilia);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	familia = new IFamiliasModel();
		    	familia.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	familia.setCodCatalogo(jsonObject.get(COLUMNA_CODCATALOGO).toString());
		    	familia.setCodFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODFAMILIA).toString()));
		    	familia.setCodSubFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODSUBFAMILIA).toString()));
		    	familia.setDesFamilia(jsonObject.get(COLUMNA_DESFAMILIA).toString());
		    	familia.setNomIcoFamilia(jsonObject.get(COLUMNA_NOMICOFAMILIA).toString());
		    	familia.setOrdFamilia(Integer.parseInt(jsonObject.get(COLUMNA_ORDFAMILIA).toString()));
		    	familia.setFlaIcoModificado(jsonObject.get(COLUMNA_FLAICOMODIFICADO).toString());
		    	familia.setObsFamilia(jsonObject.get(COLUMNA_OBSFAMILIA).toString());
		    	familia.setNomImagenFam(jsonObject.get(COLUMNA_NOMIMAGENFAM).toString());
		    	familia.setFlaImgModificado(jsonObject.get(COLUMNA_FLAIMGMODIFICADO).toString());
		    }
		}
		return familia;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostFamilia(IFamiliasModel ifam) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iFamilias";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(ifam.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODCATALOGO, stringNotNull(ifam.getCodCatalogo()));
			jsonObj.put(COLUMNA_CODFAMILIA, integerNotNull(ifam.getCodFamilia()));
			jsonObj.put(COLUMNA_CODSUBFAMILIA, integerNotNull(ifam.getCodSubFamilia()));
			jsonObj.put(COLUMNA_DESFAMILIA, stringNotNull(ifam.getDesFamilia()));
			jsonObj.put(COLUMNA_NOMICOFAMILIA, stringNotNull(ifam.getNomIcoFamilia()));
			jsonObj.put(COLUMNA_ORDFAMILIA, integerNotNull(ifam.getOrdFamilia()));
			jsonObj.put(COLUMNA_FLAICOMODIFICADO, stringNotNull(ifam.getFlaIcoModificado()));
			jsonObj.put(COLUMNA_OBSFAMILIA, stringNotNull(ifam.getObsFamilia()));
			jsonObj.put(COLUMNA_NOMIMAGENFAM, stringNotNull(ifam.getNomImagenFam()));
			jsonObj.put(COLUMNA_FLAIMGMODIFICADO, stringNotNull(ifam.getFlaImgModificado()));

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
			registrarLog("iFamilias", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
