package org.openvia.inacatalog.isectores;

import java.io.Serializable;

public class ISectoresModel implements Serializable {

	private Integer codEmpresa;
	private String codSector;
	private String desSector;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodSector() {
		return codSector;
	}
	public void setCodSector(String codSector) {
		this.codSector = codSector;
	}
	public String getDesSector() {
		return desSector;
	}
	public void setDesSector(String desSector) {
		this.desSector = desSector;
	}
	
}
