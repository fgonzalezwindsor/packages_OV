package org.openvia.inacatalog.iclienteslcontactos;

public interface I_iClientesLContactos {
	
	/** codEmpresa 
	 * integer */
	public static final String COLUMNA_CODEMPRESA = "codEmpresa";
	/** codCliente
	 * string */
	public static final String COLUMNA_CODCLIENTE = "codCliente";
	/** linContactCli
	 * integer */
	public static final String COLUMNA_LINCONTACTCLI = "linContactCli";
	/** nomContactCli
	 * string */
	public static final String COLUMNA_NOMCONTACTCLI = "nomContactCli";
	/** datPuestoContactCli
	 * string */
	public static final String COLUMNA_DATPUESTOCONTACTCLI = "datPuestoContactCli";
	/** datTelefonoContactCli
	 * string */
	public static final String COLUMNA_DATTELEFONOCONTACTCLI = "datTelefonoContactCli";
	/** datEmailContactCli
	 * string */
	public static final String COLUMNA_DATEMAILCONTACTCLI = "datEmailContactCli";
	/** Custom1ContactCli
	 * string */
	public static final String COLUMNA_CUSTOM1CONTACTCLI = "Custom1ContactCli";
	/** Custom2ContactCli
	 * string */
	public static final String COLUMNA_CUSTOM2CONTACTCLI = "Custom2ContactCli";
	/** Custom3ContactCli
	 * string */
	public static final String COLUMNA_CUSTOM3CONTACTCLI = "Custom3ContactCli";
	/** flaNvoContactCli
	 * integer */
	public static final String COLUMNA_FLANVOCONTACTCLI = "flaNvoContactCli";
	
	/**
	 * GET api/iClientesLContactos?empresa={empresa}&codcliente={codcliente}&lincontactcli={lincontactcli}
	 * @param empresa
	 * @param codCliente
	 * @param linContactCli
	 * @return IClientesLContactosModel
	 */
	public IClientesLContactosModel apiGetClienteLContacto(Integer empresa, String codCliente, Integer linContactCli);
	
	/**
	 * POST api/iClientesLContactos
	 * @param clienteLContacto
	 * @return boolean
	 */
	public boolean apiPostClienteLContacto(IClientesLContactosModel clienteLContacto);
	
}
