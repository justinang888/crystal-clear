package com.spring.aurora.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.spring.aurora.model.Expense;
import com.spring.aurora.model.Order;
import com.spring.aurora.model.Payment;

public class ReportUtil {

	public static Double getExpenseTotal (List<Expense> expenseList) {
		
		Double expenseTotal = 0.0;
		
		for (Expense e : expenseList) {
			expenseTotal += e.getAmount();
		}
		
		return expenseTotal;
	}
	
	public static Double getPaymentTotal (List<Payment> paymentList) {

		Double paymentTotal = 0.0;
		
		for (Payment p : paymentList) {
			paymentTotal += p.getAmount();
		}
		
		return paymentTotal;
	}
	
	public static int getRoundDeliveredTotal (List<Order> orderList) {
		
		int roundTotal = 0;
		
		for (Order o : orderList) {
			roundTotal += o.getRoundRefillOnlyCount();
			roundTotal += o.getRoundContainerOnlyCount();
			roundTotal += o.getRoundRefillWithContainerCount();
			roundTotal += o.getRoundFreeCount();
		}
		
		return roundTotal;
	}
	
	public static int getSlimDeliveredTotal (List<Order> orderList) {
		
		int slimTotal = 0;
		
		for (Order o : orderList) {
			slimTotal += o.getSlimRefillOnlyCount();
			slimTotal += o.getSlimContainerOnlyCount();
			slimTotal += o.getSlimRefillWithContainerCount();
			slimTotal += o.getSlimFreeCount();
		}
		
		return slimTotal;
	}
	
	public static boolean isLeapYear(int year) {

		if ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0))) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Formats an amount such as "" + someDouble with exactly two decimals, rounding half up.
	 * Handles the long binary tails of double sums (115.30000000000001) and E notation (1.2E7).
	 */
	public static String applyCurrencyFormat (String value) {
		return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}
