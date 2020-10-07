package org.openvia.inacatalog.iclientesldirs;

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

public class IClientesLDirsImp extends Common implements I_iClientesLDirs {

	@Override
	public IClientesLDirsModel apiGetClienteLDir(Integer empresa, String codCliente, Integer linDirCli) {
		JSONArray jsonArray = null;
		IClientesLDirsModel clienteLDir = null;
		try {
			jsonArray = readJsonArrayFromUrl("http://190.215.113.91/InaCatalogAPI/api/iClientesLDirs?empresa=" + empresa + "&codcliente=" + codCliente + "&lindircli=" + linDirCli);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (jsonArray != null && jsonArray.size()>0 && !jsonArray.get(0).toString().equals("[]")) {
			for (int i=0; i<jsonArray.size(); i++) {
		    	JSONObject jsonObject = (JSONObject) jsonArray.get(i);
		    	clienteLDir = new IClientesLDirsModel();
		    	clienteLDir.setCodEmpresa(Integer.parseInt(jsonObject.get(COLUMNA_CODEMPRESA).toString()));
		    	clienteLDir.setCodCliente(jsonObject.get(COLUMNA_CODCLIENTE).toString());
		    	clienteLDir.setLinDirCli(Integer.parseInt(jsonObject.get(COLUMNA_LINDIRCLI).toString()));
		    	clienteLDir.setNomDirCli(jsonObject.get(COLUMNA_NOMDIRCLI).toString());
		    	clienteLDir.setRsoDirCli(jsonObject.get(COLUMNA_RSODIRCLI).toString());
		    	clienteLDir.setDatCalleDirCli(jsonObject.get(COLUMNA_DATCALLEDIRCLI).toString());
		    	clienteLDir.setCodPostalDirCli(jsonObject.get(COLUMNA_CODPOSTALDIRCLI).toString());
		    	clienteLDir.setDatPoblacionDirCli(jsonObject.get(COLUMNA_DATPOBLACIONDIRCLI).toString());
		    	clienteLDir.setDatProvinciaDirCli(jsonObject.get(COLUMNA_DATPROVINCIADIRCLI).toString());
		    	clienteLDir.setDatPaisDirCli(jsonObject.get(COLUMNA_DATPAISDIRCLI).toString());
		    	clienteLDir.setDatContactoDirCli(jsonObject.get(COLUMNA_DATCONTACTODIRCLI).toString());
		    	clienteLDir.setDatTelefonoDirCli(jsonObject.get(COLUMNA_DATTELEFONODIRCLI).toString());
		    	clienteLDir.setDatFaxDirCli(jsonObject.get(COLUMNA_DATFAXDIRCLI).toString());
		    	clienteLDir.setDatEmailDirCli(jsonObject.get(COLUMNA_DATEMAILDIRCLI).toString());
		    	clienteLDir.setHipWebDirCli(jsonObject.get(COLUMNA_HIPWEBDIRCLI).toString());
		    	clienteLDir.setCodSuDirCli(jsonObject.get(COLUMNA_CODSUDIRCLI).toString());
		    	clienteLDir.setValLatitud(Double.valueOf(jsonObject.get(COLUMNA_VALLATITUD).toString()));
		    	clienteLDir.setValLongitud(Double.valueOf(jsonObject.get(COLUMNA_VALLONGITUD).toString()));
		    	clienteLDir.setDatTelMovilDirCli(jsonObject.get(COLUMNA_DATTELMOVILDIRCLI).toString());
		    	clienteLDir.setCodAgente(jsonObject.get(COLUMNA_CODAGENTE).toString());
		    	clienteLDir.setFlaNvoDirCli(Integer.parseInt(jsonObject.get(COLUMNA_FLANVODIRCLI).toString()));
		    }
		}
		return clienteLDir;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean apiPostClienteLDir(IClientesLDirsModel clienteLDirs) {
		boolean ret = false;
		HttpClient httpClient = new HttpClient();
		String url = "http://190.215.113.91/InaCatalogAPI/api/iClientesLDirs";
		PostMethod postMethod = new PostMethod(url);
		StringRequestEntity requestEntity = null;
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put(COLUMNA_CODEMPRESA, integerNotNull(clienteLDirs.getCodEmpresa()));
			jsonObj.put(COLUMNA_CODCLIENTE, stringNotNull(clienteLDirs.getCodCliente()));
			jsonObj.put(COLUMNA_LINDIRCLI, integerNotNull(clienteLDirs.getLinDirCli()));
			jsonObj.put(COLUMNA_NOMDIRCLI, stringNotNull(clienteLDirs.getNomDirCli()));
			jsonObj.put(COLUMNA_RSODIRCLI, stringNotNull(clienteLDirs.getRsoDirCli()));
			jsonObj.put(COLUMNA_DATCALLEDIRCLI, stringNotNull(clienteLDirs.getDatCalleDirCli()));
			jsonObj.put(COLUMNA_CODPOSTALDIRCLI, stringNotNull(clienteLDirs.getCodPostalDirCli()));
			jsonObj.put(COLUMNA_DATPOBLACIONDIRCLI, stringNotNull(clienteLDirs.getDatPoblacionDirCli()));
			jsonObj.put(COLUMNA_DATPROVINCIADIRCLI, stringNotNull(clienteLDirs.getDatProvinciaDirCli()));
			jsonObj.put(COLUMNA_DATPAISDIRCLI, stringNotNull(clienteLDirs.getDatPaisDirCli()));
			jsonObj.put(COLUMNA_DATCONTACTODIRCLI, stringNotNull(clienteLDirs.getDatContactoDirCli()));
			jsonObj.put(COLUMNA_DATTELEFONODIRCLI, stringNotNull(clienteLDirs.getDatTelefonoDirCli()));
			jsonObj.put(COLUMNA_DATFAXDIRCLI, stringNotNull(clienteLDirs.getDatFaxDirCli()));
			jsonObj.put(COLUMNA_DATEMAILDIRCLI, stringNotNull(clienteLDirs.getDatEmailDirCli()));
			jsonObj.put(COLUMNA_HIPWEBDIRCLI, stringNotNull(clienteLDirs.getHipWebDirCli()));
			jsonObj.put(COLUMNA_CODSUDIRCLI, stringNotNull(clienteLDirs.getCodSuDirCli()));
			jsonObj.put(COLUMNA_VALLATITUD, doubleNotNull(clienteLDirs.getValLatitud()));
			jsonObj.put(COLUMNA_VALLONGITUD, doubleNotNull(clienteLDirs.getValLongitud()));
			jsonObj.put(COLUMNA_DATTELMOVILDIRCLI, stringNotNull(clienteLDirs.getDatTelMovilDirCli()));
			jsonObj.put(COLUMNA_CODAGENTE, stringNotNull(clienteLDirs.getCodAgente()));
			jsonObj.put(COLUMNA_FLANVODIRCLI, integerNotNull(clienteLDirs.getFlaNvoDirCli()));
			
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
			registrarLog("iClientesLDirs", postMethod.getStatusCode(), postMethod.getStatusText(), requestEntity.getContent(), url, "POST");
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
