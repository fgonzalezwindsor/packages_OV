package org.openvia.inacatalog.iarticulostycs;

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

public class IArticulosTyCsImp extends Common implements I_iArticulosTyCs {

	@Override
	public IArticulosTyCsModel apiGetArticuloTyC(Integer empresa, String codModeloTyC, String codColor, String codTalla) {
		JSONArray jsonArray = null;
		IArticulosTyCsModel articuloTyC = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iArticulosTyCs?empresa=" + empresa + "&codmodelotyc=" + codModeloTyC + "&codcolor=" + codColor + "&codtalla=" + codTalla);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	articuloTyC = new IArticulosTyCsModel();
		    	articuloTyC.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	articuloTyC.setCodArticulo(jsonObject.get(COLUMNA_CODARTICULO).toString());
		    	articuloTyC.setCodModeloTyC(jsonObject.get(COLUMNA_CODMODELOTYC).toString());
		    	articuloTyC.setCodColor(jsonObject.get(COLUMNA_CODCOLOR).toString());
		    	articuloTyC.setDesColor(jsonObject.get(COLUMNA_DESCOLOR).toString());
		    	articuloTyC.setCodTalla(jsonObject.get(COLUMNA_CODTALLA).toString());
		    	articuloTyC.setDesTalla(jsonObject.get(COLUMNA_DESTALLA).toString());
		    	articuloTyC.setDesArticulo(jsonObject.get(COLUMNA_DESARTICULO).toString());
		    	articuloTyC.setCodEAN13(jsonObject.get(COLUMNA_CODEAN13).toString());
		    	articuloTyC.setDatMedidas(jsonObject.get(COLUMNA_DATMEDIDAS).toString());
		    	articuloTyC.setDatPeso(jsonObject.get(COLUMNA_DATPESO).toString());
		    	articuloTyC.setDatVolumen(jsonObject.get(COLUMNA_DATVOLUMEN).toString());
		    	articuloTyC.setValMinVenta(Double.valueOf(jsonObject.get(COLUMNA_VALMINVENTA).toString()));
		    	articuloTyC.setValUniXCaja(Double.valueOf(jsonObject.get(COLUMNA_VALUNIXCAJA).toString()));
		    	articuloTyC.setValUniXPalet(Double.valueOf(jsonObject.get(COLUMNA_VALUNIXPALET).toString()));
		    	articuloTyC.setValUniIncSencillo(Double.valueOf(jsonObject.get(COLUMNA_VALUNIINCSENCILLO).toString()));
		    	articuloTyC.setStoDisponible(Double.valueOf(jsonObject.get(COLUMNA_STODISPONIBLE).toString()));
		    	articuloTyC.setStoPteRecibir(Double.valueOf(jsonObject.get(COLUMNA_STOPTERECIBIR).toString()));
		    	articuloTyC.setDatFechaEntradaPrevista(jsonObject.get(COLUMNA_DATFECHAENTRADAPREVISTA).toString());
		    	articuloTyC.setOrdTalla(Integer.parseInt(jsonObject.get(COLUMNA_ORDTALLA).toString()));
		    	articuloTyC.setOrdColor(Integer.parseInt(jsonObject.get(COLUMNA_ORDCOLOR).toString()));
		    	articuloTyC.setPreArticuloGen(Double.valueOf(jsonObject.get(COLUMNA_PREARTICULOGEN).toString()));
		    	articuloTyC.setCodSurtido(jsonObject.get(COLUMNA_CODSURTIDO).toString());
		    	articuloTyC.setFlaNoAplicarDtoPP(jsonObject.get(COLUMNA_FLANOAPLICARDTOPP).toString());
		    	articuloTyC.setDatMarcas(jsonObject.get(COLUMNA_DATMARCAS).toString());
		    	articuloTyC.setPrePuntos(Double.valueOf(jsonObject.get(COLUMNA_PREPUNTOS).toString()));
		    	articuloTyC.setFlaMuestra(jsonObject.get(COLUMNA_FLAMUESTRA).toString());
		    }
		}
		return articuloTyC;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostArticuloTyC(IArticulosTyCsModel artTyC) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulosTyCs";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(artTyC.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(artTyC.getCodArticulo()));
			jsonObj.put(COLUMNA_CODMODELOTYC, stringNotNull(artTyC.getCodModeloTyC()));
			jsonObj.put(COLUMNA_CODCOLOR, stringNotNull(artTyC.getCodColor()));
			jsonObj.put(COLUMNA_DESCOLOR, stringNotNull(artTyC.getDesColor()));
			jsonObj.put(COLUMNA_CODTALLA, stringNotNull(artTyC.getCodTalla()));
			jsonObj.put(COLUMNA_DESTALLA, stringNotNull(artTyC.getDesTalla()));
			jsonObj.put(COLUMNA_DESARTICULO, stringNotNull(artTyC.getDesArticulo()));
			jsonObj.put(COLUMNA_CODEAN13, stringNotNull(artTyC.getCodEAN13()));
			jsonObj.put(COLUMNA_DATMEDIDAS, stringNotNull(artTyC.getDatMedidas()));
			jsonObj.put(COLUMNA_DATPESO, stringNotNull(artTyC.getDatPeso()));
			jsonObj.put(COLUMNA_DATVOLUMEN, stringNotNull(artTyC.getDatVolumen()));
			jsonObj.put(COLUMNA_VALMINVENTA, doubleNotNull(artTyC.getValMinVenta()));
			jsonObj.put(COLUMNA_VALUNIXCAJA, doubleNotNull(artTyC.getValUniXCaja()));
			jsonObj.put(COLUMNA_VALUNIXPALET, doubleNotNull(artTyC.getValUniXPalet()));
			jsonObj.put(COLUMNA_VALUNIINCSENCILLO, doubleNotNull(artTyC.getValUniIncSencillo()));
			jsonObj.put(COLUMNA_STODISPONIBLE, doubleNotNull(artTyC.getStoDisponible()));
			jsonObj.put(COLUMNA_STOPTERECIBIR, doubleNotNull(artTyC.getStoPteRecibir()));
			jsonObj.put(COLUMNA_DATFECHAENTRADAPREVISTA, stringNotNull(artTyC.getDatFechaEntradaPrevista()));
			jsonObj.put(COLUMNA_ORDTALLA, integerNotNull(artTyC.getOrdTalla()));
			jsonObj.put(COLUMNA_ORDCOLOR, integerNotNull(artTyC.getOrdColor()));
			jsonObj.put(COLUMNA_PREARTICULOGEN, doubleNotNull(artTyC.getPreArticuloGen()));
			jsonObj.put(COLUMNA_CODSURTIDO, stringNotNull(artTyC.getCodSurtido()));
			jsonObj.put(COLUMNA_FLANOAPLICARDTOPP, stringNotNull(artTyC.getFlaNoAplicarDtoPP()));
			jsonObj.put(COLUMNA_DATMARCAS, stringNotNull(artTyC.getDatMarcas()));
			jsonObj.put(COLUMNA_PREPUNTOS, doubleNotNull(artTyC.getPrePuntos()));
			jsonObj.put(COLUMNA_FLAMUESTRA, stringNotNull(artTyC.getFlaMuestra()));

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
			registrarLog("iArticulosTyCs", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
	public boolean apiPutArticuloTyC(IArticulosTyCsModel articuloTyC) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulosTyCs?empresa=" + articuloTyC.getCodEmpresa() + "&codmodelotyc=" + articuloTyC.getCodModeloTyC() + "&codcolor=" + articuloTyC.getCodColor() + "&codtalla=" + articuloTyC.getCodTalla();
		PutMethod putMethod = new PutMethod(url.replaceAll(" ", "%20"));
		
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(articuloTyC.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(articuloTyC.getCodArticulo()));
			jsonObj.put(COLUMNA_CODMODELOTYC, stringNotNull(articuloTyC.getCodModeloTyC()));
			jsonObj.put(COLUMNA_CODCOLOR, stringNotNull(articuloTyC.getCodColor()));
			jsonObj.put(COLUMNA_DESCOLOR, stringNotNull(articuloTyC.getDesColor()));
			jsonObj.put(COLUMNA_CODTALLA, stringNotNull(articuloTyC.getCodTalla()));
			jsonObj.put(COLUMNA_DESTALLA, stringNotNull(articuloTyC.getDesTalla()));
			jsonObj.put(COLUMNA_DESARTICULO, stringNotNull(articuloTyC.getDesArticulo()));
			jsonObj.put(COLUMNA_CODEAN13, stringNotNull(articuloTyC.getCodEAN13()));
			jsonObj.put(COLUMNA_DATMEDIDAS, stringNotNull(articuloTyC.getDatMedidas()));
			jsonObj.put(COLUMNA_DATPESO, stringNotNull(articuloTyC.getDatPeso()));
			jsonObj.put(COLUMNA_DATVOLUMEN, stringNotNull(articuloTyC.getDatVolumen()));
			jsonObj.put(COLUMNA_VALMINVENTA, doubleNotNull(articuloTyC.getValMinVenta()));
			jsonObj.put(COLUMNA_VALUNIXCAJA, doubleNotNull(articuloTyC.getValUniXCaja()));
			jsonObj.put(COLUMNA_VALUNIXPALET, doubleNotNull(articuloTyC.getValUniXPalet()));
			jsonObj.put(COLUMNA_VALUNIINCSENCILLO, doubleNotNull(articuloTyC.getValUniIncSencillo()));
			jsonObj.put(COLUMNA_STODISPONIBLE, doubleNotNull(articuloTyC.getStoDisponible()));
			jsonObj.put(COLUMNA_STOPTERECIBIR, doubleNotNull(articuloTyC.getStoPteRecibir()));
			jsonObj.put(COLUMNA_DATFECHAENTRADAPREVISTA, stringNotNull(articuloTyC.getDatFechaEntradaPrevista()));
			jsonObj.put(COLUMNA_ORDTALLA, integerNotNull(articuloTyC.getOrdTalla()));
			jsonObj.put(COLUMNA_ORDCOLOR, integerNotNull(articuloTyC.getOrdColor()));
			jsonObj.put(COLUMNA_PREARTICULOGEN, doubleNotNull(articuloTyC.getPreArticuloGen()));
			jsonObj.put(COLUMNA_CODSURTIDO, stringNotNull(articuloTyC.getCodSurtido()));
			jsonObj.put(COLUMNA_FLANOAPLICARDTOPP, stringNotNull(articuloTyC.getFlaNoAplicarDtoPP()));
			jsonObj.put(COLUMNA_DATMARCAS, stringNotNull(articuloTyC.getDatMarcas()));
			jsonObj.put(COLUMNA_PREPUNTOS, doubleNotNull(articuloTyC.getPrePuntos()));
			jsonObj.put(COLUMNA_FLAMUESTRA, stringNotNull(articuloTyC.getFlaMuestra()));

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
			registrarLog("iArticulosTyCs", putMethod.getStatusCode(), putMethod.getStatusText(), requestEntity.getContent(), url, "PUT");
			System.out.println("Codigo Status " + putMethod.getStatusCode());
		}
		
		return ret;
	}

}
