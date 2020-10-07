package org.openvia.inacatalog.iarticulos;

public interface I_iArticulos {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codArticulo
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
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
	/** obsArticulo
	 * string */
	public static final String COLUMNA_OBSARTICULO = "obsArticulo";
	/** hipArticulo
	 * string */
	public static final String COLUMNA_HIPARTICULO = "hipArticulo";
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
	/** codTipoArticulo
	 * string */
	public static final String COLUMNA_CODTIPOARTICULO = "codTipoArticulo";
	/** codCatalogo
	 * string */
	public static final String COLUMNA_CODCATALOGO = "codCatalogo";
	/** codFamilia
	 * integer */
	public static final String COLUMNA_CODFAMILIA = "codFamilia";
	/** codSubFamilia
	 * integer */
	public static final String COLUMNA_CODSUBFAMILIA = "codSubFamilia";
	/** codGrupoPreciosArticulo
	 * string */
	public static final String COLUMNA_CODGRUPOPRECIOSARTICULO = "codGrupoPreciosArticulo";
	/** tpcIva
	 * decimal number */
	public static final String COLUMNA_TPCIVA = "tpcIva";
	/** tpcRe
	 * decimal number */
	public static final String COLUMNA_TPCRE = "tpcRe";
	/** tpcIGIC
	 * decimal number */
	public static final String COLUMNA_TPCIGIC = "tpcIGIC";
	/** codModeloTyC
	 * string */
	public static final String COLUMNA_CODMODELOTYC = "codModeloTyC";
	/** desModeloTyC
	 * string */
	public static final String COLUMNA_DESMODELOTYC = "desModeloTyC";
	/** stoDisponible
	 * decimal number */
	public static final String COLUMNA_STODISPONIBLE = "stoDisponible";
	/** stoPteRecibir
	 * decimal number */
	public static final String COLUMNA_STOPTERECIBIR = "stoPteRecibir";
	/** datFechaEntradaPrevista
	 * string */
	public static final String COLUMNA_DATFECHAENTRADAPREVISTA = "datFechaEntradaPrevista";
	/** ordArticulo
	 * integer */
	public static final String COLUMNA_ORDARTICULO = "ordArticulo";
	/** preArticuloGen
	 * decimal number */
	public static final String COLUMNA_PREARTICULOGEN = "preArticuloGen";
	/** datNivel1	
	 * string */
	public static final String COLUMNA_DATNIVEL1 = "datNivel1";
	/** datNivel2
	 * string */
	public static final String COLUMNA_DATNIVEL2 = "datNivel2";
	/** codEmpSuministradora
	 * integer */
	public static final String COLUMNA_CODEMPSUMINISTRADORA = "codEmpSuministradora";
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
	 * GET api/iArticulos?empresa={empresa}&codarticulo={codarticulo}
	 * @param empresa
	 * @param codArticulo
	 * @return IArticulosModel
	 */
	public IArticulosModel apiGetArticulo(Integer empresa, String codArticulo);
	
	/**
	 * POST api/iArticulos
	 * @param articulo
	 * @return boolean
	 */
	public boolean apiPostArticulo(IArticulosModel articulo);
	
	/**
	 * PUT api/iArticulos?empresa={empresa}&codarticulo={codarticulo}
	 * @param articulo
	 * @return boolean
	 */
	public boolean apiPutArticulo(IArticulosModel articulo);
	
}
