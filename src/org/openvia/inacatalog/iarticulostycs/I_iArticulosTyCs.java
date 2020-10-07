package org.openvia.inacatalog.iarticulostycs;

public interface I_iArticulosTyCs {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codArticulo 
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
	/** codModeloTyC
	 * string */
	public static final String COLUMNA_CODMODELOTYC = "codModeloTyC";
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
	/** codEAN13
	 * string */
	public static final String COLUMNA_CODEAN13 = "codEAN13";
	/** datMedidas
	 * string */
	public static final String COLUMNA_DATMEDIDAS = "datMedidas";
	/** datPeso
	 * string */
	public static final String COLUMNA_DATPESO = "datPeso";
	/** datVolumen
	 * string */
	public static final String COLUMNA_DATVOLUMEN = "datVolumen";
	/** valMinVenta
	 * decimal number */
	public static final String COLUMNA_VALMINVENTA = "valMinVenta";
	/** valUniXCaja
	 * decimal number */
	public static final String COLUMNA_VALUNIXCAJA = "valUniXCaja";
	/** valUniXPalet
	 * decimal number */
	public static final String COLUMNA_VALUNIXPALET = "valUniXPalet";
	/** valUniIncSencillo
	 * decimal number */
	public static final String COLUMNA_VALUNIINCSENCILLO = "valUniIncSencillo";
	/** stoDisponible
	 * decimal number */
	public static final String COLUMNA_STODISPONIBLE = "stoDisponible";
	/** stoPteRecibir
	 * decimal number */
	public static final String COLUMNA_STOPTERECIBIR = "stoPteRecibir";
	/** datFechaEntradaPrevista
	 * string */
	public static final String COLUMNA_DATFECHAENTRADAPREVISTA = "datFechaEntradaPrevista";
	/** ordTalla
	 * integer */
	public static final String COLUMNA_ORDTALLA = "ordTalla";
	/** ordColor
	 * integer */
	public static final String COLUMNA_ORDCOLOR = "ordColor";
	/** preArticuloGen
	 * decimal number */
	public static final String COLUMNA_PREARTICULOGEN = "preArticuloGen";
	/** codSurtido
	 * string */
	public static final String COLUMNA_CODSURTIDO = "codSurtido";
	/** flaNoAplicarDtoPP
	 * byte */
	public static final String COLUMNA_FLANOAPLICARDTOPP = "flaNoAplicarDtoPP";
	/** datMarcas
	 * string */
	public static final String COLUMNA_DATMARCAS = "datMarcas";
	/** prePuntos
	 * decimal number */
	public static final String COLUMNA_PREPUNTOS = "prePuntos";
	/** flaMuestra
	 * byte */
	public static final String COLUMNA_FLAMUESTRA = "flaMuestra";
	
	/**
	 * GET api/iArticulosTyCs?empresa={empresa}&codmodelotyc={codmodelotyc}
	 * @param empresa
	 * @param codModeloTyC
	 * @return IArticulosTyCsModel
	 */
	public IArticulosTyCsModel apiGetArticuloTyC(Integer empresa, String codModeloTyC, String codColor, String codTalla);
	
	/**
	 * POST api/iArticulosTyCs
	 * @param articuloTyC
	 * @return boolean
	 */
	public boolean apiPostArticuloTyC(IArticulosTyCsModel articuloTyC);
	
	/**
	 * PUT api/iArticulosTyCs?empresa={empresa}&codmodelotyc={codmodelotyc}&codcolor={codcolor}&codtalla={codtalla}
	 * @param articuloTyC
	 * @return boolean
	 */
	public boolean apiPutArticuloTyC(IArticulosTyCsModel articuloTyC);
	
}
