package org.openvia.inacatalog.ipedidoscentrals;

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

public class IPedidosCentralsImp extends Common implements I_iPedidosCentrals{

	@Override
	public IPedidosCentralsModel apiGetPedidoCentral(Integer empresa, String codPedido) {
		JSONArray jsonArray = null;
		IPedidosCentralsModel pedido = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iPedidosCentrals?empresa=" + empresa + "&codpedido=" + codPedido);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	pedido = new IPedidosCentralsModel();
		    	pedido.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	pedido.setCodPedido(jsonObject.get(COLUMNA_CODPEDIDO).toString());
		    	pedido.setFecPedido(jsonObject.get(COLUMNA_FECPEDIDO).toString());
		    	pedido.setCodCliente(jsonObject.get(COLUMNA_CODCLIENTE).toString());
		    	pedido.setLinDirCli(Integer.parseInt(jsonObject.get(COLUMNA_LINDIRCLI).toString()));
		    	pedido.setCodFormaPago(jsonObject.get(COLUMNA_CODFORMAPAGO).toString());
		    	pedido.setTpcDtoPp(Double.valueOf(jsonObject.get(COLUMNA_TPCDTOPP).toString()));
		    	pedido.setTpcDto03(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO03).toString()));
		    	pedido.setCodMoneda(jsonObject.get(COLUMNA_CODMONEDA).toString());
		    	pedido.setCodIncoterm(jsonObject.get(COLUMNA_CODINCOTERM).toString());
		    	pedido.setTotBrutoPed(Double.valueOf(jsonObject.get(COLUMNA_TOTBRUTOPED).toString()));
		    	pedido.setTotBaseImponiblePed(Double.valueOf(jsonObject.get(COLUMNA_TOTBASEIMPONIBLEPED).toString()));
		    	pedido.setTotIVAPed(Double.valueOf(jsonObject.get(COLUMNA_TOTIVAPED).toString()));
		    	pedido.setTotREPed(Double.valueOf(jsonObject.get(COLUMNA_TOTREPED).toString()));
		    	pedido.setDatFechaEntrega(jsonObject.get(COLUMNA_DATFECHAENTREGA).toString());
		    	pedido.setDatEstadoPedido(jsonObject.get(COLUMNA_DATESTADOPEDIDO).toString());
		    }
		}
		return pedido;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostPedidoCentral(IPedidosCentralsModel pedido) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iPedidosCentrals";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(pedido.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODPEDIDO, stringNotNull(pedido.getCodPedido()));
			jsonObj.put(COLUMNA_FECPEDIDO, stringNotNull(pedido.getFecPedido()));
			jsonObj.put(COLUMNA_CODCLIENTE, stringNotNull(pedido.getCodCliente()));
			jsonObj.put(COLUMNA_LINDIRCLI, integerNotNull(pedido.getLinDirCli()));
			jsonObj.put(COLUMNA_CODFORMAPAGO, stringNotNull(pedido.getCodFormaPago()));
			jsonObj.put(COLUMNA_TPCDTOPP, doubleNotNull(pedido.getTpcDtoPp()));
			jsonObj.put(COLUMNA_TPCDTO03, doubleNotNull(pedido.getTpcDto03()));
			jsonObj.put(COLUMNA_CODMONEDA, stringNotNull(pedido.getCodMoneda()));
			jsonObj.put(COLUMNA_CODINCOTERM, stringNotNull(pedido.getCodIncoterm()));
			jsonObj.put(COLUMNA_TOTBRUTOPED, doubleNotNull(pedido.getTotBrutoPed()));
			jsonObj.put(COLUMNA_TOTBASEIMPONIBLEPED, doubleNotNull(pedido.getTotBaseImponiblePed()));
			jsonObj.put(COLUMNA_TOTIVAPED, doubleNotNull(pedido.getTotIVAPed()));
			jsonObj.put(COLUMNA_TOTREPED, doubleNotNull(pedido.getTotREPed()));
			jsonObj.put(COLUMNA_DATFECHAENTREGA, stringNotNull(pedido.getDatFechaEntrega()));
			jsonObj.put(COLUMNA_DATESTADOPEDIDO, stringNotNull(pedido.getDatEstadoPedido()));

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
			registrarLog("iPedidosCentrals", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
