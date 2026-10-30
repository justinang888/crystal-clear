package com.spring.aurora.entity;

import java.sql.Timestamp;

import com.spring.aurora.model.Customer;

public class CustomerPriceEntity {
    
	private Customer customer;
    private double refillPrice;
    private Timestamp mostRecentOrderDate;
    private int numberOfOrdersForTheMonth;
	
	public int getNumberOfOrdersForTheMonth() {
        return numberOfOrdersForTheMonth;
    }
    public void setNumberOfOrdersForTheMonth(int numberOfOrdersForTheMonth) {
        this.numberOfOrdersForTheMonth = numberOfOrdersForTheMonth;
    }
    public CustomerPriceEntity(Customer customer, double refillPrice) {
		super();
		this.customer = customer;
		this.refillPrice = refillPrice;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public double getRefillPrice() {
		return refillPrice;
	}
	public void setRefillPrice(double refillPrice) {
		this.refillPrice = refillPrice;
	}
	public Timestamp getMostRecentOrderDate() {
		return mostRecentOrderDate;
	}
	public void setMostRecentOrderDate(Timestamp mostRecentOrderDate) {
		this.mostRecentOrderDate = mostRecentOrderDate;
	}
   
}
