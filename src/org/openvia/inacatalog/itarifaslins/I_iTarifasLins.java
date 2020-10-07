package org.openvia.inacatalog.itarifaslins;

public interface I_iTarifasLins {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codTarifa
	 * string */
	public static final String COLUMNA_CODTARIFA = "codTarifa";
	/** codMagnitud
	 * string */
	public static final String COLUMNA_CODMAGNITUD = "codMagnitud";
	/** codArticulo
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
	/** canMinima
	 * decimal number */
	public static final String COLUMNA_CANMINIMA = "canMinima";
	/** preArticulo
	 * decimal number */
	public static final String COLUMNA_PREARTICULO = "preArticulo";
	/** flaPreMagnitud
	 * byte */
	public static final String COLUMNA_FLAPREMAGNITUD = "flaPreMagnitud";
	/** tpcDto01Def
	 * decimal number */
	public static final String COLUMNA_TPCDTO01DEF = "tpcDto01Def";
	/** tpcDto02Def
	 * decimal number */
	public static final String COLUMNA_TPCDTO02DEF = "tpcDto02Def";
	/** tpcDto01Max
	 * decimal number */
	public static final String COLUMNA_TPCDTO01MAX = "tpcDto01Max";
	/** tpcDto02Max
	 * decimal number */
	public static final String COLUMNA_TPCDTO02MAX = "tpcDto02Max";
	/** PuntosSinDto
	 * decimal number */
	public static final String COLUMNA_PUNTOSSINDTO = "PuntosSinDto";
	/** PuntosConDto
	 * decimal number */
	public static final String COLUMNA_PUNTOSCONDTO = "PuntosConDto";
	/** flaPuntosUnitarios
	 * byte */
	public static final String COLUMNA_FLAPUNTOSUNITARIOS = "flaPuntosUnitarios";
	
	/**
	 * GET api/iTarifasLins?empresa={empresa}&codtarifa={codtarifa}&codmagnitud={codmagnitud}&codarticulo={codarticulo}&canminima={canminima}
	 * @param empresa
	 * @param codTarifa
	 * @param codMagnitud
	 * @param codArticulo
	 * @param canMinima
	 * @return ITarifasLinsModel
	 */
	public ITarifasLinsModel apiGetTarifaLin(Integer empresa, String codTarifa, String codMagnitud, String codArticulo, Double canMinima);
	
	/**
	 * POST api/iTarifasLins
	 * @param tarifaLin
	 * @return boolean
	 */
	public boolean apiPostTarifaLin(ITarifasLinsModel tarifaLin);
	
	/**
	 * PUT api/iTarifasLins?empresa={empresa}&codtarifa={codtarifa}&codmagnitud={codmagnitud}&codarticulo={codarticulo}&canminima={canminima}
	 * @param empresa
	 * @param codTarifa
	 * @param codMagnitud
	 * @param codArticulo
	 * @param canMinima
	 * @return boolean
	 */
	public boolean apiPutTarifaLin(ITarifasLinsModel tarifaLin);

}
