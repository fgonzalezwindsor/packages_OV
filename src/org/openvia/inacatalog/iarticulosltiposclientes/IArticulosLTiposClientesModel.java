package org.openvia.inacatalog.iarticulosltiposclientes;

import java.io.Serializable;

public class IArticulosLTiposClientesModel implements Serializable {

	private Integer codEmpresa;
	private String codArticulo;
	private String codTipoCliente;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodArticulo() {
		return codArticulo;
	}
	public void setCodArticulo(String codArticulo) {
		this.codArticulo = codArticulo;
	}
	public String getCodTipoCliente() {
		return codTipoCliente;
	}
	public void setCodTipoCliente(String codTipoCliente) {
		this.codTipoCliente = codTipoCliente;
	}

}
