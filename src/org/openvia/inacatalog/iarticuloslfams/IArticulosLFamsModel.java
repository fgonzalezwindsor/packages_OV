package org.openvia.inacatalog.iarticuloslfams;

import java.io.Serializable;

public class IArticulosLFamsModel implements Serializable {

	private Integer codEmpresa;
	private String codArticulo;
	private String codCatalogo;
	private Integer codFamilia;
	private Integer codSubFamilia;
	private Integer ordArticulo;
	
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
	public String getCodCatalogo() {
		return codCatalogo;
	}
	public void setCodCatalogo(String codCatalogo) {
		this.codCatalogo = codCatalogo;
	}
	public Integer getCodFamilia() {
		return codFamilia;
	}
	public void setCodFamilia(Integer codFamilia) {
		this.codFamilia = codFamilia;
	}
	public Integer getCodSubFamilia() {
		return codSubFamilia;
	}
	public void setCodSubFamilia(Integer codSubFamilia) {
		this.codSubFamilia = codSubFamilia;
	}
	public Integer getOrdArticulo() {
		return ordArticulo;
	}
	public void setOrdArticulo(Integer ordArticulo) {
		this.ordArticulo = ordArticulo;
	}
	
		
}
