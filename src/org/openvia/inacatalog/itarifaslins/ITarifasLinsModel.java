package org.openvia.inacatalog.itarifaslins;

import java.io.Serializable;

public class ITarifasLinsModel implements Serializable {

	private Integer codEmpresa;
	private String codTarifa;
	private String codMagnitud;
	private String codArticulo;
	private Double canMinima;
	private Double preArticulo;
	private String flaPreMagnitud;
	private Double tpcDto01Def;
	private Double tpcDto02Def;
	private Double tpcDto01Max;
	private Double tpcDto02Max;
	private Double PuntosSinDto;
	private Double PuntosConDto;
	private String flaPuntosUnitarios;
	
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
	public String getCodMagnitud() {
		return codMagnitud;
	}
	public void setCodMagnitud(String codMagnitud) {
		this.codMagnitud = codMagnitud;
	}
	public String getCodArticulo() {
		return codArticulo;
	}
	public void setCodArticulo(String codArticulo) {
		this.codArticulo = codArticulo;
	}
	public Double getCanMinima() {
		return canMinima;
	}
	public void setCanMinima(Double canMinima) {
		this.canMinima = canMinima;
	}
	public Double getPreArticulo() {
		return preArticulo;
	}
	public void setPreArticulo(Double preArticulo) {
		this.preArticulo = preArticulo;
	}
	public String getFlaPreMagnitud() {
		return flaPreMagnitud;
	}
	public void setFlaPreMagnitud(String flaPreMagnitud) {
		this.flaPreMagnitud = flaPreMagnitud;
	}
	public Double getTpcDto01Def() {
		return tpcDto01Def;
	}
	public void setTpcDto01Def(Double tpcDto01Def) {
		this.tpcDto01Def = tpcDto01Def;
	}
	public Double getTpcDto02Def() {
		return tpcDto02Def;
	}
	public void setTpcDto02Def(Double tpcDto02Def) {
		this.tpcDto02Def = tpcDto02Def;
	}
	public Double getTpcDto01Max() {
		return tpcDto01Max;
	}
	public void setTpcDto01Max(Double tpcDto01Max) {
		this.tpcDto01Max = tpcDto01Max;
	}
	public Double getTpcDto02Max() {
		return tpcDto02Max;
	}
	public void setTpcDto02Max(Double tpcDto02Max) {
		this.tpcDto02Max = tpcDto02Max;
	}
	public Double getPuntosSinDto() {
		return PuntosSinDto;
	}
	public void setPuntosSinDto(Double puntosSinDto) {
		PuntosSinDto = puntosSinDto;
	}
	public Double getPuntosConDto() {
		return PuntosConDto;
	}
	public void setPuntosConDto(Double puntosConDto) {
		PuntosConDto = puntosConDto;
	}
	public String getFlaPuntosUnitarios() {
		return flaPuntosUnitarios;
	}
	public void setFlaPuntosUnitarios(String flaPuntosUnitarios) {
		this.flaPuntosUnitarios = flaPuntosUnitarios;
	}
	
	
}
