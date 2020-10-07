package org.openvia.inacatalog.iempresas;

public interface I_iEmpresas {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** nomEmpresa
	 * string */
	public static final String COLUMNA_NOMEMPRESA = "nomEmpresa";
	/** rsoEmpresa
	 * string */
	public static final String COLUMNA_RSOEMPRESA = "rsoEmpresa";
	/** cifEmpresa
	 * string */
	public static final String COLUMNA_CIFEMPRESA = "cifEmpresa";
	/** datCalleEmpresa
	 * string */
	public static final String COLUMNA_DATCALLEEMPRESA = "datCalleEmpresa";
	/** codPostalEmpresa
	 * string */
	public static final String COLUMNA_CODPOSTALEMPRESA = "codPostalEmpresa";
	/** datPoblacionEmpresa
	 * string */
	public static final String COLUMNA_DATPOBLACIONEMPRESA = "datPoblacionEmpresa";
	/** datProvinciaEmpresa
	 * string */
	public static final String COLUMNA_DATPROVINCIAEMPRESA = "datProvinciaEmpresa";
	/** datPaisEmpresa
	 * string */
	public static final String COLUMNA_DATPAISEMPRESA = "datPaisEmpresa";
	/** datTelefonoEmpresa
	 * string */
	public static final String COLUMNA_DATTELEFONOEMPRESA = "datTelefonoEmpresa";
	/** datFaxEmpresa
	 * string */
	public static final String COLUMNA_DATFAXEMPRESA = "datFaxEmpresa";
	/** datEmailEmpresa
	 * string */
	public static final String COLUMNA_DATEMAILEMPRESA = "datEmailEmpresa";
	/** hipWebEmpresa
	 * string */
	public static final String COLUMNA_HIPWEBEMPRESA = "hipWebEmpresa";
	/** datColetillaPedido
	 * string */
	public static final String COLUMNA_DATCOLETILLAPEDIDO = "datColetillaPedido";
	/** flaEmpSuministradora
	 * byte */
	public static final String COLUMNA_FLAEMPSUMINISTRADORA = "flaEmpSuministradora";
	/** flaImgModificada
	 * byte */
	public static final String COLUMNA_FLAIMGMODIFICADA = "flaImgModificada";
	
	/**
	 * GET api/iEmpresas?empresa={empresa}
	 * @param empresa
	 * @return IEmpresasModel
	 */
	public IEmpresasModel apiGetEmpresa(Integer empresa);
	
	/**
	 * POST api/iEmpresas
	 * @param empresa
	 * @return boolean
	 */
	public boolean apiPostEmpresa(IEmpresasModel empresa);
	
}
