package org.openvia.inacatalog.iagentes;

public interface I_iAgentes {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codAgente
	 * string */
	public static final String COLUMNA_CODAGENTE = "codAgente";
	/** nomAgente
	 * string */
	public static final String COLUMNA_NOMAGENTE = "nomAgente";
	
	/**
	 * GET api/iAgentes?empresa={empresa}&codagente={codagente}
	 * @param empresa
	 * @param codAgente
	 * @return IAgentesModel
	 */
	public IAgentesModel apiGetAgente(Integer empresa, String codAgente);
	
	/**
	 * POST api/iAgentes
	 * @param familia
	 * @return boolean
	 */
	public boolean apiPostAgente(IAgentesModel familia);
	
}
