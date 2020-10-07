package org.openvia.inacatalog.iarticuloslfams;

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
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openvia.inacatalog.Common;

public class IArticulosLFamsImp extends Common implements I_iArticulosLFams {

	@Override
	public IArticulosLFamsModel apiGetArticuloLFam(Integer empresa, String codArticulo, String codCatalogo, Integer codFamilia, Integer codSubFamilia) {
		JSONArray jsonArray = null;
		IArticulosLFamsModel articuloLFam = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iArticulosLFams?empresa=" + empresa + "&codarticulo=" + codArticulo + "&codcatalogo=" + codCatalogo + "&codfamilia=" + codFamilia + "&codsubfamilia=" + codSubFamilia);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				articuloLFam = new IArticulosLFamsModel();
				articuloLFam.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
				articuloLFam.setCodArticulo(jsonObject.get(COLUMNA_CODARTICULO).toString());
				articuloLFam.setCodCatalogo(jsonObject.get(COLUMNA_CODCATALOGO).toString());
				articuloLFam.setCodFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODFAMILIA).toString()));
				articuloLFam.setCodSubFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODSUBFAMILIA).toString()));
				articuloLFam.setOrdArticulo(Integer.parseInt(jsonObject.get(COLUMNA_ORDARTICULO).toString()));
			}
		}
		return articuloLFam;

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostArticuloLFam(IArticulosLFamsModel articuloLFam) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulosLFams";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(articuloLFam.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(articuloLFam.getCodArticulo()));
			jsonObj.put(COLUMNA_CODCATALOGO, stringNotNull(articuloLFam.getCodCatalogo()));
			jsonObj.put(COLUMNA_CODFAMILIA, integerNotNull(articuloLFam.getCodFamilia()));
			jsonObj.put(COLUMNA_CODSUBFAMILIA, integerNotNull(articuloLFam.getCodSubFamilia()));
			jsonObj.put(COLUMNA_ORDARTICULO, integerNotNull(articuloLFam.getOrdArticulo()));

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
			registrarLog("iArticulosLFams", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
			System.out.println("Codigo Status " + postMethod.getStatusCode());
		}
		
		/*
			 * else { ret = "Error al insertar registro (Status Code: " +
			 * postMethod.getStatusCode() + " Status Text: " + postMethod.getStatusText() +
			 * " Status Line: " + postMethod.getStatusLine() + ")"; }
			 */
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
	public boolean apiPutArticuloLFam(IArticulosLFamsModel articuloLFam) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulosLFams?empresa=" + articuloLFam.getCodEmpresa() + "&codarticulo=" + articuloLFam.getCodArticulo() + "&codcatalogo=" + articuloLFam.getCodCatalogo() + "&codfamilia=" + articuloLFam.getCodFamilia() + "&codsubfamilia=" + articuloLFam.getCodSubFamilia();
		PutMethod putMethod = new PutMethod(url.replaceAll(" ", "%20"));
		
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(articuloLFam.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(articuloLFam.getCodArticulo()));
			jsonObj.put(COLUMNA_CODCATALOGO, stringNotNull(articuloLFam.getCodCatalogo()));
			jsonObj.put(COLUMNA_CODFAMILIA, integerNotNull(articuloLFam.getCodFamilia()));
			jsonObj.put(COLUMNA_CODSUBFAMILIA, integerNotNull(articuloLFam.getCodSubFamilia()));
			jsonObj.put(COLUMNA_ORDARTICULO, integerNotNull(articuloLFam.getOrdArticulo()));

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
			registrarLog("iArticulosLFams", putMethod.getStatusCode(), putMethod.getStatusText(), requestEntity.getContent(), url, "PUT");
			System.out.println("Codigo Status " + putMethod.getStatusCode());
		}
		
		return ret;
	}

}
