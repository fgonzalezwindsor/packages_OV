package org.openvia.inacatalog.icobros;

public interface I_iCobros {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codCliente
	 * string */
	public static final String COLUMNA_CODCLIENTE = "codCliente";
	/** codDocumento
	 * string */
	public static final String COLUMNA_CODDOCUMENTO = "codDocumento";
	/** fecDocumento
	 * date */
	public static final String COLUMNA_FECDOCUMENTO = "fecDocumento";
	/** fecVencimiento
	 * date */
	public static final String COLUMNA_FECVENCIMIENTO = "fecVencimiento";
	/** datTipoDocumento
	 * string */
	public static final String COLUMNA_DATTIPODOCUMENTO = "datTipoDocumento";
	/** flaImpagado
	 * byte */
	public static final String COLUMNA_FLAIMPAGADO = "flaImpagado";
	/** impPendiente
	 * decimal number */
	public static final String COLUMNA_IMPPENDIENTE = "impPendiente";
	/** codMoneda
	 * string */
	public static final String COLUMNA_CODMONEDA = "codMoneda";
	
	/**
	 * GET api/iCobros?empresa={empresa}&codcliente={codcliente}&coddocumento={coddocumento}
	 * @param empresa
	 * @param codCliente
	 * @param codDocumento
	 * @return ICobrosModel
	 */
	public ICobrosModel apiGetCobro(Integer empresa, String codCliente, String codDocumento);
	
	/**
	 * POST api/iCobros
	 * @param cobro
	 * @return boolean
	 */
	public boolean apiPostCobro(ICobrosModel cobro);
	
}
