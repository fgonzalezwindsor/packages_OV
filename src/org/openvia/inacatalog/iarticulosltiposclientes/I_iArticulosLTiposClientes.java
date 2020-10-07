package org.openvia.inacatalog.iarticulosltiposclientes;

public interface I_iArticulosLTiposClientes {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codArticulo
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
	/** codTipoCliente
	 * string */
	public static final String COLUMNA_CODTIPOCLIENTE = "codTipoCliente";
	
	/**
	 * GET api/iArticulosLTiposClientes?empresa={empresa}&codarticulo={codarticulo}&codtipocliente={codtipocliente}
	 * @param empresa
	 * @param codArticulo
	 * @param codTipoCliente
	 * @return IArticulosLTiposClientesModel
	 */
	public IArticulosLTiposClientesModel apiGetArticuloLTipoCliente(Integer empresa, String codArticulo, String codTipoCliente);
	
	/**
	 * POST api/iArticulosLTiposClientes
	 * @param articuloLFam
	 * @return boolean
	 */
	public boolean apiPostArticuloLTipoCliente(IArticulosLTiposClientesModel articuloLFam);
	
}
