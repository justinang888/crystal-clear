package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.spring.aurora.entity.SalesPeriod;

/** The dashboard's sales chart reads delivered orders from the database, up to today. */
class DashboardSalesTest extends PostgresIntegrationTest {

    private long customerId;

    @BeforeEach
    void setUp() {
        this.customerId = insertCustomer("Chart Customer");
    }

    private void insertOrder(String status, LocalDate day, String total) {
        this.jdbc.update("INSERT INTO orders (customer_id, delivery_receipt_num, status, total_amount, amount_paid, "
                + "round_refill_only_count, slim_refill_only_count, cont_round_returned, cont_slim_returned, "
                + "slim_free_count, round_free_count, created_at) VALUES (?, 'DR', ?, ?::numeric, 0, 0, 0, 0, 0, 0, 0, ?)",
                this.customerId, status, total, Timestamp.valueOf(day.atTime(12, 0)));
    }

    @SuppressWarnings("unchecked")
    private List<SalesPeriod> series(String name) throws Exception {
        Map<String, Object> model = this.mvc.perform(get("/dashboard")).andReturn().getModelAndView().getModel();
        return (List<SalesPeriod>) model.get(name);
    }

    @Test
    void theDashboardSendsSalesByDayWeekAndMonth() throws Exception {
        LocalDate today = LocalDate.now();
        insertOrder("Delivered", today, "120.00");
        insertOrder("Delivered", today, "0.50");
        insertOrder("Pending", today, "999.00");
        insertOrder("Delivered", today.minusDays(1), "30.00");

        List<SalesPeriod> days = series("salesByDay");
        assertThat(days).hasSize(7);
        assertThat(days.get(6).start()).isEqualTo(today);
        assertThat(days.get(6).total()).isEqualTo(new BigDecimal("120.50"));
        assertThat(days.get(5).total()).isEqualTo(new BigDecimal("30.00"));

        assertThat(series("salesByWeek")).hasSize(8);
        List<SalesPeriod> months = series("salesByMonth");
        assertThat(months).hasSize(12);
        assertThat(months.get(11).start()).isEqualTo(today.withDayOfMonth(1));
    }
}
