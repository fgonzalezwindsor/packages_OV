package org.openvia.inacatalog.ipedidoscentrals;

import java.io.Serializable;

public class IPedidosCentralsModel implements Serializable {

	private Integer codEmpresa;
	private String codPedido;
	private String fecPedido;
	private String codCliente;
	private Integer linDirCli;
	private String codFormaPago;
	private Double tpcDtoPp;
	private Double tpcDto03;
	private String codMoneda;
	private String codIncoterm;
	private Double totBrutoPed;
	private Double totBaseImponiblePed;
	private Double totIVAPed;
	private Double totREPed;
	private String datFechaEntrega;
	private String datEstadoPedido;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodPedido() {
		return codPedido;
	}
	public void setCodPedido(String codPedido) {
		this.codPedido = codPedido;
	}
	public String getFecPedido() {
		return fecPedido;
	}
	public void setFecPedido(String fecPedido) {
		this.fecPedido = fecPedido;
	}
	public String getCodCliente() {
		return codCliente;
	}
	public void setCodCliente(String codCliente) {
		this.codCliente = codCliente;
	}
	public Integer getLinDirCli() {
		return linDirCli;
	}
	public void setLinDirCli(Integer linDirCli) {
		this.linDirCli = linDirCli;
	}
	public String getCodFormaPago() {
		return codFormaPago;
	}
	public void setCodFormaPago(String codFormaPago) {
		this.codFormaPago = codFormaPago;
	}
	public Double getTpcDtoPp() {
		return tpcDtoPp;
	}
	public void setTpcDtoPp(Double tpcDtoPp) {
		this.tpcDtoPp = tpcDtoPp;
	}
	public Double getTpcDto03() {
		return tpcDto03;
	}
	public void setTpcDto03(Double tpcDto03) {
		this.tpcDto03 = tpcDto03;
	}
	public String getCodMoneda() {
		return codMoneda;
	}
	public void setCodMoneda(String codMoneda) {
		this.codMoneda = codMoneda;
	}
	public String getCodIncoterm() {
		return codIncoterm;
	}
	public void setCodIncoterm(String codIncoterm) {
		this.codIncoterm = codIncoterm;
	}
	public Double getTotBrutoPed() {
		return totBrutoPed;
	}
	public void setTotBrutoPed(Double totBrutoPed) {
		this.totBrutoPed = totBrutoPed;
	}
	public Double getTotBaseImponiblePed() {
		return totBaseImponiblePed;
	}
	public void setTotBaseImponiblePed(Double totBaseImponiblePed) {
		this.totBaseImponiblePed = totBaseImponiblePed;
	}
	public Double getTotIVAPed() {
		return totIVAPed;
	}
	public void setTotIVAPed(Double totIVAPed) {
		this.totIVAPed = totIVAPed;
	}
	public Double getTotREPed() {
		return totREPed;
	}
	public void setTotREPed(Double totREPed) {
		this.totREPed = totREPed;
	}
	public String getDatFechaEntrega() {
		return datFechaEntrega;
	}
	public void setDatFechaEntrega(String datFechaEntrega) {
		this.datFechaEntrega = datFechaEntrega;
	}
	public String getDatEstadoPedido() {
		return datEstadoPedido;
	}
	public void setDatEstadoPedido(String datEstadoPedido) {
		this.datEstadoPedido = datEstadoPedido;
	}
		
}
