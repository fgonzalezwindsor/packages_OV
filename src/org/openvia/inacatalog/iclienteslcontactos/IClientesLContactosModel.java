package org.openvia.inacatalog.iclienteslcontactos;

import java.io.Serializable;

public class IClientesLContactosModel implements Serializable {
	
	private Integer codEmpresa;
	private String codCliente;
	private Integer linContactCli;
	private String nomContactCli;
	private String datPuestoContactCli;
	private String datTelefonoContactCli;
	private String datEmailContactCli;
	private String Custom1ContactCli;
	private String Custom2ContactCli;
	private String Custom3ContactCli;
	private Integer flaNvoContactCli;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodCliente() {
		return codCliente;
	}
	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}
	public Integer getLinContactCli() {
		return linContactCli;
	}
	public void setLinContactCli(Integer linContactCli) {
		this.linContactCli = linContactCli;
	}
	public String getNomContactCli() {
		return nomContactCli;
	}
	public void setNomContactCli(String nomContactCli) {
		this.nomContactCli = nomContactCli;
	}
	public String getDatPuestoContactCli() {
		return datPuestoContactCli;
	}
	public void setDatPuestoContactCli(String datPuestoContactCli) {
		this.datPuestoContactCli = datPuestoContactCli;
	}
	public String getDatTelefonoContactCli() {
		return datTelefonoContactCli;
	}
	public void setDatTelefonoContactCli(String datTelefonoContactCli) {
		this.datTelefonoContactCli = datTelefonoContactCli;
	}
	public String getDatEmailContactCli() {
		return datEmailContactCli;
	}
	public void setDatEmailContactCli(String datEmailContactCli) {
		this.datEmailContactCli = datEmailContactCli;
	}
	public String getCustom1ContactCli() {
		return Custom1ContactCli;
	}
	public void setCustom1ContactCli(String custom1ContactCli) {
		Custom1ContactCli = custom1ContactCli;
	}
	public String getCustom2ContactCli() {
		return Custom2ContactCli;
	}
	public void setCustom2ContactCli(String custom2ContactCli) {
		Custom2ContactCli = custom2ContactCli;
	}
	public String getCustom3ContactCli() {
		return Custom3ContactCli;
	}
	public void setCustom3ContactCli(String custom3ContactCli) {
		Custom3ContactCli = custom3ContactCli;
	}
	public Integer getFlaNvoContactCli() {
		return flaNvoContactCli;
	}
	public void setFlaNvoContactCli(Integer flaNvoContactCli) {
		this.flaNvoContactCli = flaNvoContactCli;
	}
	
}
