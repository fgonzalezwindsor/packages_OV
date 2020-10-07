package org.openvia.inacatalog.itiposclientes;

public interface I_iTiposClientes {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codTipoCliente
	 * string */
	public static final String COLUMNA_CODTIPOCLIENTE = "codTipoCliente";
	/** desTipoCliente
	 * string */
	public static final String COLUMNA_DESTIPOCLIENTE = "desTipoCliente";
	
	/**
	 * GET api/iTiposClientes?empresa={empresa}&codtipocliente={codtipocliente}
	 * @param empresa
	 * @param codtipocliente
	 * @return ITiposClientesModel
	 */
	public ITiposClientesModel apiGetTipoCliente(Integer empresa, String codTipoCliente);
	
	/**
	 * POST api/iTiposClientes
	 * @param tipoCliente
	 * @return boolean
	 */
	public boolean apiPostTipoCliente(ITiposClientesModel tipoCliente);

}
