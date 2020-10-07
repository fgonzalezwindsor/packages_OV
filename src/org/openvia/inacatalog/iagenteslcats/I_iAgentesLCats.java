package org.openvia.inacatalog.iagenteslcats;

public interface I_iAgentesLCats {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codAgente
	 * string */
	public static final String COLUMNA_CODAGENTE = "codAgente";
	/** codCatalogo
	 * string */
	public static final String COLUMNA_CODCATALOGO = "codCatalogo";
	
	/**
	 * GET api/iAgentesLCats?empresa={empresa}&codagente={codagente}&codcatalogo={codcatalogo}
	 * @param empresa
	 * @param codAgente
	 * @param codCatalogo
	 * @return IAgentesLCatsModel
	 */
	public IAgentesLCatsModel apiGetAgenteLCat(Integer empresa, String codAgente, String codCatalogo);
	
	/**
	 * POST api/iAgentesLCats
	 * @param agenteLCat
	 * @return boolean
	 */
	public boolean apiPostAgenteLCat(IAgentesLCatsModel agenteLCat);
	
}
