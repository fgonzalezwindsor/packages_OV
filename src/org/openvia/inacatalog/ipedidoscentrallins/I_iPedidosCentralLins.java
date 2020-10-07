package org.openvia.inacatalog.ipedidoscentrallins;

public interface I_iPedidosCentralLins {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codPedido
	 * string */
	public static final String COLUMNA_CODPEDIDO = "codPedido";
	/** linPedido
	 * integer */
	public static final String COLUMNA_LINPEDIDO = "linPedido";
	/** codArticulo
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
	/** desLinPed
	 * string */
	public static final String COLUMNA_DESLINPED = "desLinPed";
	/** codMagnitud
	 * string */
	public static final String COLUMNA_CODMAGNITUD = "codMagnitud";
	/** canLinPed
	 * decimal number */
	public static final String COLUMNA_CANLINPED = "canLinPed";
	/** canIndicada
	 * decimal number */
	public static final String COLUMNA_CANINDICADA = "canIndicada";
	/** tpcDto01
	 * decimal number */
	public static final String COLUMNA_TPCDTO01 = "tpcDto01";
	/** tpcDto02
	 * decimal number */
	public static final String COLUMNA_TPCDTO02 = "tpcDto02";
	/** preLinPed
	 * decimal number */
	public static final String COLUMNA_PRELINPED = "preLinPed";
	/** impBaseImponibleLinPed
	 * decimal number */
	public static final String COLUMNA_IMPBASEIMPONIBLELINPED = "impBaseImponibleLinPed";
	/** codCatalogo
	 * string */
	public static final String COLUMNA_CODCATALOGO = "codCatalogo";
	/** codFamilia
	 * integer */
	public static final String COLUMNA_CODFAMILIA = "codFamilia";
	/** codSubFamilia
	 * integer */
	public static final String COLUMNA_CODSUBFAMILIA = "codSubFamilia";
	/** canLinPedPte
	 * decimal number */
	public static final String COLUMNA_CANLINPEDPTE = "canLinPedPte";
	
	/**
	 * GET api/iPedidosCentralLins?empresa={empresa}&codpedido={codpedido}&linpedido={linpedido}
	 * @param empresa
	 * @param codPedido
	 * @param linPedido
	 * @return IPedidosCentralLinsModel
	 */
	public IPedidosCentralLinsModel apiGetPedidoCentralLin(Integer empresa, String codPedido, Integer linPedido);
	
	/**
	 * POST api/iPedidosCentralLins
	 * @param pedidoLin
	 * @return boolean
	 */
	public boolean apiPostPedidoCentralLin(IPedidosCentralLinsModel pedidoLin);
	
}
