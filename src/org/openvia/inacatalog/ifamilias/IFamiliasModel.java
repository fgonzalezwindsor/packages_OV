package org.openvia.inacatalog.ifamilias;

import java.io.Serializable;

public class IFamiliasModel implements Serializable {

	private Integer codEmpresa;
	private String codCatalogo;
	private Integer codFamilia;
	private Integer codSubFamilia;
	private String desFamilia;
	private String nomIcoFamilia;
	private Integer ordFamilia;
	private String flaIcoModificado;
	private String obsFamilia;
	private String nomImagenFam;
	private String flaImgModificado;
	
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
	public String getDesFamilia() {
		return desFamilia;
	}
	public void setDesFamilia(String desFamilia) {
		this.desFamilia = desFamilia;
	}
	public String getNomIcoFamilia() {
		return nomIcoFamilia;
	}
	public void setNomIcoFamilia(String nomIcoFamilia) {
		this.nomIcoFamilia = nomIcoFamilia;
	}
	public Integer getOrdFamilia() {
		return ordFamilia;
	}
	public void setOrdFamilia(Integer ordFamilia) {
		this.ordFamilia = ordFamilia;
	}
	public String getFlaIcoModificado() {
		return flaIcoModificado;
	}
	public void setFlaIcoModificado(String flaIcoModificado) {
		this.flaIcoModificado = flaIcoModificado;
	}
	public String getObsFamilia() {
		return obsFamilia;
	}
	public void setObsFamilia(String obsFamilia) {
		this.obsFamilia = obsFamilia;
	}
	public String getNomImagenFam() {
		return nomImagenFam;
	}
	public void setNomImagenFam(String nomImagenFam) {
		this.nomImagenFam = nomImagenFam;
	}
	public String getFlaImgModificado() {
		return flaImgModificado;
	}
	public void setFlaImgModificado(String flaImgModificado) {
		this.flaImgModificado = flaImgModificado;
	}
	
}
