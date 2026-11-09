package com.spring.aurora.model;

import java.util.Set;

/**
 * How a customer paid: payment.payment_type. Cash goes to the drawer, GCash to the wallet, Bank
 * Transfer to the account, Check to the bank later. An order's amount paid on delivery is cash.
 */
public final class PaymentTypes {

    public static final String CASH = "CASH";
    public static final String CHECK = "CHECK";
    public static final String GCASH = "GCASH";
    public static final String BANK_TRANSFER = "BANK_TRANSFER";

    public static final Set<String> ALL = Set.of(CASH, CHECK, GCASH, BANK_TRANSFER);

    private PaymentTypes() {
    }
}
