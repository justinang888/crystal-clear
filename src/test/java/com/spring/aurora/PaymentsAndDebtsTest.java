package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Payments, manually logged debts and returned containers: the other ways a customer's
 * AR summary and container balance change outside of orders.
 */
class PaymentsAndDebtsTest extends PostgresIntegrationTest {

    private long customerId;

    @BeforeEach
    void setUp() {
        this.customerId = insertCustomer("Test Customer");
    }

    private BigDecimal arSummary() {
        return this.jdbc.queryForObject("SELECT ar_amount FROM ar_summary WHERE customer_id = ?",
                BigDecimal.class, this.customerId);
    }

    private void logDebt(String amount) throws Exception {
        this.mvc.perform(post("/debts/save")
                .param("customerId", String.valueOf(this.customerId))
                .param("amount", amount)
                .param("remarks", "Beginning balance")
                .param("createdAt", "2026-03-01"))
                .andExpect(redirectedUrl("/debts/list?cid=" + this.customerId));
    }

    @Test
    void loggingADebtAddsToTheArSummary() throws Exception {
        logDebt("800.00");
        logDebt("35.50");

        assertThat(this.jdbc.queryForList("SELECT amount FROM debt WHERE customer_id = ? ORDER BY debt_id",
                BigDecimal.class, this.customerId)).containsExactly(new BigDecimal("800.00"), new BigDecimal("35.50"));
        assertThat(arSummary()).isEqualByComparingTo("835.50");
    }

    @Test
    void aPaymentAndItsWithholdingTaxBothReduceTheArSummary() throws Exception {
        logDebt("800.00");

        this.mvc.perform(post("/payments/save")
                .param("customerId", String.valueOf(this.customerId))
                .param("amount", "500.25")
                .param("withholdingTax", "1.50")
                .param("paymentType", "CHECK")
                .param("remarks", "BDO 1234")
                .param("createdAt", "2026-03-15"))
                .andExpect(redirectedUrl("/payments/list?cid=" + this.customerId));

        Map<String, Object> payment = this.jdbc.queryForMap("SELECT * FROM payment WHERE customer_id = ?",
                this.customerId);
        assertThat(payment.get("amount")).isEqualTo(new BigDecimal("500.25"));
        assertThat(payment.get("wh_tax")).isEqualTo(new BigDecimal("1.50"));
        assertThat(payment.get("payment_type")).isEqualTo("CHECK");
        assertThat(payment.get("created_at").toString()).isEqualTo("2026-03-15 00:00:00.0");
        assertThat(arSummary()).isEqualByComparingTo("298.25");
    }

    @Test
    void aPaymentWithoutAnAmountIsRejected() throws Exception {
        this.mvc.perform(post("/payments/save")
                .param("customerId", String.valueOf(this.customerId))
                .param("amount", "0")
                .param("withholdingTax", "0")
                .param("paymentType", "CASH")
                .param("remarks", "nothing")
                .param("createdAt", "2026-03-15"))
                .andExpect(view().name("new-payment"));

        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM payment", Integer.class)).isZero();
    }

    @Test
    void customerPaymentsListTotalsAmountPlusWithholdingTax() throws Exception {
        this.jdbc.update("INSERT INTO payment (customer_id, amount, wh_tax, payment_type, remarks, created_at) VALUES "
                + "(?, 100.00, 2.00, 'CASH', 'a', '2026-03-01'), (?, 50.50, 0.00, 'CASH', 'b', '2026-03-02')",
                this.customerId, this.customerId);

        Object total = this.mvc.perform(get("/payments/list").param("cid", String.valueOf(this.customerId)))
                .andReturn().getModelAndView().getModel().get("paymentsTotal");

        assertThat(total).isEqualTo(152.5);
    }

    @Test
    void returningContainersLogsActivityAndLowersTheBalance() throws Exception {
        this.jdbc.update("UPDATE customer SET total_round = 5, total_slim = 3 WHERE customer_id = ?", this.customerId);

        this.mvc.perform(post("/container/save")
                .param("customerId", String.valueOf(this.customerId))
                .param("status", "R")
                .param("roundCount", "2")
                .param("slimCount", "1"))
                .andExpect(redirectedUrl("/customers/view?customerId=" + this.customerId));

        Map<String, Object> activity = this.jdbc.queryForMap("SELECT * FROM container WHERE customer_id = ?",
                this.customerId);
        assertThat(activity.get("status")).isEqualTo("R");
        assertThat(activity.get("order_id")).isEqualTo(0);
        assertThat(activity.get("round_count")).isEqualTo(2);
        assertThat(activity.get("slim_count")).isEqualTo(1);

        Map<String, Object> customer = this.jdbc.queryForMap("SELECT * FROM customer WHERE customer_id = ?",
                this.customerId);
        assertThat(customer.get("total_round")).isEqualTo(3);
        assertThat(customer.get("total_slim")).isEqualTo(2);
    }
}
