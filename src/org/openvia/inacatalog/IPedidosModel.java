package org.openvia.inacatalog;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class IPedidosModel implements Serializable {

	private List<String> listMsg1;
	private List<String> listMsg2;
	private List<String> listMsg3;
	private List<String> listMsg4;
	private List<String> listMsg5;
	private List<IPedidosLinsModel> listIPedidosLins;
	private Integer orderID;
	private String documentNo;
	
	public List<IPedidosLinsModel> getListIPedidosLins() {
		return listIPedidosLins;
	}
	public void setListIPedidosLins(List<IPedidosLinsModel> listIPedidosLins) {
		this.listIPedidosLins = listIPedidosLins;
	}
	public Integer getOrderID() {
		return orderID;
	}
	public void setOrderID(Integer orderID) {
		this.orderID = orderID;
	}
	public String getDocumentNo() {
		return documentNo;
	}
	public void setDocumentNo(String documentNo) {
		this.documentNo = documentNo;
	}
	public List<String> getListMsg1() {
		return listMsg1;
	}
	public void setListMsg1(List<String> listMsg1) {
		this.listMsg1 = listMsg1;
	}
	public List<String> getListMsg2() {
		return listMsg2;
	}
	public void setListMsg2(List<String> listMsg2) {
		this.listMsg2 = listMsg2;
	}
	public List<String> getListMsg3() {
		return listMsg3;
	}
	public void setListMsg3(List<String> listMsg3) {
		this.listMsg3 = listMsg3;
	}
	public List<String> getListMsg4() {
		return listMsg4;
	}
	public void setListMsg4(List<String> listMsg4) {
		this.listMsg4 = listMsg4;
	}
	public List<String> getListMsg5() {
		return listMsg5;
	}
	public void setListMsg5(List<String> listMsg5) {
		this.listMsg5 = listMsg5;
	}
	
}
