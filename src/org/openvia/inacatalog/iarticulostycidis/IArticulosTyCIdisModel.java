package org.openvia.inacatalog.iarticulostycidis;

import java.io.Serializable;

public class IArticulosTyCIdisModel implements Serializable {

	private Integer codEmpresa;
	private String codArticulo;
	private String codModeloTyC;
	private String codIdiomaDestino;
	private String codColor;
	private String desColor;
	private String codTalla;
	private String desTalla;
	private String desArticulo;
	private String datMedidas;
	private String datPeso;
	private String datVolumen;
	private String datFechaEntradaPrevista;
	
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
	public String getCodModeloTyC() {
		return codModeloTyC;
	}
	public void setCodModeloTyC(String codModeloTyC) {
		this.codModeloTyC = codModeloTyC;
	}
	public String getCodIdiomaDestino() {
		return codIdiomaDestino;
	}
	public void setCodIdiomaDestino(String codIdiomaDestino) {
		this.codIdiomaDestino = codIdiomaDestino;
	}
	public String getCodColor() {
		return codColor;
	}
	public void setCodColor(String codColor) {
		this.codColor = codColor;
	}
	public String getDesColor() {
		return desColor;
	}
	public void setDesColor(String desColor) {
		this.desColor = desColor;
	}
	public String getCodTalla() {
		return codTalla;
	}
	public void setCodTalla(String codTalla) {
		this.codTalla = codTalla;
	}
	public String getDesTalla() {
		return desTalla;
	}
	public void setDesTalla(String desTalla) {
		this.desTalla = desTalla;
	}
	public String getDesArticulo() {
		return desArticulo;
	}
	public void setDesArticulo(String desArticulo) {
		this.desArticulo = desArticulo;
	}
	public String getDatMedidas() {
		return datMedidas;
	}
	public void setDatMedidas(String datMedidas) {
		this.datMedidas = datMedidas;
	}
	public String getDatPeso() {
		return datPeso;
	}
	public void setDatPeso(String datPeso) {
		this.datPeso = datPeso;
	}
	public String getDatVolumen() {
		return datVolumen;
	}
	public void setDatVolumen(String datVolumen) {
		this.datVolumen = datVolumen;
	}
	public String getDatFechaEntradaPrevista() {
		return datFechaEntradaPrevista;
	}
	public void setDatFechaEntradaPrevista(String datFechaEntradaPrevista) {
		this.datFechaEntradaPrevista = datFechaEntradaPrevista;
	}

}
