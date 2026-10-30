package com.spring.aurora.entity;

import java.util.List;

import com.spring.aurora.model.Order;
import com.spring.aurora.model.OrderProduct;

public class OrderProductEntity {

	private Order order;
	private int orderInterval;
	private List<OrderProduct> opList;
	private String saveReturned;
	
	public int getOrderInterval() {
		return this.orderInterval;
	}
	public void setOrderInterval(int orderInterval) {
		this.orderInterval = orderInterval;
	}
	public Order getOrder() {
		return this.order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public List<OrderProduct> getOpList() {
		return this.opList;
	}
	public void setOpList(List<OrderProduct> opList) {
		this.opList = opList;
	}
	public String getSaveReturned() {
		return this.saveReturned;
	}
	public void setSaveReturned(String saveReturned) {
		this.saveReturned = saveReturned;
	}
	
}
