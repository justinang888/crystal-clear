package com.spring.aurora.entity;

import java.sql.Date;

public class DailyTotalsEntity {

	private Date date;
	private Double totalCash;
	private Double totalAr;
	private Double totalExpenses;
	private Double totalPayments;
	private int totalDeliveredRound;
	private int totalDeliveredSlim;
	private int totalDeliveredContainers;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Double getTotalCash() {
		return totalCash;
	}
	public void setTotalCash(Double totalCash) {
		this.totalCash = totalCash;
	}
	public Double getTotalAr() {
		return totalAr;
	}
	public void setTotalAr(Double totalAr) {
		this.totalAr = totalAr;
	}
	public Double getTotalExpenses() {
		return totalExpenses;
	}
	public void setTotalExpenses(Double totalExpenses) {
		this.totalExpenses = totalExpenses;
	}
	public Double getTotalPayments() {
		return totalPayments;
	}
	public void setTotalPayments(Double totalPayments) {
		this.totalPayments = totalPayments;
	}
	public int getTotalDeliveredRound() {
		return totalDeliveredRound;
	}
	public void setTotalDeliveredRound(int totalDeliveredRound) {
		this.totalDeliveredRound = totalDeliveredRound;
	}
	public int getTotalDeliveredSlim() {
		return totalDeliveredSlim;
	}
	public void setTotalDeliveredSlim(int totalDeliveredSlim) {
		this.totalDeliveredSlim = totalDeliveredSlim;
	}
	public int getTotalDeliveredContainers() {
		return totalDeliveredContainers;
	}
	public void setTotalDeliveredContainers(int totalDeliveredContainers) {
		this.totalDeliveredContainers = totalDeliveredContainers;
	}
	
}
