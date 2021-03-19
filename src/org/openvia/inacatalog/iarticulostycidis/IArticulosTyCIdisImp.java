package org.openvia.inacatalog.iarticulostycidis;

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

public class IArticulosTyCIdisImp extends Common implements I_iArticulosTyCIdis {

	@Override
	public IArticulosTyCIdisModel apiGetArticuloTyCIdi(Integer empresa, String codModeloTyC, String codColor, String codTalla, String codIdiomaDestino) {
		JSONArray jsonArray = null;
		IArticulosTyCIdisModel articuloTyCIdi = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iArticulosTyCIdis?empresa=" + empresa + "&codmodelotyc=" + codModeloTyC + "&codcolor=" + codColor + "&codtalla=" + codTalla + "&codidiomadestino=" + codIdiomaDestino);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	articuloTyCIdi = new IArticulosTyCIdisModel();
		    	articuloTyCIdi.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	articuloTyCIdi.setCodArticulo(jsonObject.get(COLUMNA_CODARTICULO).toString());
		    	articuloTyCIdi.setCodModeloTyC(jsonObject.get(COLUMNA_CODMODELOTYC).toString());
		    	articuloTyCIdi.setCodIdiomaDestino(jsonObject.get(COLUMNA_CODIDIOMADESTINO).toString());
		    	articuloTyCIdi.setCodColor(jsonObject.get(COLUMNA_CODCOLOR).toString());
		    	articuloTyCIdi.setDesColor(jsonObject.get(COLUMNA_DESCOLOR).toString());
		    	articuloTyCIdi.setCodTalla(jsonObject.get(COLUMNA_CODTALLA).toString());
		    	articuloTyCIdi.setDesTalla(jsonObject.get(COLUMNA_DESTALLA).toString());
		    	articuloTyCIdi.setDesArticulo(jsonObject.get(COLUMNA_DESARTICULO).toString());
		    	articuloTyCIdi.setDatMedidas(jsonObject.get(COLUMNA_DATMEDIDAS).toString());
		    	articuloTyCIdi.setDatPeso(jsonObject.get(COLUMNA_DATPESO).toString());
		    	articuloTyCIdi.setDatVolumen(jsonObject.get(COLUMNA_DATVOLUMEN).toString());
		    	articuloTyCIdi.setDatFechaEntradaPrevista(jsonObject.get(COLUMNA_DATFECHAENTRADAPREVISTA).toString());
		    }
		}
		return articuloTyCIdi;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostArticuloTyCIdi(IArticulosTyCIdisModel artTyCIdi) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulosTyCIdis";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(artTyCIdi.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(artTyCIdi.getCodArticulo()));
			jsonObj.put(COLUMNA_CODMODELOTYC, stringNotNull(artTyCIdi.getCodModeloTyC()));
			jsonObj.put(COLUMNA_CODIDIOMADESTINO, stringNotNull(artTyCIdi.getCodIdiomaDestino()));
			jsonObj.put(COLUMNA_CODCOLOR, stringNotNull(artTyCIdi.getCodColor()));
			jsonObj.put(COLUMNA_DESCOLOR, stringNotNull(artTyCIdi.getDesColor()));
			jsonObj.put(COLUMNA_CODTALLA, stringNotNull(artTyCIdi.getCodTalla()));
			jsonObj.put(COLUMNA_DESTALLA, stringNotNull(artTyCIdi.getDesTalla()));
			jsonObj.put(COLUMNA_DESARTICULO, stringNotNull(artTyCIdi.getDesArticulo()));
			jsonObj.put(COLUMNA_DATMEDIDAS, stringNotNull(artTyCIdi.getDatMedidas()));
			jsonObj.put(COLUMNA_DATPESO, stringNotNull(artTyCIdi.getDatPeso()));
			jsonObj.put(COLUMNA_DATVOLUMEN, stringNotNull(artTyCIdi.getDatVolumen()));
			jsonObj.put(COLUMNA_DATFECHAENTRADAPREVISTA, stringNotNull(artTyCIdi.getDatFechaEntradaPrevista()));

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
			registrarLog("iArticulosTyCIdis", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
	public boolean apiPutArticuloTyCIdi(IArticulosTyCIdisModel articuloTyCIdi) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulosTyCIdis?empresa=" + articuloTyCIdi.getCodEmpresa() + "&codmodelotyc=" + articuloTyCIdi.getCodModeloTyC() + "&codcolor=" + articuloTyCIdi.getCodColor() + "&codtalla=" + articuloTyCIdi.getCodTalla() + "&codidiomadestino=" + articuloTyCIdi.getCodIdiomaDestino();
		PutMethod putMethod = new PutMethod(url.replaceAll(" ", "%20"));
		
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(articuloTyCIdi.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(articuloTyCIdi.getCodArticulo()));
			jsonObj.put(COLUMNA_CODMODELOTYC, stringNotNull(articuloTyCIdi.getCodModeloTyC()));
			jsonObj.put(COLUMNA_CODIDIOMADESTINO, stringNotNull(articuloTyCIdi.getCodIdiomaDestino()));
			jsonObj.put(COLUMNA_CODCOLOR, stringNotNull(articuloTyCIdi.getCodColor()));
			jsonObj.put(COLUMNA_DESCOLOR, stringNotNull(articuloTyCIdi.getDesColor()));
			jsonObj.put(COLUMNA_CODTALLA, stringNotNull(articuloTyCIdi.getCodTalla()));
			jsonObj.put(COLUMNA_DESTALLA, stringNotNull(articuloTyCIdi.getDesTalla()));
			jsonObj.put(COLUMNA_DESARTICULO, stringNotNull(articuloTyCIdi.getDesArticulo()));
			jsonObj.put(COLUMNA_DATMEDIDAS, stringNotNull(articuloTyCIdi.getDatMedidas()));
			jsonObj.put(COLUMNA_DATPESO, stringNotNull(articuloTyCIdi.getDatPeso()));
			jsonObj.put(COLUMNA_DATVOLUMEN, stringNotNull(articuloTyCIdi.getDatVolumen()));
			jsonObj.put(COLUMNA_DATFECHAENTRADAPREVISTA, stringNotNull(articuloTyCIdi.getDatFechaEntradaPrevista()));

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
			registrarLog("iArticulosTyCIdis", putMethod.getStatusCode(), putMethod.getStatusText(), requestEntity.getContent(), url, "PUT");
			System.out.println("Codigo Status " + putMethod.getStatusCode());
		}
		
		return ret;
	}

}
