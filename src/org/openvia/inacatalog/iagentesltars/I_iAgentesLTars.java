package org.openvia.inacatalog.iagentesltars;

public interface I_iAgentesLTars {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codAgente
	 * string */
	public static final String COLUMNA_CODAGENTE = "codAgente";
	/** codTarifa
	 * string */
	public static final String COLUMNA_CODTARIFA = "codTarifa";
	
	/**
	 * GET api/iAgentesLTars?empresa={empresa}&codagente={codagente}&codtarifa={codtarifa}
	 * @param empresa
	 * @param codAgente
	 * @param codTarifa
	 * @return IAgentesLTarsModel
	 */
	public IAgentesLTarsModel apiGetAgenteLTar(Integer empresa, String codAgente, String codTarifa);
	
	/**
	 * POST api/iAgentesLTars
	 * @param agenteLTar
	 * @return boolean
	 */
	public boolean apiPostAgenteLTar(IAgentesLTarsModel agenteLTar);
	
}
