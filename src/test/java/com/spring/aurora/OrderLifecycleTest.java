package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Order create / deliver / edit / cancel, as submitted by the new-order and edit-order forms.
 * Each test checks the rows the application leaves behind: orders, order lines, debts,
 * the per-customer AR summary, container activity and the customer's container balance.
 */
class OrderLifecycleTest extends PostgresIntegrationTest {

    private long customerId;

    @BeforeEach
    void setUp() {
        insertProducts();
        this.customerId = insertCustomer("Test Customer");
    }

    /** 3 round refills (product 1) at 55.00, 1 round container returned. */
    private MockHttpServletRequestBuilder threeRoundRefills(String amountPaid) {
        return post("/orders/save")
                .param("order.customerId", String.valueOf(this.customerId))
                .param("order.deliveryReceiptNum", "DR-1")
                .param("order.roundRefillOnlyCount", "3")
                .param("order.roundReturned", "1")
                .param("order.slimReturned", "")
                .param("order.totalAmount", "165.00")
                .param("order.amountPaid", amountPaid)
                .param("order.remarks", "test order")
                .param("orderInterval", "5")
                .param("opList[0].productId", "1").param("opList[0].quantity", "3")
                .param("opList[1].productId", "2").param("opList[1].quantity", "");
    }

    private long createOrder(String amountPaid) throws Exception {
        this.mvc.perform(threeRoundRefills(amountPaid)).andExpect(redirectedUrl("/orders/list"));
        return this.jdbc.queryForObject("SELECT max(order_id) FROM orders", Long.class);
    }

    private void deliver(long orderId) throws Exception {
        this.mvc.perform(post("/orders/deliver").param("orderId", String.valueOf(orderId)))
                .andExpect(redirectedUrl("/orders/list"));
    }

    private Map<String, Object> order(long orderId) {
        return this.jdbc.queryForMap("SELECT * FROM orders WHERE order_id = ?", orderId);
    }

    private Map<String, Object> customer() {
        return this.jdbc.queryForMap("SELECT * FROM customer WHERE customer_id = ?", this.customerId);
    }

    private List<Map<String, Object>> debts() {
        return this.jdbc.queryForList("SELECT * FROM debt WHERE customer_id = ? ORDER BY debt_id", this.customerId);
    }

    private BigDecimal arSummary() {
        return this.jdbc.queryForObject("SELECT ar_amount FROM ar_summary WHERE customer_id = ?",
                BigDecimal.class, this.customerId);
    }

    private List<String> containerActivity() {
        return this.jdbc.queryForList("SELECT status || ' order=' || order_id || ' round=' || round_count "
                + "|| ' slim=' || slim_count FROM container WHERE customer_id = ? ORDER BY container_id",
                String.class, this.customerId);
    }

    @Test
    void newOrderIsPendingWithItsLinesAndUpdatesContainerBalance() throws Exception {
        long orderId = createOrder("0");

        Map<String, Object> order = order(orderId);
        assertThat(order.get("status")).isEqualTo("Pending");
        assertThat(order.get("total_amount")).isEqualTo(new BigDecimal("165.00"));
        assertThat(order.get("round_refill_only_count")).isEqualTo(3);
        assertThat(order.get("cont_round_returned")).isEqualTo(1);
        assertThat(order.get("cont_slim_returned")).as("blank returned count is stored as 0").isEqualTo(0);

        assertThat(this.jdbc.queryForList("SELECT product_id || 'x' || quantity FROM order_product "
                + "WHERE order_id = ?", String.class, orderId))
                .as("only lines with a quantity are saved").containsExactly("1x3");

        // Borrowed 3 refill containers, returned 1.
        assertThat(customer().get("total_round")).isEqualTo(2);
        assertThat(customer().get("order_interval")).isEqualTo(5);

        assertThat(debts()).as("no debt until delivery").isEmpty();
        assertThat(containerActivity()).as("no container activity until delivery").isEmpty();
    }

    @Test
    void deliveringAnUnpaidOrderRecordsDebtArAndContainerActivity() throws Exception {
        long orderId = createOrder("0");

        deliver(orderId);

        assertThat(order(orderId).get("status")).isEqualTo("Delivered");
        List<Map<String, Object>> debts = debts();
        assertThat(debts).hasSize(1);
        assertThat(debts.get(0).get("amount")).isEqualTo(new BigDecimal("165.00"));
        assertThat(debts.get(0).get("linked_order_id")).isEqualTo((int) orderId);
        assertThat(debts.get(0).get("remarks"))
                .isEqualTo("Total amount is: Php165.0 but the customer hasn't paid yet. PreviousOrderId: " + orderId);
        assertThat(arSummary()).isEqualByComparingTo("165.00");

        assertThat(containerActivity()).containsExactly(
                "B order=" + orderId + " round=3 slim=0",
                "BC order=" + orderId + " round=0 slim=0",
                "RO order=" + orderId + " round=1 slim=0");
        assertThat(customer().get("total_round")).as("delivery does not change the balance again").isEqualTo(2);
    }

    @Test
    void deliveringAPartlyPaidOrderRecordsTheShortfall() throws Exception {
        long orderId = createOrder("100.00");

        deliver(orderId);

        assertThat(debts()).extracting(d -> d.get("amount")).containsExactly(new BigDecimal("65.00"));
        assertThat(debts().get(0).get("remarks"))
                .isEqualTo("Total amount is: Php165.0 but the amount paid is only Php100.0. PreviousOrderId: " + orderId);
        assertThat(arSummary()).isEqualByComparingTo("65.00");
    }

    @Test
    void deliveringAPaidOrderRecordsAZeroDebt() throws Exception {
        long orderId = createOrder("165.00");

        deliver(orderId);

        assertThat(debts()).extracting(d -> d.get("amount")).containsExactly(new BigDecimal("0.00"));
        assertThat(arSummary()).isEqualByComparingTo("0.00");
    }

    @Test
    void editingADeliveredOrderReplacesItAndAdjustsDebtArAndBalance() throws Exception {
        long orderId = createOrder("0");
        deliver(orderId);
        long debtId = ((Number) debts().get(0).get("debt_id")).longValue();
        long lineId = this.jdbc.queryForObject("SELECT order_product_id FROM order_product WHERE order_id = ?",
                Long.class, orderId);
        String createdAt = order(orderId).get("created_at").toString();

        // Customer paid in full and returned a second container.
        this.mvc.perform(post("/orders/update")
                .param("order.orderId", String.valueOf(orderId))
                .param("order.customerId", String.valueOf(this.customerId))
                .param("order.deliveryReceiptNum", "DR-1")
                .param("order.status", "Delivered")
                .param("order.createdAt", createdAt)
                .param("order.roundRefillOnlyCount", "3")
                .param("order.roundReturned", "2")
                .param("order.slimReturned", "0")
                .param("order.totalAmount", "165.00")
                .param("order.amountPaid", "165.00")
                .param("order.remarks", "edited")
                .param("opList[0].orderProductId", String.valueOf(lineId))
                .param("opList[0].productId", "1").param("opList[0].quantity", "3")
                .param("opList[1].productId", "2").param("opList[1].quantity", ""))
                .andExpect(redirectedUrl("/orders/list"));

        // The edit deletes the order and inserts it again under a new id.
        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM orders WHERE order_id = ?", Integer.class, orderId))
                .isZero();
        long newOrderId = this.jdbc.queryForObject("SELECT max(order_id) FROM orders", Long.class);
        assertThat(newOrderId).isGreaterThan(orderId);
        Map<String, Object> edited = order(newOrderId);
        assertThat(edited.get("amount_paid")).isEqualTo(new BigDecimal("165.00"));
        assertThat(edited.get("cont_round_returned")).isEqualTo(2);
        assertThat(edited.get("remarks")).isEqualTo("edited");
        assertThat(edited.get("created_at").toString()).as("keeps the original order time").isEqualTo(createdAt);
        assertThat(this.jdbc.queryForList("SELECT product_id || 'x' || quantity FROM order_product "
                + "WHERE order_id = ?", String.class, newOrderId)).containsExactly("1x3");

        // The same debt row is reused for the new order and cleared.
        assertThat(debts()).hasSize(1);
        assertThat(((Number) debts().get(0).get("debt_id")).longValue()).isEqualTo(debtId);
        assertThat(debts().get(0).get("amount")).isEqualTo(new BigDecimal("0.00"));
        assertThat(debts().get(0).get("linked_order_id")).isEqualTo((int) newOrderId);
        assertThat(arSummary()).isEqualByComparingTo("0.00");

        // Borrowed 3, now returned 2.
        assertThat(customer().get("total_round")).isEqualTo(1);
    }

    /**
     * KNOWN BUG (pinned so the upgrade can't change it silently): editing an order is meant to
     * move its container activity to the new order id, but the "delete" passes new objects
     * without ids, which deletes nothing. The original rows stay and a second set is added.
     */
    @Test
    void editingAnOrderDuplicatesItsContainerActivity() throws Exception {
        long orderId = createOrder("0");
        deliver(orderId);
        String createdAt = order(orderId).get("created_at").toString();

        this.mvc.perform(post("/orders/update")
                .param("order.orderId", String.valueOf(orderId))
                .param("order.customerId", String.valueOf(this.customerId))
                .param("order.deliveryReceiptNum", "DR-1")
                .param("order.status", "Delivered")
                .param("order.createdAt", createdAt)
                .param("order.roundRefillOnlyCount", "3")
                .param("order.roundReturned", "1")
                .param("order.totalAmount", "165.00")
                .param("order.amountPaid", "0")
                .param("opList[0].productId", "1").param("opList[0].quantity", "3"))
                .andExpect(redirectedUrl("/orders/list"));
        long newOrderId = this.jdbc.queryForObject("SELECT max(order_id) FROM orders", Long.class);

        assertThat(containerActivity()).containsExactly(
                "B order=" + orderId + " round=3 slim=0",
                "BC order=" + orderId + " round=0 slim=0",
                "RO order=" + orderId + " round=1 slim=0",
                "B order=" + newOrderId + " round=3 slim=0",
                "BC order=" + newOrderId + " round=0 slim=0",
                "RO order=" + newOrderId + " round=1 slim=0");
    }

    /**
     * KNOWN BUG (pinned): cancelling marks the order cancelled but does not undo the container
     * balance that was added when the order was created.
     */
    @Test
    void cancellingAnOrderMarksItCancelledButKeepsTheContainerBalance() throws Exception {
        long orderId = createOrder("0");

        this.mvc.perform(post("/orders/cancel").param("orderId", String.valueOf(orderId)))
                .andExpect(redirectedUrl("/orders/list"));

        assertThat(order(orderId).get("status")).isEqualTo("Cancelled");
        assertThat(debts()).isEmpty();
        assertThat(customer().get("total_round")).isEqualTo(2);
    }
}
