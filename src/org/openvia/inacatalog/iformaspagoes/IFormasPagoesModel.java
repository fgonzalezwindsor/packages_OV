package org.openvia.inacatalog.iformaspagoes;

import java.io.Serializable;

public class IFormasPagoesModel implements Serializable {

	private Integer codEmpresa;
	private String codFormaPago;
	private String desFormaPago;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodFormaPago() {
		return codFormaPago;
	}
	public void setCodFormaPago(String codFormaPago) {
		this.codFormaPago = codFormaPago;
	}
	public String getDesFormaPago() {
		return desFormaPago;
	}
	public void setDesFormaPago(String desFormaPago) {
		this.desFormaPago = desFormaPago;
	}
	
	
}
