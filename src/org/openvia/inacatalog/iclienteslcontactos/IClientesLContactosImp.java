package org.openvia.inacatalog.iclienteslcontactos;

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

public class IClientesLContactosImp extends Common implements I_iClientesLContactos {

	@Override
	public IClientesLContactosModel apiGetClienteLContacto(Integer empresa, String codCliente, Integer linContactCli) {
		JSONArray jsonArray = null;
		IClientesLContactosModel clienteLContacto = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iClientesLContactos?empresa=" + empresa + "&codcliente=" + codCliente + "&lincontactcli=" + linContactCli);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	clienteLContacto = new IClientesLContactosModel();
		    	clienteLContacto.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	clienteLContacto.setCodCliente(jsonObject.get(COLUMNA_CODCLIENTE).toString());
		    	clienteLContacto.setLinContactCli(Integer.parseInt(jsonObject.get(COLUMNA_LINCONTACTCLI).toString()));
		    	clienteLContacto.setNomContactCli(jsonObject.get(COLUMNA_NOMCONTACTCLI).toString());
		    	clienteLContacto.setDatPuestoContactCli(jsonObject.get(COLUMNA_DATPUESTOCONTACTCLI).toString());
		    	clienteLContacto.setDatTelefonoContactCli(jsonObject.get(COLUMNA_DATTELEFONOCONTACTCLI).toString());
		    	clienteLContacto.setDatEmailContactCli(jsonObject.get(COLUMNA_DATEMAILCONTACTCLI).toString());
		    	clienteLContacto.setCustom1ContactCli(jsonObject.get(COLUMNA_CUSTOM1CONTACTCLI).toString());
		    	clienteLContacto.setCustom2ContactCli(jsonObject.get(COLUMNA_CUSTOM2CONTACTCLI).toString());
		    	clienteLContacto.setCustom3ContactCli(jsonObject.get(COLUMNA_CUSTOM3CONTACTCLI).toString());
		    	clienteLContacto.setFlaNvoContactCli(Integer.parseInt(jsonObject.get(COLUMNA_FLANVOCONTACTCLI).toString()));
		    }
		}
		return clienteLContacto;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostClienteLContacto(IClientesLContactosModel clienteLContacto) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iClientesLContactos";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(clienteLContacto.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODCLIENTE, stringNotNull(clienteLContacto.getCodCliente()));
			jsonObj.put(COLUMNA_LINCONTACTCLI, integerNotNull(clienteLContacto.getLinContactCli()));
			jsonObj.put(COLUMNA_NOMCONTACTCLI, stringNotNull(clienteLContacto.getNomContactCli()));
			jsonObj.put(COLUMNA_DATPUESTOCONTACTCLI, stringNotNull(clienteLContacto.getDatPuestoContactCli()));
			jsonObj.put(COLUMNA_DATTELEFONOCONTACTCLI, stringNotNull(clienteLContacto.getDatTelefonoContactCli()));
			jsonObj.put(COLUMNA_DATEMAILCONTACTCLI, stringNotNull(clienteLContacto.getDatEmailContactCli()));
			jsonObj.put(COLUMNA_CUSTOM1CONTACTCLI, stringNotNull(clienteLContacto.getCustom1ContactCli()));
			jsonObj.put(COLUMNA_CUSTOM2CONTACTCLI, stringNotNull(clienteLContacto.getCustom2ContactCli()));
			jsonObj.put(COLUMNA_CUSTOM3CONTACTCLI, stringNotNull(clienteLContacto.getCustom3ContactCli()));
			jsonObj.put(COLUMNA_FLANVOCONTACTCLI, integerNotNull(clienteLContacto.getFlaNvoContactCli()));
			
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
			registrarLog("iClientesLContactos", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
