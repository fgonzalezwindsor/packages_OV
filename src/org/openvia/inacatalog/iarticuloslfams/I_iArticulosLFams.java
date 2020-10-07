package org.openvia.inacatalog.iarticuloslfams;

public interface I_iArticulosLFams {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codArticulo
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
	/** codCatalogo
	 * string */
	public static final String COLUMNA_CODCATALOGO = "codCatalogo";
	/** codFamilia
	 * integer */
	public static final String COLUMNA_CODFAMILIA = "codFamilia";
	/** codSubFamilia
	 * integer */
	public static final String COLUMNA_CODSUBFAMILIA = "codSubFamilia";
	/** ordArticulo
	 * integer */
	public static final String COLUMNA_ORDARTICULO = "ordArticulo";
	
	/**
	 * GET api/iArticulosLFams?empresa={empresa}&codarticulo={codarticulo}&codcatalogo={codcatalogo}&codfamilia={codfamilia}&codsubfamilia={codsubfamilia}
	 * @param empresa
	 * @param codArticulo
	 * @param codCatalogo
	 * @param codFamilia
	 * @param codSubFamilia
	 * @return IArticulosLFamsModel
	 */
	public IArticulosLFamsModel apiGetArticuloLFam(Integer empresa, String codArticulo, String codCatalogo, Integer codFamilia, Integer codSubFamilia);
	
	/**
	 * POST api/iArticulosLFams
	 * @param articuloLFam
	 * @return boolean
	 */
	public boolean apiPostArticuloLFam(IArticulosLFamsModel articuloLFam);
	
	/**
	 * PUT api/iArticulosLFams?empresa={empresa}&codarticulo={codarticulo}&codcatalogo={codcatalogo}&codfamilia={codfamilia}&codsubfamilia={codsubfamilia}
	 * @param articuloLFam
	 * @return boolean
	 */
	public boolean apiPutArticuloLFam(IArticulosLFamsModel articuloLFam);
	
}
