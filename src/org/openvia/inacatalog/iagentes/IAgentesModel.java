package org.openvia.inacatalog.iagentes;

import java.io.Serializable;

public class IAgentesModel implements Serializable {

	private Integer codEmpresa;
	private String codAgente;
	private String nomAgente;
	
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
	public String getNomAgente() {
		return nomAgente;
	}
	public void setNomAgente(String nomAgente) {
		this.nomAgente = nomAgente;
	}
	
		
}
