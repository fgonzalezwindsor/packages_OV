package org.openvia.inacatalog.isectores;

public interface I_iSectores {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codSector
	 * string */
	public static final String COLUMNA_CODSECTOR = "codSector";
	/** desSector
	 * string */
	public static final String COLUMNA_DESSECTOR = "desSector";
	
	/**
	 * GET api/iSectores?empresa={empresa}&codsector={codsector}
	 * @param empresa
	 * @param codSector
	 * @return ISectoresModel
	 */
	public ISectoresModel apiGetSector(Integer empresa, String codSector);
	
	/**
	 * POST api/iSectores
	 * @param sector
	 * @return boolean
	 */
	public boolean apiPostSector(ISectoresModel sector);

}
