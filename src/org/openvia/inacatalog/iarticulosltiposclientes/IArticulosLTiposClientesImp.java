package org.openvia.inacatalog.iarticulosltiposclientes;

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

public class IArticulosLTiposClientesImp extends Common implements I_iArticulosLTiposClientes {

	@Override
	public IArticulosLTiposClientesModel apiGetArticuloLTipoCliente(Integer empresa, String codArticulo, String codTipoCliente) {
		JSONArray jsonArray = null;
		IArticulosLTiposClientesModel articuloLTipoCliente = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iArticulosLTiposClientes?empresa=" + empresa + "&codarticulo=" + codArticulo + "&codTipoCliente=" + codTipoCliente);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				articuloLTipoCliente = new IArticulosLTiposClientesModel();
				articuloLTipoCliente.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
				articuloLTipoCliente.setCodArticulo(jsonObject.get(COLUMNA_CODARTICULO).toString());
				articuloLTipoCliente.setCodTipoCliente(jsonObject.get(COLUMNA_CODTIPOCLIENTE).toString());
			}
		}
		return articuloLTipoCliente;

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostArticuloLTipoCliente(IArticulosLTiposClientesModel articuloLFam) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulosLTiposClientes";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(articuloLFam.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(articuloLFam.getCodArticulo()));
			jsonObj.put(COLUMNA_CODTIPOCLIENTE, stringNotNull(articuloLFam.getCodTipoCliente()));

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
			registrarLog("iArticulosLTiposClientes", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
	
}
