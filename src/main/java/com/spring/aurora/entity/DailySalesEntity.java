package com.spring.aurora.entity;

import com.spring.aurora.model.Container;
import com.spring.aurora.model.Debt;
import com.spring.aurora.model.Expense;
import com.spring.aurora.model.Order;
import com.spring.aurora.model.Payment;

public class DailySalesEntity {

	private Order order;
	private Container container;
	private Debt debt;
	private Payment payment;
	private Expense expense;
	private String customerName;
	private Double expenseAmount;
	private Double paidCash;
	private Double paidCheck;
	private Double balanceAmount;
	private String remarks;
	private String dateAndTime;
	private String returnedRound;
	private String returnedSlim;
	/** For an expense row: how it was paid (Cash, GCash, Bank Transfer). */
	private String expensePaymentType;
	/** For a payment row paid by GCash or by bank transfer: the amount, in that method's column. */
	private Double paidGcash;
	private Double paidBankTransfer;

	public Double getPaidGcash() {
		return paidGcash;
	}

	public void setPaidGcash(Double paidGcash) {
		this.paidGcash = paidGcash;
	}

	public Double getPaidBankTransfer() {
		return paidBankTransfer;
	}

	public void setPaidBankTransfer(Double paidBankTransfer) {
		this.paidBankTransfer = paidBankTransfer;
	}

	public String getExpensePaymentType() {
		return expensePaymentType;
	}

	public void setExpensePaymentType(String expensePaymentType) {
		this.expensePaymentType = expensePaymentType;
	}
	
	public String getDateAndTime() {
		return this.dateAndTime;
	}
	public void setDateAndTime(String dateAndTime) {
		this.dateAndTime = dateAndTime;
	}
	public Order getOrder() {
		return this.order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public Container getContainer() {
		return this.container;
	}
	public void setContainer(Container container) {
		this.container = container;
	}
	public Debt getDebt() {
		return this.debt;
	}
	public void setDebt(Debt debt) {
		this.debt = debt;
	}
	public Payment getPayment() {
		return this.payment;
	}
	public void setPayment(Payment payment) {
		this.payment = payment;
	}
	public String getCustomerName() {
		return this.customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public Expense getExpense() {
		return this.expense;
	}
	public void setExpense(Expense expense) {
		this.expense = expense;
	}
	public Double getExpenseAmount() {
		return this.expenseAmount;
	}
	public void setExpenseAmount(Double expenseAmount) {
		this.expenseAmount = expenseAmount;
	}
	public Double getPaidCash() {
		return this.paidCash;
	}
	public void setPaidCash(Double paidCash) {
		this.paidCash = paidCash;
	}
	public Double getPaidCheck() {
		return this.paidCheck;
	}
	public void setPaidCheck(Double paidCheck) {
		this.paidCheck = paidCheck;
	}
	public Double getBalanceAmount() {
		return this.balanceAmount;
	}
	public void setBalanceAmount(Double balanceAmount) {
		this.balanceAmount = balanceAmount;
	}
	public String getRemarks() {
		return this.remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getReturnedRound() {
		return this.returnedRound;
	}
	public void setReturnedRound(String returnedRound) {
		this.returnedRound = returnedRound;
	}
	public String getReturnedSlim() {
		return this.returnedSlim;
	}
	public void setReturnedSlim(String returnedSlim) {
		this.returnedSlim = returnedSlim;
	}
	
}
