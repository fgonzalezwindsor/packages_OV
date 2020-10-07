package org.openvia.inacatalog.izonas;

import java.io.Serializable;

public class IZonasModel implements Serializable {

	private Integer codEmpresa;
	private String codZona;
	private String desZona;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodZona() {
		return codZona;
	}
	public void setCodZona(String codZona) {
		this.codZona = codZona;
	}
	public String getDesZona() {
		return desZona;
	}
	public void setDesZona(String desZona) {
		this.desZona = desZona;
	}
	
	
	
}
