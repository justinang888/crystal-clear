package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.spring.aurora.entity.DailySalesEntity;

/**
 * The daily report's money by payment method: what came in, what went out and the net, for
 * Cash, GCash, Bank Transfer and Check. Customer payments can be any of the four; expenses Cash,
 * GCash or Bank Transfer; an order's amount paid on delivery is cash.
 */
class DailyNetsTest extends PostgresIntegrationTest {

    private long customerId;

    @BeforeEach
    void setUp() {
        this.customerId = insertCustomer("Payer");
    }

    private void payment(String amount, String type) throws Exception {
        this.mvc.perform(post("/payments/save").param("customerId", String.valueOf(this.customerId))
                .param("amount", amount).param("withholdingTax", "0").param("paymentType", type)
                .param("remarks", type).param("createdAt", "2026-03-15"))
                .andExpect(redirectedUrl("/payments/list?cid=" + this.customerId));
    }

    private void expense(String amount, String type) {
        this.jdbc.update("INSERT INTO expense (amount, description, created_at, payment_type) VALUES (?::numeric, ?, '2026-03-15', ?)",
                amount, type, type);
    }

    private Map<String, Object> daily() throws Exception {
        return this.mvc.perform(get("/orders/daily").param("d", "2026-03-15")).andReturn().getModelAndView().getModel();
    }

    @Test
    void customerPaymentsCanBeGcashOrBankTransfer() throws Exception {
        payment("150.00", "GCASH");
        payment("2000.00", "BANK_TRANSFER");

        assertThat(this.jdbc.queryForList("SELECT payment_type FROM payment ORDER BY payment_id", String.class))
                .containsExactly("GCASH", "BANK_TRANSFER");
    }

    @Test
    void anUnknownPaymentTypeIsRefused() throws Exception {
        this.mvc.perform(post("/payments/save").param("customerId", String.valueOf(this.customerId))
                .param("amount", "10.00").param("withholdingTax", "0").param("paymentType", "BITCOIN")
                .param("remarks", "x").param("createdAt", "2026-03-15"));

        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM payment", Integer.class)).isZero();
    }

    @Test
    void eachPaymentMethodHasItsOwnMoneyInExpensesAndNet() throws Exception {
        // An order paid on delivery: its amount paid is cash.
        this.jdbc.update("INSERT INTO orders (customer_id, delivery_receipt_num, status, total_amount, amount_paid, "
                + "round_refill_only_count, slim_refill_only_count, cont_round_returned, cont_slim_returned, "
                + "slim_free_count, round_free_count, created_at) VALUES (?, 'DR1', 'Delivered', 165, 165, 3, 0, 0, 0, 0, 0, "
                + "'2026-03-15 10:00:00')", this.customerId);
        payment("200.00", "CASH");
        payment("150.00", "GCASH");
        payment("2000.00", "BANK_TRANSFER");
        payment("1000.00", "CHECK");
        expense("100.00", "CASH");
        expense("480.00", "GCASH");
        expense("2500.00", "BANK_TRANSFER");

        Map<String, Object> m = daily();

        assertThat(m.get("totalCashPayments")).isEqualTo("365.00");   // 165 on the order + 200 paid
        assertThat(m.get("totalCashExpenses")).isEqualTo("100.00");
        assertThat(m.get("netCash")).isEqualTo("265.00");

        assertThat(m.get("totalGcashPayments")).isEqualTo("150.00");
        assertThat(m.get("totalGcashExpenses")).isEqualTo("480.00");
        assertThat(m.get("netGcash")).isEqualTo("-330.00");

        assertThat(m.get("totalBankTransferPayments")).isEqualTo("2000.00");
        assertThat(m.get("totalBankTransferExpenses")).isEqualTo("2500.00");
        assertThat(m.get("netBankTransfer")).isEqualTo("-500.00");

        // Checks are their own method now: GCash and bank transfers no longer count as checks.
        assertThat(m.get("totalCheckPayments")).isEqualTo("1000.00");
        assertThat(m.get("totalExpenses")).isEqualTo("3080.00");
    }

    @Test
    void eachPaymentRowSaysWhichColumnItsMoneyIsIn() throws Exception {
        payment("150.00", "GCASH");
        payment("2000.00", "BANK_TRANSFER");
        payment("1000.00", "CHECK");

        @SuppressWarnings("unchecked")
        List<DailySalesEntity> rows = (List<DailySalesEntity>) daily().get("dailySales");

        assertThat(rows).extracting(DailySalesEntity::getRemarks, DailySalesEntity::getPaidGcash,
                DailySalesEntity::getPaidBankTransfer, DailySalesEntity::getPaidCheck, DailySalesEntity::getPaidCash)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("GCASH", 150.0, null, null, null),
                        org.assertj.core.groups.Tuple.tuple("BANK_TRANSFER", null, 2000.0, null, null),
                        org.assertj.core.groups.Tuple.tuple("CHECK", null, null, 1000.0, null));
    }
}
