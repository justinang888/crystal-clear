package com.spring.aurora.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.spring.aurora.model.Container;
import com.spring.aurora.model.Expense;
import com.spring.aurora.model.ExpensePaymentType;
import com.spring.aurora.model.Order;
import com.spring.aurora.model.Payment;
import com.spring.aurora.model.PaymentTypes;

/**
 * The sales rules the Daily Sales report and the Monthly Summary share, so a day reads the same
 * on both. What counts:
 * <ul>
 * <li>Delivered orders only (pending and cancelled ones are left out): their amount paid is cash
 * in, their unpaid amount is A/R, their containers are delivered and their returns returned.</li>
 * <li>Payments by method: Cash, Check, GCash, Bank Transfer (anything older that isn't Cash
 * counts as Check, as the report always did).</li>
 * <li>Expenses by method: Cash, GCash, Bank Transfer.</li>
 * <li>Containers returned on their own (status R).</li>
 * </ul>
 * Each method's net is its money in minus its expenses: the drawer, the GCash wallet, the bank.
 * Amounts are added as decimals so the cents are exact.
 */
public class SalesTotals {

    private BigDecimal cashIn = BigDecimal.ZERO;
    private BigDecimal checkIn = BigDecimal.ZERO;
    private BigDecimal gcashIn = BigDecimal.ZERO;
    private BigDecimal bankIn = BigDecimal.ZERO;
    private BigDecimal cashOut = BigDecimal.ZERO;
    private BigDecimal gcashOut = BigDecimal.ZERO;
    private BigDecimal bankOut = BigDecimal.ZERO;
    private BigDecimal debt = BigDecimal.ZERO;
    private int roundDelivered;
    private int slimDelivered;
    private int roundReturned;
    private int slimReturned;
    private final Set<Long> customers = new HashSet<>();
    private final Set<Long> served = new HashSet<>();

    private static BigDecimal money(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    public static boolean isDelivered(Order order) {
        return "Delivered".equalsIgnoreCase(order.getStatus());
    }

    /** Adds a delivered order; returns false (and adds nothing) for a pending or cancelled one. */
    public boolean addOrder(Order o) {
        if (!isDelivered(o)) {
            return false;
        }
        this.cashIn = this.cashIn.add(money(o.getAmountPaid()));
        this.debt = this.debt.add(money(o.getTotalAmount()).subtract(money(o.getAmountPaid())));
        this.roundDelivered += o.getRoundRefillOnlyCount() + o.getRoundContainerOnlyCount()
                + o.getRoundRefillWithContainerCount() + o.getRoundFreeCount();
        this.slimDelivered += o.getSlimRefillOnlyCount() + o.getSlimContainerOnlyCount()
                + o.getSlimRefillWithContainerCount() + o.getSlimFreeCount();
        this.roundReturned += parseCount(o.getRoundReturned());
        this.slimReturned += parseCount(o.getSlimReturned());
        this.customers.add(o.getCustomerId());
        this.served.add(o.getCustomerId());
        return true;
    }

    /** Adds a customer payment under its method. */
    public void addPayment(Payment p) {
        BigDecimal amount = money(p.getAmount());
        switch (p.getPaymentType() == null ? "" : p.getPaymentType().toUpperCase()) {
            case PaymentTypes.CASH -> this.cashIn = this.cashIn.add(amount);
            case PaymentTypes.GCASH -> this.gcashIn = this.gcashIn.add(amount);
            case PaymentTypes.BANK_TRANSFER -> this.bankIn = this.bankIn.add(amount);
            default -> this.checkIn = this.checkIn.add(amount);
        }
        this.customers.add(p.getCustomerId());
    }

    /** Adds an expense under how it was paid. */
    public void addExpense(Expense e) {
        BigDecimal amount = money(e.getAmount());
        ExpensePaymentType type = e.getPaymentType() == null ? ExpensePaymentType.CASH : e.getPaymentType();
        switch (type) {
            case GCASH -> this.gcashOut = this.gcashOut.add(amount);
            case BANK_TRANSFER -> this.bankOut = this.bankOut.add(amount);
            default -> this.cashOut = this.cashOut.add(amount);
        }
    }

    /** Adds containers returned without an order; returns false for other container activity. */
    public boolean addContainerReturn(Container c) {
        if (!"R".equalsIgnoreCase(c.getStatus())) {
            return false;
        }
        this.roundReturned += c.getRoundCount();
        this.slimReturned += c.getSlimCount();
        this.customers.add(c.getCustomerId());
        return true;
    }

    private static int parseCount(String count) {
        try {
            return count == null || count.isBlank() ? 0 : Integer.parseInt(count.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String format(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * The totals under the names the Daily Sales and Monthly Summary screens read. Money is a
     * string with two decimals ("365.00", "-330.00"), counts are numbers.
     */
    public Map<String, Object> attributes() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("totalCashPayments", format(this.cashIn));
        a.put("totalCheckPayments", format(this.checkIn));
        a.put("totalGcashPayments", format(this.gcashIn));
        a.put("totalBankTransferPayments", format(this.bankIn));
        a.put("totalCashExpenses", format(this.cashOut));
        a.put("totalGcashExpenses", format(this.gcashOut));
        a.put("totalBankTransferExpenses", format(this.bankOut));
        a.put("totalOtherExpenses", format(this.gcashOut.add(this.bankOut)));
        a.put("totalExpenses", format(this.cashOut.add(this.gcashOut).add(this.bankOut)));
        a.put("netCash", format(this.cashIn.subtract(this.cashOut)));
        a.put("netGcash", format(this.gcashIn.subtract(this.gcashOut)));
        a.put("netBankTransfer", format(this.bankIn.subtract(this.bankOut)));
        a.put("totalDebt", format(this.debt));
        a.put("totalRoundDelivered", this.roundDelivered);
        a.put("totalSlimDelivered", this.slimDelivered);
        a.put("totalRoundReturned", this.roundReturned);
        a.put("totalSlimReturned", this.slimReturned);
        a.put("totalCustomers", this.customers.size());
        a.put("customersServed", this.served.size());
        return a;
    }
}
