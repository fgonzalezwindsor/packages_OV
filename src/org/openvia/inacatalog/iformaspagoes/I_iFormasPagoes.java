package org.openvia.inacatalog.iformaspagoes;

public interface I_iFormasPagoes {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codFormaPago
	 * string */
	public static final String COLUMNA_CODFORMAPAGO = "codFormaPago";
	/** desTarifa
	 * string */
	public static final String COLUMNA_DESFORMAPAGO = "desFormaPago";
	
	/**
	 * GET api/iFormasPagoes?empresa={empresa}&codformapago={codformapago}
	 * @param empresa
	 * @param codformapago
	 * @return ITarifasModel
	 */
	public IFormasPagoesModel apiGetFormaPago(Integer empresa, String codFormaPago);
	
	/**
	 * POST api/iFormasPagoes
	 * @param formaPago
	 * @return boolean
	 */
	public boolean apiPostTarifa(IFormasPagoesModel formaPago);

}
