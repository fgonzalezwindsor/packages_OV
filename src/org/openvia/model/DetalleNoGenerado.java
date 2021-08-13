package org.openvia.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DetalleNoGenerado implements Serializable {

	private String codigo;
	private String descripcion;
	private BigDecimal uniNoInyectada;
	private BigDecimal demand;
	
	public DetalleNoGenerado(String codigo, String descripcion, BigDecimal uniNoInyectada, BigDecimal demand) {
		super();
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.uniNoInyectada = uniNoInyectada;
		this.demand = demand;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public BigDecimal getUniNoInyectada() {
		return uniNoInyectada;
	}
	public void setUniNoInyectada(BigDecimal uniNoInyectada) {
		this.uniNoInyectada = uniNoInyectada;
	}
	public BigDecimal getDemand() {
		return demand;
	}
	public void setDemand(BigDecimal demand) {
		this.demand = demand;
	}
	
}
