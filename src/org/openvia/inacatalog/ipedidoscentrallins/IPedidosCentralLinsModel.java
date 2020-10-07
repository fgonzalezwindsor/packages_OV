package org.openvia.inacatalog.ipedidoscentrallins;

import java.io.Serializable;

public class IPedidosCentralLinsModel implements Serializable {

	private Integer codEmpresa;
	private String codPedido;
	private Integer linPedido;
	private String codArticulo;
	private String desLinPed;
	private String codMagnitud;
	private Double canLinPed;
	private Double canIndicada;
	private Double tpcDto01;
	private Double tpcDto02;
	private Double preLinPed;
	private Double impBaseImponibleLinPed;
	private String codCatalogo;
	private Integer codFamilia;
	private Integer codSubFamilia;
	private Double canLinPedPte;
	
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
	public Integer getLinPedido() {
		return linPedido;
	}
	public void setLinPedido(Integer linPedido) {
		this.linPedido = linPedido;
	}
	public String getCodArticulo() {
		return codArticulo;
	}
	public void setCodArticulo(String codArticulo) {
		this.codArticulo = codArticulo;
	}
	public String getDesLinPed() {
		return desLinPed;
	}
	public void setDesLinPed(String desLinPed) {
		this.desLinPed = desLinPed;
	}
	public String getCodMagnitud() {
		return codMagnitud;
	}
	public void setCodMagnitud(String codMagnitud) {
		this.codMagnitud = codMagnitud;
	}
	public Double getCanLinPed() {
		return canLinPed;
	}
	public void setCanLinPed(Double canLinPed) {
		this.canLinPed = canLinPed;
	}
	public Double getCanIndicada() {
		return canIndicada;
	}
	public void setCanIndicada(Double canIndicada) {
		this.canIndicada = canIndicada;
	}
	public Double getTpcDto01() {
		return tpcDto01;
	}
	public void setTpcDto01(Double tpcDto01) {
		this.tpcDto01 = tpcDto01;
	}
	public Double getTpcDto02() {
		return tpcDto02;
	}
	public void setTpcDto02(Double tpcDto02) {
		this.tpcDto02 = tpcDto02;
	}
	public Double getPreLinPed() {
		return preLinPed;
	}
	public void setPreLinPed(Double preLinPed) {
		this.preLinPed = preLinPed;
	}
	public Double getImpBaseImponibleLinPed() {
		return impBaseImponibleLinPed;
	}
	public void setImpBaseImponibleLinPed(Double impBaseImponibleLinPed) {
		this.impBaseImponibleLinPed = impBaseImponibleLinPed;
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
	public Double getCanLinPedPte() {
		return canLinPedPte;
	}
	public void setCanLinPedPte(Double canLinPedPte) {
		this.canLinPedPte = canLinPedPte;
	}
			
}
