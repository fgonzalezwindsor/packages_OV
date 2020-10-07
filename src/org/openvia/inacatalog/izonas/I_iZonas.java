package org.openvia.inacatalog.izonas;

public interface I_iZonas {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codZona
	 * string */
	public static final String COLUMNA_CODZONA = "codZona";
	/** desZona
	 * string */
	public static final String COLUMNA_DESZONA = "desZona";
	
	/**
	 * GET GET api/iZonas?empresa={empresa}&codzona={codzona}
	 * @param empresa
	 * @param codzona
	 * @return IZonasModel
	 */
	public IZonasModel apiGetZona(Integer empresa, String codZona);
	
	/**
	 * POST api/iZonas
	 * @param zona
	 * @return boolean
	 */
	public boolean apiPostZona(IZonasModel zona);

}
