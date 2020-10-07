package org.openvia.inacatalog.iarticulos;

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

public class IArticulosImp extends Common implements I_iArticulos {

	@Override
	public IArticulosModel apiGetArticulo(Integer empresa, String codArticulo) {
		JSONArray jsonArray = null;
		IArticulosModel articulo = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iArticulos?empresa=" + empresa + "&codArticulo=" + codArticulo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				articulo = new IArticulosModel();
				articulo.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
				articulo.setCodArticulo(jsonObject.get(COLUMNA_CODARTICULO).toString());
				articulo.setDesArticulo(jsonObject.get(COLUMNA_DESARTICULO).toString());
				articulo.setCodEAN13(jsonObject.get(COLUMNA_CODEAN13).toString());
				articulo.setDatMedidas(jsonObject.get(COLUMNA_DATMEDIDAS).toString());
				articulo.setDatPeso(jsonObject.get(COLUMNA_DATPESO).toString());
				articulo.setDatVolumen(jsonObject.get(COLUMNA_DATVOLUMEN).toString());
				articulo.setObsArticulo(jsonObject.get(COLUMNA_OBSARTICULO).toString());
				articulo.setHipArticulo(jsonObject.get(COLUMNA_HIPARTICULO).toString());
				articulo.setValMinVenta(Double.valueOf(jsonObject.get(COLUMNA_VALMINVENTA).toString()));
				articulo.setValUniXCaja(Double.valueOf(jsonObject.get(COLUMNA_VALUNIXCAJA).toString()));
				articulo.setValUniXPalet(Double.valueOf(jsonObject.get(COLUMNA_VALUNIXPALET).toString()));
				articulo.setValUniIncSencillo(Double.valueOf(jsonObject.get(COLUMNA_VALUNIINCSENCILLO).toString()));
				articulo.setCodTipoArticulo(jsonObject.get(COLUMNA_CODTIPOARTICULO).toString());
				articulo.setCodCatalogo(jsonObject.get(COLUMNA_CODCATALOGO).toString());
				articulo.setCodFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODFAMILIA).toString()));
				articulo.setCodSubFamilia(Integer.parseInt(jsonObject.get(COLUMNA_CODSUBFAMILIA).toString()));
				articulo.setCodGrupoPreciosArticulo(jsonObject.get(COLUMNA_CODGRUPOPRECIOSARTICULO).toString());
				articulo.setTpcIva(Double.valueOf(jsonObject.get(COLUMNA_TPCIVA).toString()));
				articulo.setTpcRe(Double.valueOf(jsonObject.get(COLUMNA_TPCRE).toString()));
				articulo.setTpcIGIC(Double.valueOf(jsonObject.get(COLUMNA_TPCIGIC).toString()));
				articulo.setCodModeloTyC(jsonObject.get(COLUMNA_CODMODELOTYC).toString());
				articulo.setDesModeloTyC(jsonObject.get(COLUMNA_DESMODELOTYC).toString());
				articulo.setStoDisponible(Double.valueOf(jsonObject.get(COLUMNA_STODISPONIBLE).toString()));
				articulo.setStoPteRecibir(Double.valueOf(jsonObject.get(COLUMNA_STOPTERECIBIR).toString()));
				articulo.setDatFechaEntradaPrevista(jsonObject.get(COLUMNA_DATFECHAENTRADAPREVISTA).toString());
				articulo.setOrdArticulo(Integer.parseInt(jsonObject.get(COLUMNA_ORDARTICULO).toString()));
				articulo.setPreArticuloGen(Double.valueOf(jsonObject.get(COLUMNA_PREARTICULOGEN).toString()));
				articulo.setDatNivel1(jsonObject.get(COLUMNA_DATNIVEL1).toString());
				articulo.setDatNivel2(jsonObject.get(COLUMNA_DATNIVEL2).toString());
				articulo.setCodEmpSuministradora(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPSUMINISTRADORA).toString()));
				articulo.setFlaNoAplicarDtoPP(jsonObject.get(COLUMNA_FLANOAPLICARDTOPP).toString());
				articulo.setDatMarcas(jsonObject.get(COLUMNA_DATMARCAS).toString());
				articulo.setPrePuntos(Double.valueOf(jsonObject.get(COLUMNA_PREPUNTOS).toString()));
				articulo.setFlaMuestra(jsonObject.get(COLUMNA_FLAMUESTRA).toString());
			}
		}
		return articulo;

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostArticulo(IArticulosModel articulo) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulos";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(articulo.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(articulo.getCodArticulo()));
			jsonObj.put(COLUMNA_DESARTICULO, stringNotNull(articulo.getDesArticulo()));
			jsonObj.put(COLUMNA_CODEAN13, stringNotNull(articulo.getCodEAN13()));
			jsonObj.put(COLUMNA_DATMEDIDAS, stringNotNull(articulo.getDatMedidas()));
			jsonObj.put(COLUMNA_DATPESO, stringNotNull(articulo.getDatPeso()));
			jsonObj.put(COLUMNA_DATVOLUMEN, stringNotNull(articulo.getDatVolumen()));
			jsonObj.put(COLUMNA_OBSARTICULO, stringNotNull(articulo.getObsArticulo()));
			jsonObj.put(COLUMNA_HIPARTICULO, stringNotNull(articulo.getHipArticulo()));
			jsonObj.put(COLUMNA_VALMINVENTA, doubleNotNull(articulo.getValMinVenta()));
			jsonObj.put(COLUMNA_VALUNIXCAJA, doubleNotNull(articulo.getValUniXCaja()));
			jsonObj.put(COLUMNA_VALUNIXPALET, doubleNotNull(articulo.getValUniXPalet()));
			jsonObj.put(COLUMNA_VALUNIINCSENCILLO, doubleNotNull(articulo.getValUniIncSencillo()));
			jsonObj.put(COLUMNA_CODTIPOARTICULO, stringNotNull(articulo.getCodTipoArticulo()));
			jsonObj.put(COLUMNA_CODCATALOGO, stringNotNull(articulo.getCodCatalogo()));
			jsonObj.put(COLUMNA_CODFAMILIA, integerNotNull(articulo.getCodFamilia()));
			jsonObj.put(COLUMNA_CODSUBFAMILIA, integerNotNull(articulo.getCodSubFamilia()));
			jsonObj.put(COLUMNA_CODGRUPOPRECIOSARTICULO, stringNotNull(articulo.getCodGrupoPreciosArticulo()));
			jsonObj.put(COLUMNA_TPCIVA, doubleNotNull(articulo.getTpcIva()));
			jsonObj.put(COLUMNA_TPCRE, doubleNotNull(articulo.getTpcRe()));
			jsonObj.put(COLUMNA_TPCIGIC, doubleNotNull(articulo.getTpcIGIC()));
			jsonObj.put(COLUMNA_CODMODELOTYC, stringNotNull(articulo.getCodModeloTyC()));
			jsonObj.put(COLUMNA_DESMODELOTYC, stringNotNull(articulo.getDesModeloTyC()));
			jsonObj.put(COLUMNA_STODISPONIBLE, doubleNotNull(articulo.getStoDisponible()));
			jsonObj.put(COLUMNA_STOPTERECIBIR, doubleNotNull(articulo.getStoPteRecibir()));
			jsonObj.put(COLUMNA_DATFECHAENTRADAPREVISTA, stringNotNull(articulo.getDatFechaEntradaPrevista()));
			jsonObj.put(COLUMNA_ORDARTICULO, integerNotNull(articulo.getOrdArticulo()));
			jsonObj.put(COLUMNA_PREARTICULOGEN, doubleNotNull(articulo.getPreArticuloGen()));
			jsonObj.put(COLUMNA_DATNIVEL1, stringNotNull(articulo.getDatNivel1()));
			jsonObj.put(COLUMNA_DATNIVEL2, stringNotNull(articulo.getDatNivel2()));
			jsonObj.put(COLUMNA_CODEMPSUMINISTRADORA, integerNotNull(articulo.getCodEmpSuministradora()));
			jsonObj.put(COLUMNA_FLANOAPLICARDTOPP, stringNotNull(articulo.getFlaNoAplicarDtoPP()));
			jsonObj.put(COLUMNA_DATMARCAS, stringNotNull(articulo.getDatMarcas()));
			jsonObj.put(COLUMNA_PREPUNTOS, doubleNotNull(articulo.getPrePuntos()));
			jsonObj.put(COLUMNA_FLAMUESTRA, stringNotNull(articulo.getFlaMuestra()));

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
			registrarLog("iArticulos", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
	public boolean apiPutArticulo(IArticulosModel articulo) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iArticulos?empresa=" + articulo.getCodEmpresa() + "&codarticulo=" + articulo.getCodArticulo();
		PutMethod putMethod = new PutMethod(url.replaceAll(" ", "%20"));
		
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(articulo.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODARTICULO, stringNotNull(articulo.getCodArticulo()));
			jsonObj.put(COLUMNA_DESARTICULO, stringNotNull(articulo.getDesArticulo()));
			jsonObj.put(COLUMNA_CODEAN13, stringNotNull(articulo.getCodEAN13()));
			jsonObj.put(COLUMNA_DATMEDIDAS, stringNotNull(articulo.getDatMedidas()));
			jsonObj.put(COLUMNA_DATPESO, stringNotNull(articulo.getDatPeso()));
			jsonObj.put(COLUMNA_DATVOLUMEN, stringNotNull(articulo.getDatVolumen()));
			jsonObj.put(COLUMNA_OBSARTICULO, stringNotNull(articulo.getObsArticulo()));
			jsonObj.put(COLUMNA_HIPARTICULO, stringNotNull(articulo.getHipArticulo()));
			jsonObj.put(COLUMNA_VALMINVENTA, doubleNotNull(articulo.getValMinVenta()));
			jsonObj.put(COLUMNA_VALUNIXCAJA, doubleNotNull(articulo.getValUniXCaja()));
			jsonObj.put(COLUMNA_VALUNIXPALET, doubleNotNull(articulo.getValUniXPalet()));
			jsonObj.put(COLUMNA_VALUNIINCSENCILLO, doubleNotNull(articulo.getValUniIncSencillo()));
			jsonObj.put(COLUMNA_CODTIPOARTICULO, stringNotNull(articulo.getCodTipoArticulo()));
			jsonObj.put(COLUMNA_CODCATALOGO, stringNotNull(articulo.getCodCatalogo()));
			jsonObj.put(COLUMNA_CODFAMILIA, integerNotNull(articulo.getCodFamilia()));
			jsonObj.put(COLUMNA_CODSUBFAMILIA, integerNotNull(articulo.getCodSubFamilia()));
			jsonObj.put(COLUMNA_CODGRUPOPRECIOSARTICULO, stringNotNull(articulo.getCodGrupoPreciosArticulo()));
			jsonObj.put(COLUMNA_TPCIVA, doubleNotNull(articulo.getTpcIva()));
			jsonObj.put(COLUMNA_TPCRE, doubleNotNull(articulo.getTpcRe()));
			jsonObj.put(COLUMNA_TPCIGIC, doubleNotNull(articulo.getTpcIGIC()));
			jsonObj.put(COLUMNA_CODMODELOTYC, stringNotNull(articulo.getCodModeloTyC()));
			jsonObj.put(COLUMNA_DESMODELOTYC, stringNotNull(articulo.getDesModeloTyC()));
			jsonObj.put(COLUMNA_STODISPONIBLE, doubleNotNull(articulo.getStoDisponible()));
			jsonObj.put(COLUMNA_STOPTERECIBIR, doubleNotNull(articulo.getStoPteRecibir()));
			jsonObj.put(COLUMNA_DATFECHAENTRADAPREVISTA, stringNotNull(articulo.getDatFechaEntradaPrevista()));
			jsonObj.put(COLUMNA_ORDARTICULO, integerNotNull(articulo.getOrdArticulo()));
			jsonObj.put(COLUMNA_PREARTICULOGEN, doubleNotNull(articulo.getPreArticuloGen()));
			jsonObj.put(COLUMNA_DATNIVEL1, stringNotNull(articulo.getDatNivel1()));
			jsonObj.put(COLUMNA_DATNIVEL2, stringNotNull(articulo.getDatNivel2()));
			jsonObj.put(COLUMNA_CODEMPSUMINISTRADORA, integerNotNull(articulo.getCodEmpSuministradora()));
			jsonObj.put(COLUMNA_FLANOAPLICARDTOPP, stringNotNull(articulo.getFlaNoAplicarDtoPP()));
			jsonObj.put(COLUMNA_DATMARCAS, stringNotNull(articulo.getDatMarcas()));
			jsonObj.put(COLUMNA_PREPUNTOS, doubleNotNull(articulo.getPrePuntos()));
			jsonObj.put(COLUMNA_FLAMUESTRA, stringNotNull(articulo.getFlaMuestra()));

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
			registrarLog("iArticulos", putMethod.getStatusCode(), putMethod.getStatusText(), requestEntity.getContent(), url, "PUT");
			System.out.println("Codigo Status " + putMethod.getStatusCode());
		}
		
		return ret;
	}

}
