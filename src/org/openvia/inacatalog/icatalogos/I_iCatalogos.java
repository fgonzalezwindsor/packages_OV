package org.openvia.inacatalog.icatalogos;

public interface I_iCatalogos {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codCatalogo
	 * string */
	public static final String COLUMNA_CODCATALOGO = "codCatalogo";
	/** desCatalogo
	 * string */
	public static final String COLUMNA_DESCATALOGO = "desCatalogo";
	/** obsCatalogo 
	 * string */
	public static final String COLUMNA_OBSCATALOGO = "obsCatalogo";
	/** nomImagenCat
	 * string */
	public static final String COLUMNA_NOMIMAGENCAT = "nomImagenCat";
	/** nomIconoCat
	 * integer */
	public static final String COLUMNA_NOMICONOCAT = "nomIconoCat";
	/** flaIcoModificado
	 * byte */
	public static final String COLUMNA_FLAICOMODIFICADO = "flaIcoModificado";
	/** flaImgModificado
	 * byte */
	public static final String COLUMNA_FLAIMGMODIFICADO = "flaImgModificado";
	/** ordCatalogo
	 * decimal string */
	public static final String COLUMNA_ORDCATALOGO = "ordCatalogo";
	
	/**
	 * GET api/iCatalogos?empresa={empresa}&codcatalogo={codcatalogo}
	 * @param empresa
	 * @param codCatalogo
	 * @return ICatalogoModel
	 */
	public ICatalogosModel apiGetCatalogo(Integer empresa, String codCatalogo);
	
	/**
	 * POST api/iCatalogos
	 * @param catalogo
	 * @return boolean
	 */
	public boolean apiPostCatalogo(ICatalogosModel catalogo);

}
