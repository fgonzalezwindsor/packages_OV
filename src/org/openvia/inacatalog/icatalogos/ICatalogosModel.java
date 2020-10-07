package org.openvia.inacatalog.icatalogos;

import java.io.Serializable;

public class ICatalogosModel implements Serializable {

	private Integer codEmpresa;
	private String codCatalogo;
	private String desCatalogo;
	private String obsCatalogo;
	private String nomImagenCat;
	private String nomIconoCat;
	private String flaIcoModificado;
	private String flaImgModificado;
	private Integer ordCatalogo;
	
	public Integer getCodEmpresa() {
		return codEmpresa;
	}
	public void setCodEmpresa(Integer codEmpresa) {
		this.codEmpresa = codEmpresa;
	}
	public String getCodCatalogo() {
		return codCatalogo;
	}
	public void setCodCatalogo(String codCatalogo) {
		this.codCatalogo = codCatalogo;
	}
	public String getDesCatalogo() {
		return desCatalogo;
	}
	public void setDesCatalogo(String desCatalogo) {
		this.desCatalogo = desCatalogo;
	}
	public String getObsCatalogo() {
		return obsCatalogo;
	}
	public void setObsCatalogo(String obsCatalogo) {
		this.obsCatalogo = obsCatalogo;
	}
	public String getNomImagenCat() {
		return nomImagenCat;
	}
	public void setNomImagenCat(String nomImagenCat) {
		this.nomImagenCat = nomImagenCat;
	}
	public String getNomIconoCat() {
		return nomIconoCat;
	}
	public void setNomIconoCat(String nomIconoCat) {
		this.nomIconoCat = nomIconoCat;
	}
	public String getFlaIcoModificado() {
		return flaIcoModificado;
	}
	public void setFlaIcoModificado(String flaIcoModificado) {
		this.flaIcoModificado = flaIcoModificado;
	}
	public String getFlaImgModificado() {
		return flaImgModificado;
	}
	public void setFlaImgModificado(String flaImgModificado) {
		this.flaImgModificado = flaImgModificado;
	}
	public Integer getOrdCatalogo() {
		return ordCatalogo;
	}
	public void setOrdCatalogo(Integer ordCatalogo) {
		this.ordCatalogo = ordCatalogo;
	}
	
	
}
