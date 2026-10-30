package com.spring.aurora.entity;

public class DebtCustomerEntity {
    private String customerName;
    private double debtsTotal;
    private double paymentsTotal;

    public DebtCustomerEntity(String name, double debtsTotal, double paymentsTotal) {
        this.customerName = name;
        this.debtsTotal = debtsTotal;
        this.paymentsTotal = paymentsTotal;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getDebtsTotal() {
        return debtsTotal;
    }

    public void setDebtsTotal(double debtsTotal) {
        this.debtsTotal = debtsTotal;
    }

	public double getPaymentsTotal() {
		return paymentsTotal;
	}

	public void setPaymentsTotal(double paymentsTotal) {
		this.paymentsTotal = paymentsTotal;
	}
}
