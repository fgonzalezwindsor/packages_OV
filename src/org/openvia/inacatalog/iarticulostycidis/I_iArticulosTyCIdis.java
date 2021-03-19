package org.openvia.inacatalog.iarticulostycidis;

public interface I_iArticulosTyCIdis {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codArticulo 
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
	/** codModeloTyC
	 * string */
	public static final String COLUMNA_CODMODELOTYC = "codModeloTyC";
	/** codIdiomaDestino
	 * string */
	public static final String COLUMNA_CODIDIOMADESTINO = "codIdiomaDestino";
	/** codColor
	 * string */
	public static final String COLUMNA_CODCOLOR = "codColor";
	/** desColor
	 * string */
	public static final String COLUMNA_DESCOLOR = "desColor";
	/** codTalla
	 * string */
	public static final String COLUMNA_CODTALLA = "codTalla";
	/** desTalla
	 * string */
	public static final String COLUMNA_DESTALLA = "desTalla";
	/** desArticulo
	 * string */
	public static final String COLUMNA_DESARTICULO = "desArticulo";
	/** datMedidas
	 * string */
	public static final String COLUMNA_DATMEDIDAS = "datMedidas";
	/** datPeso
	 * string */
	public static final String COLUMNA_DATPESO = "datPeso";
	/** datVolumen
	 * string */
	public static final String COLUMNA_DATVOLUMEN = "datVolumen";
	/** datFechaEntradaPrevista
	 * string */
	public static final String COLUMNA_DATFECHAENTRADAPREVISTA = "datFechaEntradaPrevista";
	
	/**
	 * GET api/iArticulosTyCIdis?empresa={empresa}&codmodelotyc={codmodelotyc}&codcolor={codcolor}&codtalla={codtalla}&codidiomadestino={codidiomadestino}
	 * @param empresa
	 * @param codModeloTyC
	 * @param codcolor
	 * @param codtalla
	 * @param codidiomadestino
	 * @return iArticulosTyCIdis
	 */
	public IArticulosTyCIdisModel apiGetArticuloTyCIdi(Integer empresa, String codModeloTyC, String codColor, String codTalla, String codidiomadestino);
	
	/**
	 * POST api/iArticulosTyCIdis
	 * @param articulosTyCIdi
	 * @return boolean
	 */
	public boolean apiPostArticuloTyCIdi(IArticulosTyCIdisModel articuloTyCIdi);
	
	/**
	 * PUT api/iArticulosTyCIdis?empresa={empresa}&codmodelotyc={codmodelotyc}&codcolor={codcolor}&codtalla={codtalla}&codidiomadestino={codidiomadestino}
	 * @param articuloTyCIdi
	 * @return boolean
	 */
	public boolean apiPutArticuloTyCIdi(IArticulosTyCIdisModel articuloTyCIdi);
	
}
