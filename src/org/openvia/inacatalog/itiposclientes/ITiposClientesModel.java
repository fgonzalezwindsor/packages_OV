package org.openvia.inacatalog.itiposclientes;

import java.io.Serializable;

public class ITiposClientesModel implements Serializable {

	private Integer codEmpresa;
	private String codTipoCliente;
	private String desTipoCliente;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodTipoCliente() {
		return codTipoCliente;
	}
	public void setCodTipoCliente(String codTipoCliente) {
		this.codTipoCliente = codTipoCliente;
	}
	public String getDesTipoCliente() {
		return desTipoCliente;
	}
	public void setDesTipoCliente(String desTipoCliente) {
		this.desTipoCliente = desTipoCliente;
	}

}
