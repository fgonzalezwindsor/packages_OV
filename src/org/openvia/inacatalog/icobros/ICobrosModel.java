package org.openvia.inacatalog.icobros;

import java.io.Serializable;

public class ICobrosModel implements Serializable {

	private Integer codEmpresa;
	private String codCliente;
	private String codDocumento;
	private String fecDocumento;
	private String fecVencimiento;
	private String datTipoDocumento;
	private String flaImpagado;
	private Double impPendiente;
	private String codMoneda;
	
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
	public String getCodDocumento() {
		return codDocumento;
	}
	public void setCodDocumento(String codDocumento) {
		this.codDocumento = codDocumento;
	}
	public String getFecDocumento() {
		return fecDocumento;
	}
	public void setFecDocumento(String fecDocumento) {
		this.fecDocumento = fecDocumento;
	}
	public String getFecVencimiento() {
		return fecVencimiento;
	}
	public void setFecVencimiento(String fecVencimiento) {
		this.fecVencimiento = fecVencimiento;
	}
	public String getDatTipoDocumento() {
		return datTipoDocumento;
	}
	public void setDatTipoDocumento(String datTipoDocumento) {
		this.datTipoDocumento = datTipoDocumento;
	}
	public String getFlaImpagado() {
		return flaImpagado;
	}
	public void setFlaImpagado(String flaImpagado) {
		this.flaImpagado = flaImpagado;
	}
	public Double getImpPendiente() {
		return impPendiente;
	}
	public void setImpPendiente(Double impPendiente) {
		this.impPendiente = impPendiente;
	}
	public String getCodMoneda() {
		return codMoneda;
	}
	public void setCodMoneda(String codMoneda) {
		this.codMoneda = codMoneda;
	}
		
}
