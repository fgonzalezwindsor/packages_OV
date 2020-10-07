package org.openvia.inacatalog;

import org.json.simple.JSONArray;

public interface I_iPedidosLins {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** nomIPad
	 * string */
	public static final String COLUMNA_NOMIPAD = "nomIPad";
	/** codPedido
	 * integer */
	public static final String COLUMNA_CODPEDIDO = "codPedido";
	/** linPedido
	 * integer */
	public static final String COLUMNA_LINPEDIDO = "linPedido";
	/** linMadre
	 * integer */
	public static final String COLUMNA_LINMADRE = "linMadre";
	/** sliPedido
	 * integer */
	public static final String COLUMNA_SLIPEDIDO = "sliPedido";
	/** codArticulo
	 * string */
	public static final String COLUMNA_CODARTICULO = "codArticulo";
	/** desLinPed
	 * string */
	public static final String COLUMNA_DESLINPED = "desLinPed";
	/** tpcDto01
	 * decimal number */
	public static final String COLUMNA_TPCDTO01 = "tpcDto01";
	/** tpcDto02 
	 * decimal number */
	public static final String COLUMNA_TPCDTO02 = "tpcDto02";
	/** codTarifa
	 * string */
	public static final String COLUMNA_CODTARIFA = "codTarifa";
	/** codMagnitud
	 * string */
	public static final String COLUMNA_CODMAGNITUD = "codMagnitud";
	/** canLinPed
	 * decimal number */
	public static final String COLUMNA_CANLINPED = "canLinPed";
	/** preLinPed
	 * decimal number */
	public static final String COLUMNA_PRELINPED = "preLinPed";
	/** preLinPedSinIVA
	 * decimal number */
	public static final String COLUMNA_PRELINPEDSINIVA = "preLinPedSinIVA";
	/** codOrigenPrecio
	 * string */
	public static final String COLUMNA_CODORIGENPRECIO = "codOrigenPrecio";
	/** obsLinPed
	 * string */
	public static final String COLUMNA_OBSLINPED = "obsLinPed";
	/** obsLinPed2
	 * string */
	public static final String COLUMNA_OBSLINPED2 = "obsLinPed2";
	/** obsLinPed3
	 * string */
	public static final String COLUMNA_OBSLINPED3 = "obsLinPed3";
	/** impBrutoLinPed
	 * decimal number */
	public static final String COLUMNA_IMPBRUTOLINPED = "impBrutoLinPed";
	/** impDto1LinPed
	 * decimal number */
	public static final String COLUMNA_IMPDTO1LINPED = "impDto1LinPed";
	/** impDto2LinPed
	 * decimal number */
	public static final String COLUMNA_IMPDTO2LINPED = "impDto2LinPed";
	/** impDto3LinPed
	 * decimal number */
	public static final String COLUMNA_IMPDTO3LINPED = "impDto3LinPed";
	/** impNetoLinPed
	 * decimal number */
	public static final String COLUMNA_IMPNETOLINPED = "impNetoLinPed";
	/** impDtoPPLinPed
	 * decimal number */
	public static final String COLUMNA_IMPDTOPPLINPED = "impDtoPPLinPed";
	/** impBaseImponibleLinPed
	 * decimal number */
	public static final String COLUMNA_IMPBASEIMPONIBLELINPED = "impBaseImponibleLinPed";
	/** tpcIva
	 * decimal number */
	public static final String COLUMNA_TPCIVA = "tpcIva";
	/** tpcRe
	 * decimal number */
	public static final String COLUMNA_TPCRE = "tpcRe";
	/** valConjunto
	 * integer */
	public static final String COLUMNA_VALCONJUNTO = "valConjunto";
	/** canIndicada
	 * decimal number */
	public static final String COLUMNA_CANINDICADA = "canIndicada";
	/** codCatalogo
	 * string */
	public static final String COLUMNA_CODCATALOGO = "codCatalogo";
	/** codFamilia
	 * integer */
	public static final String COLUMNA_CODFAMILIA = "codFamilia";
	/** codSubFamilia
	 * integer */
	public static final String COLUMNA_CODSUBFAMILIA = "codSubFamilia";
	/** codPlantillaComercialLP	
	 * string */
	public static final String COLUMNA_CODPLANTILLACOMERCIALLP = "codPlantillaComercialLP";
	/** codPlantillaComercialVolumen
	 * string */
	public static final String COLUMNA_CODPLANTILLACOMERCIALVOLUMEN = "codPlantillaComercialVolumen";
	/** tipDto01Accion
	 * string */
	public static final String COLUMNA_TIPDTO01ACCION = "tipDto01Accion";
	/** tpcModifDto01
	 * decimal number */
	public static final String COLUMNA_TPCMODIFDTO01 = "tpcModifDto01";
	/** tipDto02Accion
	 * string */
	public static final String COLUMNA_TIPDTO02ACCION = "tipDto02Accion";
	/** tpcModifDto02
	 * decimal number */
	public static final String COLUMNA_TPCMODIFDTO02 = "tpcModifDto02";
	/** canLinPedOri
	 * decimal number */
	public static final String COLUMNA_CANLINPEDORI = "canLinPedOri";
	/** preLinPedOri
	 * decimal number */
	public static final String COLUMNA_PRELINPEDORI = "preLinPedOri";
	/** tpcDto01Ori
	 * decimal number */
	public static final String COLUMNA_TPCDTO01ORI = "tpcDto01Ori";
	/** tpcDto02Ori
	 * decimal number */
	public static final String COLUMNA_TPCDTO02ORI = "tpcDto02Ori";
	/** flaOfertaAlterada
	 * byte */
	public static final String COLUMNA_FLAOFERTAALTERADA = "flaOfertaAlterada";
	/** codPlantillaComercialLM
	 * string */
	public static final String COLUMNA_CODPLANTILLACOMERCIALLM = "codPlantillaComercialLM";
	/** codTipoArticulo
	 * string */
	public static final String COLUMNA_CODTIPOARTICULO = "codTipoArticulo";
	/** codGrupoPreciosArticulo
	 * string */
	public static final String COLUMNA_CODGRUPOPRECIOSARTICULO = "codGrupoPreciosArticulo";
	/** codModeloTyC
	 * string */
	public static final String COLUMNA_CODMODELOTYC = "codModeloTyC";
	/** Puntos
	 * decimal number */
	public static final String COLUMNA_PUNTOS = "Puntos";
	
}
