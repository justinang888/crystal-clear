package com.spring.aurora.sms;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Who is offered a text reminder: a customer whose last delivered order is more than their usual
 * order interval plus {@link #GRACE_DAYS} days ago. Customers who never had a delivery are left
 * out. A blank or zero interval counts as {@link #DEFAULT_INTERVAL_DAYS} days.
 */
public final class OverdueCustomers {

    public static final int GRACE_DAYS = 2;
    public static final int DEFAULT_INTERVAL_DAYS = 30;

    private OverdueCustomers() {
    }

    public static boolean isOverdue(LocalDate lastDelivered, int orderInterval, LocalDate today) {
        if (lastDelivered == null) {
            return false;
        }
        int interval = orderInterval > 0 ? orderInterval : DEFAULT_INTERVAL_DAYS;
        return daysSince(lastDelivered, today) > interval + GRACE_DAYS;
    }

    /** Calendar days from the last delivery to today, in shop time. */
    public static long daysSince(LocalDate lastDelivered, LocalDate today) {
        return ChronoUnit.DAYS.between(lastDelivered, today);
    }
}
