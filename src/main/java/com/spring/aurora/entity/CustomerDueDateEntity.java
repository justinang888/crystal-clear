package com.spring.aurora.entity;

import java.time.LocalDate;

import com.spring.aurora.model.Customer;

public class CustomerDueDateEntity {

	private Customer customer;
	private LocalDate lastOrderDate;
	private LocalDate dueDate;
	private long daysRemaining;
	
	public CustomerDueDateEntity(Customer customer, LocalDate lastOrderDate, LocalDate dueDate, long daysRemaining) {
		super();
		this.customer = customer;
		this.lastOrderDate = lastOrderDate;
		this.dueDate = dueDate;
		this.daysRemaining = daysRemaining;
	}
	
	public CustomerDueDateEntity(Customer customer, LocalDate lastOrderDate) {
		super();
		this.customer = customer;
		this.lastOrderDate = lastOrderDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public LocalDate getLastOrderDate() {
		return lastOrderDate;
	}

	public void setLastOrderDate(LocalDate lastOrderDate) {
		this.lastOrderDate = lastOrderDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public long getDaysRemaining() {
		return daysRemaining;
	}

	public void setDaysRemaining(long daysRemaining) {
		this.daysRemaining = daysRemaining;
	}
	
}
