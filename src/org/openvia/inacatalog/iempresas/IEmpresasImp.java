package org.openvia.inacatalog.iempresas;

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

public class IEmpresasImp extends Common implements I_iEmpresas{

	@Override
	public IEmpresasModel apiGetEmpresa(Integer empresa) {
		JSONArray jsonArray = null;
		IEmpresasModel emp = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iEmpresas?empresa=" + empresa);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	emp = new IEmpresasModel();
		    	emp.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	emp.setNomEmpresa(jsonObject.get(COLUMNA_NOMEMPRESA).toString());
		    	emp.setRsoEmpresa(jsonObject.get(COLUMNA_RSOEMPRESA).toString());
		    	emp.setCifEmpresa(jsonObject.get(COLUMNA_CIFEMPRESA).toString());
		    	emp.setDatCalleEmpresa(jsonObject.get(COLUMNA_DATCALLEEMPRESA).toString());
		    	emp.setCodPostalEmpresa(jsonObject.get(COLUMNA_CODPOSTALEMPRESA).toString());
		    	emp.setDatPoblacionEmpresa(jsonObject.get(COLUMNA_DATPOBLACIONEMPRESA).toString());
		    	emp.setDatProvinciaEmpresa(jsonObject.get(COLUMNA_DATPROVINCIAEMPRESA).toString());
		    	emp.setDatPaisEmpresa(jsonObject.get(COLUMNA_DATPAISEMPRESA).toString());
		    	emp.setDatTelefonoEmpresa(jsonObject.get(COLUMNA_DATTELEFONOEMPRESA).toString());
		    	emp.setDatFaxEmpresa(jsonObject.get(COLUMNA_DATFAXEMPRESA).toString());
		    	emp.setDatEmailEmpresa(jsonObject.get(COLUMNA_DATEMAILEMPRESA).toString());
		    	emp.setHipWebEmpresa(jsonObject.get(COLUMNA_HIPWEBEMPRESA).toString());
		    	emp.setDatColetillaPedido(jsonObject.get(COLUMNA_DATCOLETILLAPEDIDO).toString());
		    	emp.setFlaEmpSuministradora(jsonObject.get(COLUMNA_FLAEMPSUMINISTRADORA).toString());
		    	emp.setFlaImgModificada(jsonObject.get(COLUMNA_FLAIMGMODIFICADA).toString());
		    }
		}
		return emp;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostEmpresa(IEmpresasModel emp) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iEmpresas";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(emp.getCodEmpresa()));
			jsonObj.put(COLUMNA_NOMEMPRESA, stringNotNull(emp.getNomEmpresa()));
			jsonObj.put(COLUMNA_RSOEMPRESA, stringNotNull(emp.getRsoEmpresa()));
			jsonObj.put(COLUMNA_CIFEMPRESA, stringNotNull(emp.getCifEmpresa()));
			jsonObj.put(COLUMNA_DATCALLEEMPRESA, stringNotNull(emp.getDatCalleEmpresa()));
			jsonObj.put(COLUMNA_CODPOSTALEMPRESA, stringNotNull(emp.getCodPostalEmpresa()));
			jsonObj.put(COLUMNA_DATPOBLACIONEMPRESA, stringNotNull(emp.getDatPoblacionEmpresa()));
			jsonObj.put(COLUMNA_DATPROVINCIAEMPRESA	, stringNotNull(emp.getDatProvinciaEmpresa()));
			jsonObj.put(COLUMNA_DATPAISEMPRESA, stringNotNull(emp.getDatPaisEmpresa()));
			jsonObj.put(COLUMNA_DATTELEFONOEMPRESA, stringNotNull(emp.getDatTelefonoEmpresa()));
			jsonObj.put(COLUMNA_DATFAXEMPRESA, stringNotNull(emp.getDatFaxEmpresa()));
			jsonObj.put(COLUMNA_DATEMAILEMPRESA, stringNotNull(emp.getDatEmailEmpresa()));
			jsonObj.put(COLUMNA_HIPWEBEMPRESA, stringNotNull(emp.getHipWebEmpresa()));
			jsonObj.put(COLUMNA_DATCOLETILLAPEDIDO, stringNotNull(emp.getDatColetillaPedido()));
			jsonObj.put(COLUMNA_FLAEMPSUMINISTRADORA, stringNotNull(emp.getFlaEmpSuministradora()));
			jsonObj.put(COLUMNA_FLAIMGMODIFICADA, stringNotNull(emp.getFlaImgModificada()));
			

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
			registrarLog("iEmpresas", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
