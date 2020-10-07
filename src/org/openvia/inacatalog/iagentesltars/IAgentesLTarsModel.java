package org.openvia.inacatalog.iagentesltars;

import java.io.Serializable;

public class IAgentesLTarsModel implements Serializable {

	private Integer codEmpresa;
	private String codAgente;
	private String codTarifa;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodAgente() {
		return codAgente;
	}
	public void setCodAgente(String codAgente) {
		this.codAgente = codAgente;
	}
	public String getCodTarifa() {
		return codTarifa;
	}
	public void setCodTarifa(String codTarifa) {
		this.codTarifa = codTarifa;
	}
		
}
