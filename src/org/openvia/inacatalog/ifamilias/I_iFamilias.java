package org.openvia.inacatalog.ifamilias;

public interface I_iFamilias {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codCatalogo
	 * string */
	public static final String COLUMNA_CODCATALOGO = "codCatalogo";
	/** codFamilia
	 * string */
	public static final String COLUMNA_CODFAMILIA = "codFamilia";
	/** codSubFamilia 
	 * string */
	public static final String COLUMNA_CODSUBFAMILIA = "codSubFamilia";
	/** desFamilia
	 * string */
	public static final String COLUMNA_DESFAMILIA = "desFamilia";
	/** nomIcoFamilia
	 * integer */
	public static final String COLUMNA_NOMICOFAMILIA = "nomIcoFamilia";
	/** ordFamilia
	 * byte */
	public static final String COLUMNA_ORDFAMILIA = "ordFamilia";
	/** flaIcoModificado
	 * byte */
	public static final String COLUMNA_FLAICOMODIFICADO = "flaIcoModificado";
	/** obsFamilia
	 * decimal string */
	public static final String COLUMNA_OBSFAMILIA = "obsFamilia";
	/** nomImagenFam
	 * decimal string */
	public static final String COLUMNA_NOMIMAGENFAM = "nomImagenFam";
	/** flaImgModificado
	 * decimal string */
	public static final String COLUMNA_FLAIMGMODIFICADO = "flaImgModificado";
	
	/**
	 * GET api/iFamilias?empresa={empresa}&codcatalogo={codcatalogo}&codfamilia={codfamilia}&codsubfamilia={codsubfamilia}
	 * @param empresa
	 * @param codFamilia
	 * @param codsubfamilia
	 * @return IFamiliasModel
	 */
	public IFamiliasModel apiGetFamilia(Integer empresa, String codCatalogo, Integer codFamilia, Integer codSubFamilia);
	
	/**
	 * POST api/iFamilias
	 * @param familia
	 * @return boolean
	 */
	public boolean apiPostFamilia(IFamiliasModel familia);
	
}
