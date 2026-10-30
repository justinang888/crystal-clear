package com.spring.aurora.entity;

import com.spring.aurora.model.Customer;

public class ContainerCustomerEntity {
    
	private Customer customer;
    private int slimTotal;
    private int roundTotal;
    private int roundTotalDelivered;
    private int slimTotalDelivered;
    private int roundTotalReturned;
    private int slimTotalReturned;

    public ContainerCustomerEntity(Customer customer, int slimTotal, int roundTotal) {
		super();
		this.customer = customer;
		this.slimTotal = slimTotal;
		this.roundTotal = roundTotal;
	}

	public ContainerCustomerEntity(Customer customer, int roundTotalDelivered, int slimTotalDelivered,
			int roundTotalReturned, int slimTotalReturned) {
		super();
		this.customer = customer;
		this.roundTotalDelivered = roundTotalDelivered;
		this.slimTotalDelivered = slimTotalDelivered;
		this.roundTotalReturned = roundTotalReturned;
		this.slimTotalReturned = slimTotalReturned;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public int getSlimTotal() {
		return slimTotal;
	}

	public void setSlimTotal(int slimTotal) {
		this.slimTotal = slimTotal;
	}

	public int getRoundTotal() {
		return roundTotal;
	}

	public void setRoundTotal(int roundTotal) {
		this.roundTotal = roundTotal;
	}

	public int getRoundTotalDelivered() {
		return roundTotalDelivered;
	}

	public void setRoundTotalDelivered(int roundTotalDelivered) {
		this.roundTotalDelivered = roundTotalDelivered;
	}

	public int getSlimTotalDelivered() {
		return slimTotalDelivered;
	}

	public void setSlimTotalDelivered(int slimTotalDelivered) {
		this.slimTotalDelivered = slimTotalDelivered;
	}

	public int getRoundTotalReturned() {
		return roundTotalReturned;
	}

	public void setRoundTotalReturned(int roundTotalReturned) {
		this.roundTotalReturned = roundTotalReturned;
	}

	public int getSlimTotalReturned() {
		return slimTotalReturned;
	}

	public void setSlimTotalReturned(int slimTotalReturned) {
		this.slimTotalReturned = slimTotalReturned;
	}

}
