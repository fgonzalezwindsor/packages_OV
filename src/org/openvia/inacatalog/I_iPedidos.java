package org.openvia.inacatalog;

import org.json.simple.JSONArray;

public interface I_iPedidos {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** nomIPad
	 * string */
	public static final String COLUMNA_NOMIPAD = "nomIPad";
	/** codPedido
	 * integer */
	public static final String COLUMNA_CODPEDIDO = "codPedido";
	/** fecPedido 
	 * date */
	public static final String COLUMNA_FECPEDIDO = "fecPedido";
	/** codCliente
	 * string */
	public static final String COLUMNA_CODCLIENTE = "codCliente";
	/** linDirCli
	 * integer */
	public static final String COLUMNA_LINDIRCLI = "linDirCli";
	/** codAgente
	 * string */
	public static final String COLUMNA_CODAGENTE = "codAgente";
	/** codFormaPago
	 * string */
	public static final String COLUMNA_CODFORMAPAGO = "codFormaPago";
	/** tpcDto01
	 * decimal number */
	public static final String COLUMNA_TPCDTO01 = "tpcDto01";
	/** tpcDto02 
	 * decimal number */
	public static final String COLUMNA_TPCDTO02 = "tpcDto02";
	/** tpcDtoPp
	 * decimal number */
	public static final String COLUMNA_TPCDTOPP = "tpcDtoPp";
	/** tpcDto03
	 * decimal number */
	public static final String COLUMNA_TPCDTO03 = "tpcDto03";
	/** codMoneda
	 * string */
	public static final String COLUMNA_CODMONEDA = "codMoneda";
	/** codIncoterm
	 * string */
	public static final String COLUMNA_CODINCOTERM = "codIncoterm";
	/** totBrutoPed
	 * decimal number */
	public static final String COLUMNA_TOTBRUTOPED = "totBrutoPed";
	/** totDto1Ped
	 * decimal number */
	public static final String COLUMNA_TOTDTO1PED = "totDto1Ped";
	/** totDto2Ped
	 * decimal number */
	public static final String COLUMNA_TOTDTO2PED = "totDto2Ped";
	/** totDto3Ped
	 * decimal number */
	public static final String COLUMNA_TOTDTO3PED = "totDto3Ped";
	/** totNetoPed
	 * decimal number */
	public static final String COLUMNA_TOTNETOPED = "totNetoPed";
	/** totDtoPPPed
	 * decimal number */
	public static final String COLUMNA_TOTDTOPPPED = "totDtoPPPed";
	/** totBaseImponiblePed
	 * decimal number */
	public static final String COLUMNA_TOTBASEIMPONIBLEPED = "totBaseImponiblePed";
	/** totIVAPed
	 * decimal number */
	public static final String COLUMNA_TOTIVAPED = "totIVAPed";
	/** totREPed
	 * decimal number */
	public static final String COLUMNA_TOTREPED = "totREPed";
	/** totPed
	 * decimal number */
	public static final String COLUMNA_TOTPED = "totPed";
	/** datFechaEntrega
	 * string */
	public static final String COLUMNA_DATFECHAENTREGA = "datFechaEntrega";
	/** obsPedido
	 * string */
	public static final String COLUMNA_OBSPEDIDO = "obsPedido";
	/** flaExpPedido
	 * byte */
	public static final String COLUMNA_FLAEXPPEDIDO = "flaExpPedido";
	/** datEstadoPedido
	 * string */
	public static final String COLUMNA_DATESTADOPEDIDO = "datEstadoPedido";
	/** flaIVAIncluido
	 * byte */
	public static final String COLUMNA_FLAIVAINCLUIDO = "flaIVAIncluido";
	/** codTipoVenta
	 * string */
	public static final String COLUMNA_CODTIPOVENTA = "codTipoVenta";
	/** codOrigenVenta
	 * string */
	public static final String COLUMNA_CODORIGENVENTA = "codOrigenVenta";
	/** totPuntosPedido
	 * decimal number */
	public static final String COLUMNA_TOTPUNTOSPEDIDO = "totPuntosPedido";
	/** codFactorPuntos
	 * string */
	public static final String COLUMNA_CODFACTORPUNTOS = "codFactorPuntos";
	/** datFactorPuntos
	 * decimal number */
	public static final String COLUMNA_DATFACTORPUNTOS = "datFactorPuntos";
	/** totPuntosTotales
	 * decimal number */
	public static final String COLUMNA_TOTPUNTOSTOTALES = "totPuntosTotales";
	/** totPuntosConsumidos
	 * decimal number */
	public static final String COLUMNA_TOTPUNTOSCONSUMIDOS = "totPuntosConsumidos";
	/** flaRecibidoMS
	 * byte */
	public static final String COLUMNA_FLARECIBIDOMS = "flaRecibidoMS";
	/** Custom1
	 * string */
	public static final String COLUMNA_CUSTOM1 = "Custom1";
	/** Custom2
	 * string */
	public static final String COLUMNA_CUSTOM2 = "Custom2";
	/** Custom3
	 * string */
	public static final String COLUMNA_CUSTOM3 = "Custom3";
	
	
	
	public JSONArray readJsonArrayFromUrl(String url) throws Exception;

}
