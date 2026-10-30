package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.spring.aurora.dao.CustomerDao;
import com.spring.aurora.dao.OrderDao;
import com.spring.aurora.entity.CustomerWithOrder;

/**
 * Daily sales and monthly totals for 15 March 2026, plus the date queries behind them.
 * The data includes orders at 23:59:59 on the 15th and 00:00:00 on the 16th, so the
 * day boundary is checked as well as the arithmetic.
 */
class SalesReportsTest extends PostgresIntegrationTest {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private CustomerDao customerDao;

    private long customerId;
    private long otherCustomerId;

    @BeforeEach
    void setUp() {
        this.customerId = insertCustomer("Day Customer");
        this.otherCustomerId = insertCustomer("Quiet Customer");

        // 15 March: two delivered orders, one pending order.
        insertOrder("Delivered", "2026-03-15 09:00:00", "165.00", "100.00", 3, 0, 1, 0);
        insertOrder("Delivered", "2026-03-15 23:59:59", "60.00", "60.00", 0, 1, 0, 0);
        insertOrder("Pending", "2026-03-15 10:00:00", "999.00", "999.00", 0, 0, 0, 0);
        // 16 March, first second of the day: not part of the 15th.
        insertOrder("Delivered", "2026-03-16 00:00:00", "500.00", "500.00", 2, 0, 0, 0);

        // Expenses and payments are stored at midnight of their day.
        this.jdbc.update("INSERT INTO expense (amount, description, created_at) VALUES "
                + "(25.50, 'Gasoline', '2026-03-15'), (10.00, 'Ice', '2026-03-16')");
        this.jdbc.update("INSERT INTO payment (customer_id, amount, wh_tax, payment_type, remarks, created_at) VALUES "
                + "(?, 200.00, 0.00, 'CASH', 'cash', '2026-03-15'), (?, 1000.00, 20.00, 'CHECK', 'check', '2026-03-15')",
                this.customerId, this.customerId);
        // Containers returned without an order.
        this.jdbc.update("INSERT INTO container (customer_id, order_id, status, round_count, slim_count, created_at) "
                + "VALUES (?, 0, 'R', 2, 1, '2026-03-15 14:00:00')", this.customerId);
    }

    private void insertOrder(String status, String createdAt, String total, String paid,
            int roundRefills, int slimRefills, int roundReturned, int slimReturned) {
        this.jdbc.update("INSERT INTO orders (customer_id, delivery_receipt_num, status, total_amount, amount_paid, "
                + "round_refill_only_count, slim_refill_only_count, cont_round_returned, cont_slim_returned, "
                + "slim_free_count, round_free_count, created_at) VALUES (?, 'DR', ?, ?::numeric, ?::numeric, ?, ?, ?, ?, 0, 0, ?::timestamp)",
                this.customerId, status, total, paid, roundRefills, slimRefills, roundReturned, slimReturned, createdAt);
    }

    private Map<String, Object> model(String url) throws Exception {
        return this.mvc.perform(get(url)).andReturn().getModelAndView().getModel();
    }

    @Test
    void dailySalesTotalsDeliveredOrdersPaymentsExpensesAndReturns() throws Exception {
        Map<String, Object> model = model("/orders/daily?d=2026-03-15");

        // Cash = paid on delivered orders (100 + 60) + cash payments (200).
        assertThat(model.get("totalCashPayments")).isEqualTo("360.00");
        assertThat(model.get("totalCheckPayments")).isEqualTo("1000.00");
        assertThat(model.get("totalExpenses")).isEqualTo("25.50");
        assertThat(model.get("totalDebt")).isEqualTo("65.00");
        assertThat(model.get("netCash")).isEqualTo("334.50");

        assertThat(model.get("totalRoundDelivered")).isEqualTo(3);
        assertThat(model.get("totalSlimDelivered")).isEqualTo(1);
        // Returned with orders (1 round) plus returned on their own (2 round, 1 slim).
        assertThat(model.get("totalRoundReturned")).isEqualTo(3);
        assertThat(model.get("totalSlimReturned")).isEqualTo(1);

        // 2 delivered orders + 1 expense + 2 payments + 1 container return.
        assertThat((List<?>) model.get("dailySales")).hasSize(6);
        assertThat(model.get("datePicked")).isEqualTo("Mar 15 2026");
    }

    @Test
    void monthlyTotalsAddUpEveryOrderInTheMonth() throws Exception {
        Map<String, Object> model = model("/orders/monthly?d=03-2026");

        assertThat((List<?>) model.get("dailyTotals")).hasSize(31);
        // Monthly totals include pending orders, unlike the daily report.
        assertThat(model.get("grandTotalCash")).isEqualTo(1659.0);
        assertThat(model.get("grandTotalAr")).isEqualTo(65.0);
        assertThat(model.get("grandTotalPayment")).isEqualTo(1200.0);
        assertThat(model.get("grandTotalExpense")).isEqualTo(35.5);
        assertThat(model.get("grandTotalRound")).isEqualTo(5);
        assertThat(model.get("grandTotalSlim")).isEqualTo(1);
        assertThat(model.get("grandTotalContainers")).isEqualTo(6);
        assertThat(model.get("monthYear")).isEqualTo("Mar 2026");
    }

    @Test
    void monthlyTotalsKnowFebruaryLengths() throws Exception {
        assertThat((List<?>) model("/orders/monthly?d=02-2026").get("dailyTotals")).hasSize(28);
        assertThat((List<?>) model("/orders/monthly?d=02-2028").get("dailyTotals")).hasSize(29);
    }

    @Test
    void ordersForADayIncludeTheLastSecondButNotTheNextDay() {
        assertThat(this.orderDao.findAllOrdersToday(Date.valueOf("2026-03-15")))
                .extracting(o -> o.getCreatedAt().toString())
                .containsExactlyInAnyOrder("2026-03-15 09:00:00.0", "2026-03-15 23:59:59.0", "2026-03-15 10:00:00.0");
        assertThat(this.orderDao.findAllOrdersToday(Date.valueOf("2026-03-16"))).hasSize(1);
    }

    @Test
    void monthAndYearQueriesFindTheRightOrders() {
        assertThat(this.orderDao.findAllOrdersPerMonth(3, 2026)).hasSize(4);
        assertThat(this.orderDao.findAllOrdersPerMonth(3, 2025)).isEmpty();
        assertThat(this.orderDao.getOrderCountPerMonth(3, 2026, this.customerId)).isEqualTo(4);
        assertThat(this.orderDao.getOrderCountPerMonth(3, 2026, this.otherCustomerId)).isZero();

        List<CustomerWithOrder> active = this.customerDao.findMonthlyActiveCustomers(3, 2026);
        assertThat(active).hasSize(4).allSatisfy(c -> assertThat(c.getCustomerId()).isEqualTo(this.customerId));
    }

    @Test
    void mostRecentOrderAndInactiveCustomerQueries() {
        assertThat(this.orderDao.getMostRecentOrderDate(this.customerId))
                .isEqualTo(Timestamp.valueOf("2026-03-16 00:00:00"));
        assertThat(this.orderDao.getMostRecentOrderDate(this.otherCustomerId)).isNull();

        assertThat(this.orderDao.inactiveCustomerIds(Date.valueOf("2026-03-15"))).containsOnly(this.customerId);
        assertThat(this.orderDao.inactiveCustomerIds(Date.valueOf("2026-03-17"))).isEmpty();
    }
}
