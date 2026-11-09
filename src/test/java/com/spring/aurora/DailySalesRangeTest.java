package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.spring.aurora.entity.DailySalesEntity;

/**
 * Daily sales for one day (?d=) or a range of days (?from=&to=, both included), and the number of
 * customers in the days shown. A single day is a one-day range, so both read the same way.
 */
class DailySalesRangeTest extends PostgresIntegrationTest {

    private long ana;
    private long ben;
    private long cora;

    @BeforeEach
    void setUp() {
        this.ana = insertCustomer("Ana");
        this.ben = insertCustomer("Ben");
        this.cora = insertCustomer("Cora");
        // 1 March: Ana's delivered order, Ben's pending one, a cash expense.
        order(this.ana, "Delivered", "2026-03-01 09:00:00", "165.00", "165.00");
        order(this.ben, "Pending", "2026-03-01 10:00:00", "55.00", "0.00");
        expense("2026-03-01 00:00:00", "100.00", "CASH");
        // 2 March: Ben's delivered order (partly paid), Cora pays by GCash late in the day, a GCash expense.
        order(this.ben, "Delivered", "2026-03-02 11:00:00", "220.00", "200.00");
        payment(this.cora, "2026-03-02 21:15:00", "300.00", "GCASH");
        expense("2026-03-02 17:00:00", "50.00", "GCASH");
        // 3 March: Ana returns containers without an order.
        this.jdbc.update("INSERT INTO container (customer_id, order_id, status, round_count, slim_count, created_at) "
                + "VALUES (?, 0, 'R', 2, 0, '2026-03-03 14:00:00')", this.ana);
        // Outside the range.
        order(this.cora, "Delivered", "2026-02-28 23:59:59", "999.00", "999.00");
        payment(this.ben, "2026-03-04 00:00:00", "999.00", "CASH");
    }

    private void order(long customer, String status, String at, String total, String paid) {
        this.jdbc.update("INSERT INTO orders (customer_id, delivery_receipt_num, status, total_amount, amount_paid, "
                + "round_refill_only_count, slim_refill_only_count, cont_round_returned, cont_slim_returned, "
                + "slim_free_count, round_free_count, created_at) VALUES (?, 'DR', ?, ?::numeric, ?::numeric, 3, 0, 1, 0, 0, 0, ?::timestamp)",
                customer, status, total, paid, at);
    }

    private void payment(long customer, String at, String amount, String type) {
        this.jdbc.update("INSERT INTO payment (customer_id, amount, wh_tax, payment_type, remarks, created_at) "
                + "VALUES (?, ?::numeric, 0, ?, 'p', ?::timestamp)", customer, amount, type, at);
    }

    private void expense(String at, String amount, String type) {
        this.jdbc.update("INSERT INTO expense (amount, description, created_at, payment_type) VALUES (?::numeric, 'e', ?::timestamp, ?)",
                amount, at, type);
    }

    private Map<String, Object> page(String... params) throws Exception {
        var request = get("/orders/daily");
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return this.mvc.perform(request).andReturn().getModelAndView().getModel();
    }

    @Test
    void aRangeAddsUpEveryDayInIt() throws Exception {
        Map<String, Object> m = page("from", "2026-03-01", "to", "2026-03-03");

        assertThat(m.get("totalCashPayments")).isEqualTo("365.00");    // 165 + 200 paid on delivered orders
        assertThat(m.get("totalCashExpenses")).isEqualTo("100.00");
        assertThat(m.get("netCash")).isEqualTo("265.00");
        assertThat(m.get("totalGcashPayments")).isEqualTo("300.00");
        assertThat(m.get("netGcash")).isEqualTo("250.00");
        assertThat(m.get("totalDebt")).isEqualTo("20.00");
        assertThat(m.get("totalRoundDelivered")).isEqualTo(6);
        assertThat(m.get("totalRoundReturned")).isEqualTo(4);           // 1 + 1 with orders, 2 on their own
        assertThat(m.get("from")).isEqualTo("2026-03-01");
        assertThat(m.get("to")).isEqualTo("2026-03-03");
        assertThat(m.get("datePicked")).isEqualTo("Mar 01 2026 – Mar 03 2026");
    }

    @Test
    void theCustomerCountFollowsTheDaysShown() throws Exception {
        Map<String, Object> range = page("from", "2026-03-01", "to", "2026-03-03");
        // Ana (delivery, return), Ben (delivery; his pending order doesn't count), Cora (payment).
        assertThat(range.get("totalCustomers")).isEqualTo(3);
        assertThat(range.get("customersServed")).as("with a delivered order").isEqualTo(2);

        Map<String, Object> firstDay = page("d", "2026-03-01");
        assertThat(firstDay.get("totalCustomers")).isEqualTo(1);       // only Ana: Ben's order is pending
        assertThat(firstDay.get("customersServed")).isEqualTo(1);

        Map<String, Object> thirdDay = page("d", "2026-03-03");
        assertThat(thirdDay.get("totalCustomers")).isEqualTo(1);       // Ana's container return
        assertThat(thirdDay.get("customersServed")).isEqualTo(0);
    }

    @Test
    void paymentsAndExpensesCountWhateverTheirTimeOfDay() throws Exception {
        Map<String, Object> secondDay = page("d", "2026-03-02");

        assertThat(secondDay.get("totalGcashPayments")).isEqualTo("300.00");   // paid at 21:15
        assertThat(secondDay.get("totalGcashExpenses")).isEqualTo("50.00");    // spent at 17:00
        assertThat(secondDay.get("from")).isEqualTo("2026-03-02");
        assertThat(secondDay.get("datePicked")).isEqualTo("Mar 02 2026");
    }

    @Test
    void aRangeGivenBackwardsIsTurnedAround() throws Exception {
        Map<String, Object> m = page("from", "2026-03-03", "to", "2026-03-01");

        assertThat(m.get("from")).isEqualTo("2026-03-01");
        assertThat(m.get("netCash")).isEqualTo("265.00");
    }

    @Test
    void rowsInARangeSayWhichDayTheyAreFrom() throws Exception {
        @SuppressWarnings("unchecked")
        List<DailySalesEntity> rows = (List<DailySalesEntity>) page("from", "2026-03-01", "to", "2026-03-03").get("dailySales");

        assertThat(rows).filteredOn(r -> r.getOrder() != null).extracting(DailySalesEntity::getDateAndTime)
                .containsExactlyInAnyOrder("Mar 1, 9:00 AM", "Mar 2, 11:00 AM");
    }
}
