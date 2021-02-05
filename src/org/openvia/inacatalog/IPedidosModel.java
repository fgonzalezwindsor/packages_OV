package org.openvia.inacatalog;

import java.io.Serializable;
import java.util.List;

public class IPedidosModel implements Serializable {

	private List<String> listMsg;
	private List<IPedidosLinsModel> listIPedidosLins;
	private Integer orderID;
	private String documentNo;
	
	public List<String> getListMsg() {
		return listMsg;
	}
	public void setListMsg(List<String> listMsg) {
		this.listMsg = listMsg;
	}
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
	
}
