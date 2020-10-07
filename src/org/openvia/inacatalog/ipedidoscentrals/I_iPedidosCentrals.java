package org.openvia.inacatalog.ipedidoscentrals;

public interface I_iPedidosCentrals {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codPedido
	 * string */
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
	/** codFormaPago
	 * string */
	public static final String COLUMNA_CODFORMAPAGO = "codFormaPago";
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
	/** totBaseImponiblePed
	 * decimal number */
	public static final String COLUMNA_TOTBASEIMPONIBLEPED = "totBaseImponiblePed";
	/** totIVAPed
	 * decimal number */
	public static final String COLUMNA_TOTIVAPED = "totIVAPed";
	/** totREPed
	 * decimal number */
	public static final String COLUMNA_TOTREPED = "totREPed";
	/** datFechaEntrega
	 * string */
	public static final String COLUMNA_DATFECHAENTREGA = "datFechaEntrega";
	/** datEstadoPedido
	 * string */
	public static final String COLUMNA_DATESTADOPEDIDO = "datEstadoPedido";
	
	/**
	 * GET api/iPedidosCentrals?empresa={empresa}&codpedido={codpedido}
	 * @param empresa
	 * @param codPedido
	 * @return IPedidosCentralsModel
	 */
	public IPedidosCentralsModel apiGetPedidoCentral(Integer empresa, String codPedido);
	
	/**
	 * POST api/iPedidosCentrals
	 * @param pedido
	 * @return boolean
	 */
	public boolean apiPostPedidoCentral(IPedidosCentralsModel pedido);
	
}
