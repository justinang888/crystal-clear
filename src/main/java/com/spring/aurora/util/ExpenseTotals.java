package com.spring.aurora.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.spring.aurora.model.Expense;
import com.spring.aurora.model.ExpensePaymentType;

/** Expense totals by how they were paid, added as decimals so the cents stay exact. */
public final class ExpenseTotals {

    private ExpenseTotals() {
    }

    /** Every payment type, in order, with its total (zero when there were none): CASH, GCASH, BANK_TRANSFER. */
    public static Map<String, BigDecimal> byType(List<Expense> expenses) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (ExpensePaymentType type : ExpensePaymentType.values()) {
            totals.put(type.name(), BigDecimal.ZERO.setScale(2));
        }
        for (Expense e : expenses) {
            if (e.getAmount() == null) {
                continue;
            }
            ExpensePaymentType type = e.getPaymentType() == null ? ExpensePaymentType.CASH : e.getPaymentType();
            totals.merge(type.name(), BigDecimal.valueOf(e.getAmount()), BigDecimal::add);
        }
        totals.replaceAll((type, total) -> total.setScale(2, RoundingMode.HALF_UP));
        return totals;
    }

    /** All expenses, every type. */
    public static BigDecimal total(List<Expense> expenses) {
        return byType(expenses).values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
