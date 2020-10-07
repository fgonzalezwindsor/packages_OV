package org.openvia.inacatalog.iclientes;

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

public class IClientesImp extends Common implements I_iClientes {

	@Override
	public IClientesModel apiGetCliente(Integer empresa, String codCliente) {
		JSONArray jsonArray = null;
		IClientesModel cliente = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iClientes?empresa=" + empresa + "&codcliente=" + codCliente);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	cliente = new IClientesModel();
		    	cliente.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	cliente.setCodCliente(jsonObject.get(COLUMNA_CODCLIENTE).toString());
		    	cliente.setNomCliente(jsonObject.get(COLUMNA_NOMCLIENTE).toString());
		    	cliente.setRsoCliente(jsonObject.get(COLUMNA_RSOCLIENTE).toString());
		    	cliente.setCifCliente(jsonObject.get(COLUMNA_CIFCLIENTE).toString());
		    	cliente.setCodZona(jsonObject.get(COLUMNA_CODZONA).toString());
		    	cliente.setCodAgente(jsonObject.get(COLUMNA_CODAGENTE).toString());
		    	cliente.setCodTipoCliente(jsonObject.get(COLUMNA_CODTIPOCLIENTE).toString());
		    	cliente.setTipIVA(jsonObject.get(COLUMNA_TIPIVA).toString());
		    	cliente.setTpcDto01(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO01).toString()));
		    	cliente.setTpcDto02(Double.valueOf(jsonObject.get(COLUMNA_TPCDTO02).toString()));
		    	cliente.setTpcDtoPp(Double.valueOf(jsonObject.get(COLUMNA_TPCDTOPP).toString()));
		    	cliente.setCodFormaPago(jsonObject.get(COLUMNA_CODFORMAPAGO).toString());
		    	cliente.setFlaNvoCliente(jsonObject.get(COLUMNA_FLANVOCLIENTE).toString());
		    	cliente.setFlaExpCliente(jsonObject.get(COLUMNA_FLAEXPCLIENTE).toString());
		    	cliente.setCodTarifa(jsonObject.get(COLUMNA_CODTARIFA).toString());
		    	cliente.setCodGrupoPreciosCliente(jsonObject.get(COLUMNA_CODGRUPOPRECIOSCLIENTE).toString());
		    	cliente.setFlaObsoleto(jsonObject.get(COLUMNA_FLAOBSOLETO).toString());
		    	cliente.setImpPendienteRiesgo(Double.valueOf(jsonObject.get(COLUMNA_IMPPENDIENTERIESGO).toString()));
		    	cliente.setImpVencidoRiesgo(Double.valueOf(jsonObject.get(COLUMNA_IMPVENCIDORIESGO).toString()));
		    	cliente.setImpImpagadoRiesgo(Double.valueOf(jsonObject.get(COLUMNA_IMPIMPAGADORIESGO).toString()));
		    	cliente.setImpCoberturaRiesgo(Double.valueOf(jsonObject.get(COLUMNA_IMPCOBERTURARIESGO).toString()));
		    	cliente.setFlaBloqueaClienteRiesgo(jsonObject.get(COLUMNA_FLABLOQUEACLIENTERIESGO).toString());
		    	cliente.setCodIdioma(jsonObject.get(COLUMNA_CODIDIOMA).toString());
		    	cliente.setDatIBAN(jsonObject.get(COLUMNA_DATIBAN).toString());
		    	cliente.setCodSector(jsonObject.get(COLUMNA_CODSECTOR).toString());
		    	cliente.setDatBlog(jsonObject.get(COLUMNA_DATBLOG).toString());
		    	cliente.setImpFacturacion(Double.valueOf(jsonObject.get(COLUMNA_IMPFACTURACION).toString()));
		    	cliente.setObsClienteNoEdi(jsonObject.get(COLUMNA_OBSCLIENTENOEDI).toString());
		    	cliente.setObsClienteEdi(jsonObject.get(COLUMNA_OBSCLIENTEEDI).toString());
		    	cliente.setCustom1(jsonObject.get(COLUMNA_CUSTOM1).toString());
		    	cliente.setCustom2(jsonObject.get(COLUMNA_CUSTOM2).toString());
		    	cliente.setCustom3(jsonObject.get(COLUMNA_CUSTOM3).toString());
		    	cliente.setCustom4(jsonObject.get(COLUMNA_CUSTOM4).toString());
		    	cliente.setCustom5(jsonObject.get(COLUMNA_CUSTOM5).toString());
		    	cliente.setFecAltaCliente(jsonObject.get(COLUMNA_FECALTACLIENTE).toString());
		    	cliente.setCodMonedaRiesgo(jsonObject.get(COLUMNA_CODMONEDARIESGO).toString());		    	
		    }
		}
		return cliente;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostCliente(IClientesModel cli) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iClientes";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(cli.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODCLIENTE, stringNotNull(cli.getCodCliente()));
			jsonObj.put(COLUMNA_NOMCLIENTE, stringNotNull(cli.getNomCliente()));
			jsonObj.put(COLUMNA_RSOCLIENTE, stringNotNull(cli.getRsoCliente()));
			jsonObj.put(COLUMNA_CIFCLIENTE, stringNotNull(cli.getCifCliente()));
			jsonObj.put(COLUMNA_CODZONA, stringNotNull(cli.getCodZona()));
			jsonObj.put(COLUMNA_CODAGENTE, stringNotNull(cli.getCodAgente()));
			jsonObj.put(COLUMNA_CODTIPOCLIENTE, stringNotNull(cli.getCodTipoCliente()));
			jsonObj.put(COLUMNA_TIPIVA, stringNotNull(cli.getTipIVA()));
			jsonObj.put(COLUMNA_TPCDTO01, doubleNotNull(cli.getTpcDto01()));
			jsonObj.put(COLUMNA_TPCDTO02, doubleNotNull(cli.getTpcDto02()));
			jsonObj.put(COLUMNA_TPCDTOPP, doubleNotNull(cli.getTpcDtoPp()));
			jsonObj.put(COLUMNA_CODFORMAPAGO, stringNotNull(cli.getCodFormaPago()));
			jsonObj.put(COLUMNA_FLANVOCLIENTE, stringNotNull(cli.getFlaNvoCliente()));
			jsonObj.put(COLUMNA_FLAEXPCLIENTE, stringNotNull(cli.getFlaExpCliente()));
			jsonObj.put(COLUMNA_CODTARIFA, stringNotNull(cli.getCodTarifa()));
			jsonObj.put(COLUMNA_CODGRUPOPRECIOSCLIENTE, stringNotNull(cli.getCodGrupoPreciosCliente()));
			jsonObj.put(COLUMNA_FLAOBSOLETO, stringNotNull(cli.getFlaObsoleto()));
			jsonObj.put(COLUMNA_IMPPENDIENTERIESGO, doubleNotNull(cli.getImpPendienteRiesgo()));
			jsonObj.put(COLUMNA_IMPVENCIDORIESGO, doubleNotNull(cli.getImpVencidoRiesgo()));
			jsonObj.put(COLUMNA_IMPIMPAGADORIESGO, doubleNotNull(cli.getImpImpagadoRiesgo()));
			jsonObj.put(COLUMNA_IMPCOBERTURARIESGO, doubleNotNull(cli.getImpCoberturaRiesgo()));
			jsonObj.put(COLUMNA_FLABLOQUEACLIENTERIESGO, stringNotNull(cli.getFlaBloqueaClienteRiesgo()));
			jsonObj.put(COLUMNA_CODIDIOMA, stringNotNull(cli.getCodIdioma()));
			jsonObj.put(COLUMNA_DATIBAN, stringNotNull(cli.getDatIBAN()));
			jsonObj.put(COLUMNA_CODSECTOR, stringNotNull(cli.getCodSector()));
			jsonObj.put(COLUMNA_DATBLOG, stringNotNull(cli.getDatBlog()));
			jsonObj.put(COLUMNA_IMPFACTURACION, doubleNotNull(cli.getImpFacturacion()));
			jsonObj.put(COLUMNA_OBSCLIENTENOEDI, stringNotNull(cli.getObsClienteNoEdi()));
			jsonObj.put(COLUMNA_OBSCLIENTEEDI, stringNotNull(cli.getObsClienteEdi()));
			jsonObj.put(COLUMNA_CUSTOM1, stringNotNull(cli.getCustom1()));
			jsonObj.put(COLUMNA_CUSTOM2, stringNotNull(cli.getCustom2()));
			jsonObj.put(COLUMNA_CUSTOM3, stringNotNull(cli.getCustom3()));
			jsonObj.put(COLUMNA_CUSTOM4, stringNotNull(cli.getCustom4()));
			jsonObj.put(COLUMNA_CUSTOM5, stringNotNull(cli.getCustom5()));
			jsonObj.put(COLUMNA_FECALTACLIENTE, stringNotNull(cli.getFecAltaCliente()));
			jsonObj.put(COLUMNA_CODMONEDARIESGO, stringNotNull(cli.getCodMonedaRiesgo()));
			
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
			registrarLog("iClientes", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
