package org.openvia.inacatalog.iagenteslcats;

import java.io.Serializable;

public class IAgentesLCatsModel implements Serializable {

	private Integer codEmpresa;
	private String codAgente;
	private String codCatalogo;
	
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
	public String getCodCatalogo() {
		return codCatalogo;
	}
	public void setCodCatalogo(String codCatalogo) {
		this.codCatalogo = codCatalogo;
	}
		
}
