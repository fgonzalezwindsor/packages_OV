package org.openvia.inacatalog.itarifas;

import java.io.Serializable;

public class ITarifasModel implements Serializable {

	private Integer codEmpresa;
	private String codTarifa;
	private String desTarifa;
	private String codIncoterm;
	private String flaIVAIncluido;
	private String codMoneda;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodTarifa() {
		return codTarifa;
	}
	public void setCodTarifa(String codTarifa) {
		this.codTarifa = codTarifa;
	}
	public String getDesTarifa() {
		return desTarifa;
	}
	public void setDesTarifa(String desTarifa) {
		this.desTarifa = desTarifa;
	}
	public String getCodIncoterm() {
		return codIncoterm;
	}
	public void setCodIncoterm(String codIncoterm) {
		this.codIncoterm = codIncoterm;
	}
	public String getFlaIVAIncluido() {
		return flaIVAIncluido;
	}
	public void setFlaIVAIncluido(String flaIVAIncluido) {
		this.flaIVAIncluido = flaIVAIncluido;
	}
	public String getCodMoneda() {
		return codMoneda;
	}
	public void setCodMoneda(String codMoneda) {
		this.codMoneda = codMoneda;
	}
			
}
