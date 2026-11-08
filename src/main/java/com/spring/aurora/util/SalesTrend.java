package com.spring.aurora.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.spring.aurora.entity.SalesPeriod;
import com.spring.aurora.model.Order;

/**
 * Groups delivered orders into the dashboard's sales chart. A sale is a delivered order's total
 * amount (paid now or owed), the same orders the daily sales report counts. Each series ends with
 * the current day, week or month so far. Weeks run Monday to Sunday.
 */
public final class SalesTrend {

    public static final int DAYS = 7;
    public static final int WEEKS = 8;
    public static final int MONTHS = 12;

    private SalesTrend() {
    }

    /** The first day any series needs: the first of the oldest month shown. */
    public static LocalDate earliestDay(LocalDate today) {
        return today.withDayOfMonth(1).minusMonths(MONTHS - 1);
    }

    public static List<SalesPeriod> byDay(List<Order> orders, LocalDate today) {
        return periods(orders, today.minusDays(DAYS - 1), DAYS, d -> d.plusDays(1));
    }

    public static List<SalesPeriod> byWeek(List<Order> orders, LocalDate today) {
        LocalDate thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return periods(orders, thisMonday.minusWeeks(WEEKS - 1), WEEKS, d -> d.plusWeeks(1));
    }

    public static List<SalesPeriod> byMonth(List<Order> orders, LocalDate today) {
        return periods(orders, earliestDay(today), MONTHS, d -> d.plusMonths(1));
    }

    private static List<SalesPeriod> periods(List<Order> orders, LocalDate first, int count, UnaryOperator<LocalDate> next) {
        List<SalesPeriod> result = new ArrayList<>(count);
        LocalDate start = first;
        for (int i = 0; i < count; i++) {
            LocalDate nextStart = next.apply(start);
            result.add(new SalesPeriod(start, nextStart.minus(1, ChronoUnit.DAYS), total(orders, start, nextStart)));
            start = nextStart;
        }
        return result;
    }

    private static BigDecimal total(List<Order> orders, LocalDate from, LocalDate until) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Order order : orders) {
            if (!"Delivered".equalsIgnoreCase(order.getStatus()) || order.getCreatedAt() == null || order.getTotalAmount() == null) {
                continue;
            }
            LocalDate day = order.getCreatedAt().toLocalDateTime().toLocalDate();
            if (!day.isBefore(from) && day.isBefore(until)) {
                // Amounts are stored with two decimals; adding them as decimals keeps the cents exact.
                sum = sum.add(BigDecimal.valueOf(order.getTotalAmount()));
            }
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }
}
