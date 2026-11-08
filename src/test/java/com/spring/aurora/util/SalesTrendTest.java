package com.spring.aurora.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.spring.aurora.entity.SalesPeriod;
import com.spring.aurora.model.Order;

/**
 * The dashboard's sales chart: delivered orders' totals grouped by day, by week (Monday to
 * Sunday) and by month, ending with the current period so far.
 */
class SalesTrendTest {

    /** A Sunday, so the current week is Monday 2 to Sunday 8 November. */
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 8);

    private static Order delivered(String createdAt, double total) {
        Order order = new Order();
        order.setStatus("Delivered");
        order.setCreatedAt(Timestamp.valueOf(createdAt));
        order.setTotalAmount(total);
        return order;
    }

    private static final List<Order> ORDERS = List.of(
            delivered("2026-11-08 23:59:59", 55.10),   // today, last second
            delivered("2026-11-08 08:00:00", 60.20),   // today
            delivered("2026-11-02 00:00:00", 100.00),  // Monday: first day of this week
            delivered("2026-11-01 12:00:00", 40.00),   // Sunday: last week, but this month
            delivered("2026-10-27 09:00:00", 25.00),   // last week, last month
            delivered("2026-09-14 09:00:00", 500.00),  // two months ago
            delivered("2025-12-01 00:00:00", 7.00),    // first day of the oldest month shown
            delivered("2025-11-30 23:59:59", 999.00)); // the day before: outside every series

    private static BigDecimal pesos(String amount) {
        return new BigDecimal(amount);
    }

    @Test
    void byDayIsTheLastSevenDaysEndingToday() {
        List<SalesPeriod> days = SalesTrend.byDay(ORDERS, TODAY);

        assertThat(days).extracting(SalesPeriod::start).containsExactly(
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 4),
                LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 7), TODAY);
        assertThat(days).allMatch(d -> d.start().equals(d.end()));
        // 55.10 + 60.20 is 115.30000000000001 as doubles; the chart must show 115.30.
        assertThat(days.get(6).total()).isEqualTo(pesos("115.30"));
        assertThat(days.get(0).total()).isEqualTo(pesos("100.00"));
        assertThat(days.get(1).total()).isEqualTo(pesos("0.00"));
    }

    @Test
    void byWeekIsTheLastEightMondayToSundayWeeks() {
        List<SalesPeriod> weeks = SalesTrend.byWeek(ORDERS, TODAY);

        assertThat(weeks).hasSize(8);
        assertThat(weeks.get(7).start()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(weeks.get(7).end()).isEqualTo(TODAY);
        assertThat(weeks.get(7).total()).isEqualTo(pesos("215.30"));
        assertThat(weeks.get(6).start()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(weeks.get(6).end()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(weeks.get(6).total()).isEqualTo(pesos("65.00"));
        assertThat(weeks.get(0).start()).isEqualTo(LocalDate.of(2026, 9, 14));
        assertThat(weeks.get(0).total()).isEqualTo(pesos("500.00"));
    }

    @Test
    void byMonthIsTheLastTwelveMonths() {
        List<SalesPeriod> months = SalesTrend.byMonth(ORDERS, TODAY);

        assertThat(months).hasSize(12);
        assertThat(months.get(11).start()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(months.get(11).end()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(months.get(11).total()).isEqualTo(pesos("255.30"));
        assertThat(months.get(10).total()).isEqualTo(pesos("25.00"));
        assertThat(months.get(9).start()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(months.get(9).total()).isEqualTo(pesos("500.00"));
        assertThat(months.get(0).start()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(months.get(0).total()).isEqualTo(pesos("7.00"));
    }

    @Test
    void theEarliestDayNeededIsTheFirstOfTheOldestMonth() {
        assertThat(SalesTrend.earliestDay(TODAY)).isEqualTo(LocalDate.of(2025, 12, 1));
    }

    @Test
    void onlyDeliveredOrdersCount() {
        Order pending = delivered("2026-11-08 10:00:00", 999.00);
        pending.setStatus("Pending");
        Order cancelled = delivered("2026-11-08 10:00:00", 999.00);
        cancelled.setStatus("Cancelled");

        List<SalesPeriod> days = SalesTrend.byDay(List.of(pending, cancelled), TODAY);

        assertThat(days.get(6).total()).isEqualTo(pesos("0.00"));
    }
}
