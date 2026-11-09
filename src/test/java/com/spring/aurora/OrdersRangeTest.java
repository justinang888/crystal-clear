package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.spring.aurora.entity.OrderCustomerEntity;

/** The Orders screen shows one day (?d=) or a range of days (?from=&to=, both included). */
class OrdersRangeTest extends PostgresIntegrationTest {

    private long ana;
    private long ben;

    @BeforeEach
    void setUp() {
        this.ana = insertCustomer("Ana");
        this.ben = insertCustomer("Ben");
        order(this.ana, "DR-0301", "Delivered", "2026-03-01 08:00:00");
        order(this.ben, "DR-0305", "Pending", "2026-03-05 23:59:59");   // late on the last day
        order(this.ana, "DR-0303", "Cancelled", "2026-03-03 12:00:00");
        order(this.ben, "DR-0228", "Delivered", "2026-02-28 23:59:59"); // the day before
        order(this.ana, "DR-0306", "Pending", "2026-03-06 00:00:00");   // the day after
    }

    private void order(long customer, String dr, String status, String at) {
        this.jdbc.update("INSERT INTO orders (customer_id, delivery_receipt_num, status, total_amount, amount_paid, "
                + "round_refill_only_count, slim_refill_only_count, cont_round_returned, cont_slim_returned, "
                + "slim_free_count, round_free_count, created_at) VALUES (?, ?, ?, 55, 55, 1, 0, 0, 0, 0, 0, ?::timestamp)",
                customer, dr, status, at);
    }

    private Map<String, Object> page(String... params) throws Exception {
        var request = get("/orders/list");
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return this.mvc.perform(request).andReturn().getModelAndView().getModel();
    }

    @SuppressWarnings("unchecked")
    private static List<String> receipts(Map<String, Object> model) {
        return ((List<OrderCustomerEntity>) model.get("orders")).stream()
                .map(o -> o.getOrder().getDeliveryReceiptNum()).sorted().toList();
    }

    @Test
    void aRangeShowsEveryOrderFromTheFirstDayToTheLast() throws Exception {
        Map<String, Object> model = page("from", "2026-03-01", "to", "2026-03-05");

        assertThat(receipts(model)).containsExactly("DR-0301", "DR-0303", "DR-0305");
        assertThat(model.get("pendingCount")).isEqualTo(1);
        assertThat(model.get("from")).isEqualTo("2026-03-01");
        assertThat(model.get("to")).isEqualTo("2026-03-05");
        assertThat(model.get("datePicked")).isEqualTo("Mar 01 2026 – Mar 05 2026");
    }

    @Test
    void eachOrderNamesItsCustomer() throws Exception {
        @SuppressWarnings("unchecked")
        List<OrderCustomerEntity> orders = (List<OrderCustomerEntity>) page("from", "2026-03-01", "to", "2026-03-05").get("orders");

        assertThat(orders).extracting(o -> o.getOrder().getDeliveryReceiptNum() + " " + o.getCustomerName())
                .containsExactlyInAnyOrder("DR-0301 Ana", "DR-0303 Ana", "DR-0305 Ben");
    }

    @Test
    void aRangeGivenBackwardsIsTurnedAround() throws Exception {
        Map<String, Object> model = page("from", "2026-03-05", "to", "2026-03-01");

        assertThat(receipts(model)).containsExactly("DR-0301", "DR-0303", "DR-0305");
        assertThat(model.get("from")).isEqualTo("2026-03-01");
    }

    @Test
    void aSingleDayStillWorksAsBefore() throws Exception {
        Map<String, Object> model = page("d", "2026-03-05");

        assertThat(receipts(model)).containsExactly("DR-0305");
        assertThat(model.get("from")).isEqualTo("2026-03-05");
        assertThat(model.get("to")).isEqualTo("2026-03-05");
        assertThat(model.get("datePicked")).isEqualTo("Mar 05 2026");
    }

    @Test
    void aOneDayRangeIsShownAsThatDay() throws Exception {
        Map<String, Object> model = page("from", "2026-03-06", "to", "2026-03-06");

        assertThat(receipts(model)).containsExactly("DR-0306");
        assertThat(model.get("datePicked")).isEqualTo("Mar 06 2026");
    }
}
