package org.openvia.inacatalog.iclientes;

public interface I_iClientes {

	/** codEmpresa
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codCliente
	 * string */
	public static final String COLUMNA_CODCLIENTE = "codCliente";
	/** nomCliente
	 * string */
	public static final String COLUMNA_NOMCLIENTE = "nomCliente";
	/** rsoCliente
	 * string */
	public static final String COLUMNA_RSOCLIENTE = "rsoCliente";
	/** cifCliente
	 * string */
	public static final String COLUMNA_CIFCLIENTE = "cifCliente";
	/** codZona
	 * string */
	public static final String COLUMNA_CODZONA = "codZona";
	/** codAgente
	 * string */
	public static final String COLUMNA_CODAGENTE = "codAgente";
	/** codTipoCliente
	 * string */
	public static final String COLUMNA_CODTIPOCLIENTE = "codTipoCliente";
	/** tipIVA
	 * string */
	public static final String COLUMNA_TIPIVA = "tipIVA";
	/** tpcDto01
	 * decimal number */
	public static final String COLUMNA_TPCDTO01 = "tpcDto01";
	/** tpcDto02
	 * decimal number */
	public static final String COLUMNA_TPCDTO02 = "tpcDto02";
	/** tpcDtoPp
	 * decimal number */
	public static final String COLUMNA_TPCDTOPP = "tpcDtoPp";
	/** codFormaPago
	 * string */
	public static final String COLUMNA_CODFORMAPAGO = "codFormaPago";
	/** flaNvoCliente
	 * byte */
	public static final String COLUMNA_FLANVOCLIENTE = "flaNvoCliente";
	/** flaExpCliente
	 * byte */
	public static final String COLUMNA_FLAEXPCLIENTE = "flaExpCliente";
	/** codTarifa
	 * string */
	public static final String COLUMNA_CODTARIFA = "codTarifa";
	/** codGrupoPreciosCliente
	 * string */
	public static final String COLUMNA_CODGRUPOPRECIOSCLIENTE = "codGrupoPreciosCliente";
	/** flaObsoleto
	 * byte */
	public static final String COLUMNA_FLAOBSOLETO = "flaObsoleto";
	/** impPendienteRiesgo
	 * decimal number */
	public static final String COLUMNA_IMPPENDIENTERIESGO = "impPendienteRiesgo";
	/** impVencidoRiesgo
	 * decimal number */
	public static final String COLUMNA_IMPVENCIDORIESGO = "impVencidoRiesgo";
	/** impImpagadoRiesgo
	 * decimal number */
	public static final String COLUMNA_IMPIMPAGADORIESGO = "impImpagadoRiesgo";
	/** impCoberturaRiesgo
	 * decimal number */
	public static final String COLUMNA_IMPCOBERTURARIESGO = "impCoberturaRiesgo";
	/** flaBloqueaClienteRiesgo
	 * byte */
	public static final String COLUMNA_FLABLOQUEACLIENTERIESGO = "flaBloqueaClienteRiesgo";
	/** codIdioma
	 * string */
	public static final String COLUMNA_CODIDIOMA = "codIdioma";
	/** datIBAN
	 * string */
	public static final String COLUMNA_DATIBAN = "datIBAN";
	/** codSector
	 * string */
	public static final String COLUMNA_CODSECTOR = "codSector";
	/** datBlog
	 * string */
	public static final String COLUMNA_DATBLOG = "datBlog";
	/** impFacturacion
	 * decimal number */
	public static final String COLUMNA_IMPFACTURACION = "impFacturacion";
	/** obsClienteNoEdi
	 * string */
	public static final String COLUMNA_OBSCLIENTENOEDI = "obsClienteNoEdi";
	/** obsClienteEdi
	 * string */
	public static final String COLUMNA_OBSCLIENTEEDI = "obsClienteEdi";
	/** Custom1
	 * string */
	public static final String COLUMNA_CUSTOM1 = "Custom1";
	/** Custom2
	 * string */
	public static final String COLUMNA_CUSTOM2 = "Custom2";
	/** Custom3
	 * string */
	public static final String COLUMNA_CUSTOM3 = "Custom3";
	/** Custom4
	 * string */
	public static final String COLUMNA_CUSTOM4 = "Custom4";
	/** Custom5
	 * string */
	public static final String COLUMNA_CUSTOM5 = "Custom5";
	/** fecAltaCliente
	 * date */
	public static final String COLUMNA_FECALTACLIENTE = "fecAltaCliente";
	/** codMonedaRiesgo
	 * string */
	public static final String COLUMNA_CODMONEDARIESGO = "codMonedaRiesgo";
	
	
	
	
	
	/**
	 * GET api/iClientes?empresa={empresa}&codcliente={codcliente}
	 * @param empresa
	 * @param codcliente
	 * @return IClientesModel
	 */
	public IClientesModel apiGetCliente(Integer empresa, String codcliente);
	
	/**
	 * POST api/iClientes
	 * @param cliente
	 * @return boolean
	 */
	public boolean apiPostCliente(IClientesModel cliente);
	
}
