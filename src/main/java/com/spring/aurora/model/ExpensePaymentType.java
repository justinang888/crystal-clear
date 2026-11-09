package com.spring.aurora.model;

/** How an expense was paid. Only cash comes out of the drawer. */
public enum ExpensePaymentType {
    CASH("Cash"),
    GCASH("GCash"),
    BANK_TRANSFER("Bank Transfer");

    private final String label;

    ExpensePaymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
