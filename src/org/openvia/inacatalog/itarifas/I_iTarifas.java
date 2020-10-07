package org.openvia.inacatalog.itarifas;

public interface I_iTarifas {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codTarifa
	 * string */
	public static final String COLUMNA_CODTARIFA = "codTarifa";
	/** desTarifa
	 * string */
	public static final String COLUMNA_DESTARIFA = "desTarifa";
	/** codIncoterm
	 * string */
	public static final String COLUMNA_CODINCOTERM = "codIncoterm";
	/** flaIVAIncluido
	 * byte */
	public static final String COLUMNA_FLAIVAINCLUIDO = "flaIVAIncluido";
	/** codMoneda
	 * string */
	public static final String COLUMNA_CODMONEDA = "codMoneda";
	
	/**
	 * GET api/iTarifas?empresa={empresa}&codtarifa={codtarifa}
	 * @param empresa
	 * @param codtarifa
	 * @return ITarifasModel
	 */
	public ITarifasModel apiGetTarifa(Integer empresa, String codTarifa);
	
	/**
	 * POST api/iTarifas
	 * @param tarifa
	 * @return boolean
	 */
	public boolean apiPostTarifa(ITarifasModel tarifa);

}
