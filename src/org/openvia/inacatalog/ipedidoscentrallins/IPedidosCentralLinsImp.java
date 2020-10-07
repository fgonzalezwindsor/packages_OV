package org.openvia.inacatalog.ipedidoscentrallins;

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

public class IPedidosCentralLinsImp extends Common implements I_iPedidosCentralLins{

	@Override
	public IPedidosCentralLinsModel apiGetPedidoCentralLin(Integer empresa, String codPedido, Integer linPedido) {
		JSONArray jsonArray = null;
		IPedidosCentralLinsModel pedidoLin = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iPedidosCentralLins?empresa=" + empresa + "&codpedido=" + codPedido + "&linpedido=" + linPedido);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	pedidoLin = new IPedidosCentralLinsModel();
		    	pedidoLin.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	pedidoLin.setCodPedido(jsonObject.get(COLUMNA_CODPEDIDO).toString());
		    	pedidoLin.setLinPedido(Integer.parseInt(jsonObject.get(COLUMNA_LINPEDIDO).toString()));
		    	pedidoLin.setCodArticulo(jsonObject.get(COLUMNA_CODARTICULO).toString());
		    	pedidoLin.setDesLinPed(jsonObject.get(COLUMNA_DESLINPED).toString());
		    	pedidoLin.setCodMagnitud(jsonObject.get(COLUMNA_CODMAGNITUD).toString());
		    	pedidoLin.setCanLinPed(Double.valueOf(jsonObject.get(COLUMNA_CANLINPED).toString()));
		    	pedidoLin.setCanIndicada(Double.valueOf(jsonObject.get(COLUMNA_CANINDICADA).toString()));
		    	pedidoLin.setTpcDto01(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO01).toString()));
		    	pedidoLin.setTpcDto02(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO02).toString()));
		    	pedidoLin.setPreLinPed(Double.valueOf(jsonObject.get(COLUMNA_PRELINPED).toString()));
		    	pedidoLin.setImpBaseImponibleLinPed(Double.valueOf(jsonObject.get(COLUMNA_IMPBASEIMPONIBLELINPED).toString()));
		    	pedidoLin.setCodCatalogo(jsonObject.get(COLUMNA_CODCATALOGO).toString());
		    	pedidoLin.setCodFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODFAMILIA).toString()));
		    	pedidoLin.setCodSubFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODSUBFAMILIA).toString()));
		    	pedidoLin.setCanLinPedPte(Double.valueOf(jsonObject.get(COLUMNA_CANLINPEDPTE).toString()));
		    }
		}
		return pedidoLin;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostPedidoCentralLin(IPedidosCentralLinsModel pedidoLin) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iPedidosCentralLins";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(pedidoLin.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODPEDIDO, stringNotNull(pedidoLin.getCodPedido()));
			jsonObj.put(COLUMNA_LINPEDIDO, integerNotNull(pedidoLin.getLinPedido()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(pedidoLin.getCodArticulo()));
			jsonObj.put(COLUMNA_DESLINPED, stringNotNull(pedidoLin.getDesLinPed()));
			jsonObj.put(COLUMNA_CODMAGNITUD, stringNotNull(pedidoLin.getCodMagnitud()));
			jsonObj.put(COLUMNA_CANLINPED, doubleNotNull(pedidoLin.getCanLinPed()));
			jsonObj.put(COLUMNA_CANINDICADA, doubleNotNull(pedidoLin.getCanIndicada()));
			jsonObj.put(COLUMNA_TPCDTO01, doubleNotNull(pedidoLin.getTpcDto01()));
			jsonObj.put(COLUMNA_TPCDTO02, doubleNotNull(pedidoLin.getTpcDto02()));
			jsonObj.put(COLUMNA_PRELINPED, doubleNotNull(pedidoLin.getPreLinPed()));
			jsonObj.put(COLUMNA_IMPBASEIMPONIBLELINPED, doubleNotNull(pedidoLin.getImpBaseImponibleLinPed()));
			jsonObj.put(COLUMNA_CODCATALOGO, stringNotNull(pedidoLin.getCodCatalogo()));
			jsonObj.put(COLUMNA_CODFAMILIA, integerNotNull(pedidoLin.getCodFamilia()));
			jsonObj.put(COLUMNA_CODSUBFAMILIA, integerNotNull(pedidoLin.getCodSubFamilia()));
			jsonObj.put(COLUMNA_CANLINPEDPTE, doubleNotNull(pedidoLin.getCanLinPedPte()));

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
			registrarLog("iPedidosCentralLins", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
