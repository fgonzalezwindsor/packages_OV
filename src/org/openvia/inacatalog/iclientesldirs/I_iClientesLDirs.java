package org.openvia.inacatalog.iclientesldirs;

public interface I_iClientesLDirs {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codCliente
	 * string */
	public static final String COLUMNA_CODCLIENTE = "codCliente";
	/** linDirCli
	 * integer */
	public static final String COLUMNA_LINDIRCLI = "linDirCli";
	/** nomDirCli
	 * string */
	public static final String COLUMNA_NOMDIRCLI = "nomDirCli";
	/** rsoDirCli
	 * string */
	public static final String COLUMNA_RSODIRCLI = "rsoDirCli";
	/** datCalleDirCli
	 * string */
	public static final String COLUMNA_DATCALLEDIRCLI = "datCalleDirCli";
	/** codPostalDirCli
	 * string */
	public static final String COLUMNA_CODPOSTALDIRCLI = "codPostalDirCli";
	/** datPoblacionDirCli
	 * string */
	public static final String COLUMNA_DATPOBLACIONDIRCLI = "datPoblacionDirCli";
	/** datProvinciaDirCli
	 * string */
	public static final String COLUMNA_DATPROVINCIADIRCLI = "datProvinciaDirCli";
	/** datPaisDirCli
	 * string */
	public static final String COLUMNA_DATPAISDIRCLI = "datPaisDirCli";
	/** datContactoDirCli
	 * string */
	public static final String COLUMNA_DATCONTACTODIRCLI = "datContactoDirCli";
	/** datTelefonoDirCli
	 * string */
	public static final String COLUMNA_DATTELEFONODIRCLI = "datTelefonoDirCli";
	/** datFaxDirCli
	 * string */
	public static final String COLUMNA_DATFAXDIRCLI = "datFaxDirCli";
	/** datEmailDirCli
	 * string */
	public static final String COLUMNA_DATEMAILDIRCLI = "datEmailDirCli";
	/** hipWebDirCli
	 * string */
	public static final String COLUMNA_HIPWEBDIRCLI = "hipWebDirCli";
	/** codSuDirCli
	 * string */
	public static final String COLUMNA_CODSUDIRCLI = "codSuDirCli";
	/** valLatitud
	 * decimal number */
	public static final String COLUMNA_VALLATITUD = "valLatitud";
	/** valLongitud
	 * decimal number */
	public static final String COLUMNA_VALLONGITUD = "valLongitud";
	/** datTelMovilDirCli
	 * string */
	public static final String COLUMNA_DATTELMOVILDIRCLI = "datTelMovilDirCli";
	/** codAgente
	 * string */
	public static final String COLUMNA_CODAGENTE = "codAgente";
	/** flaNvoDirCli
	 * integer */
	public static final String COLUMNA_FLANVODIRCLI = "flaNvoDirCli";
	
	/**
	 * GET api/iClientesLDirs?empresa={empresa}&codcliente={codcliente}&lindircli={lindircli}
	 * @param empresa
	 * @param codCliente
	 * @param linDirCli
	 * @return IClientesLDirsModel
	 */
	public IClientesLDirsModel apiGetClienteLDir(Integer empresa, String codCliente, Integer linDirCli);
	
	/**
	 * POST api/iClientesLDirs
	 * @param clienteLDirs
	 * @return boolean
	 */
	public boolean apiPostClienteLDir(IClientesLDirsModel clienteLDirs);
	
}
