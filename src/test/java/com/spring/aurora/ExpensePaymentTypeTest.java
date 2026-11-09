package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Expenses record how they were paid (Cash, GCash, Bank Transfer); only cash comes out of the
 * drawer, so only cash expenses lower the daily report's net cash.
 * See docs/specs/owner-role-bank-reconciliation.md.
 */
class ExpensePaymentTypeTest extends PostgresIntegrationTest {

    private String paymentType(String description) {
        return this.jdbc.queryForObject("SELECT payment_type FROM expense WHERE description = ?", String.class, description);
    }

    private void saveExpense(String description, String amount, String paymentType) throws Exception {
        var request = post("/expenses/save").param("description", description).param("amount", amount)
                .param("createdAt", "2026-03-15");
        if (paymentType != null) {
            request.param("paymentType", paymentType);
        }
        this.mvc.perform(request).andExpect(redirectedUrl("/expenses/list"));
    }

    @Test
    void theFormSavesHowTheExpenseWasPaid() throws Exception {
        saveExpense("Gasoline", "500.00", "CASH");
        saveExpense("Filter cartridges", "1200.00", "GCASH");
        saveExpense("Water test lab", "2500.00", "BANK_TRANSFER");

        assertThat(paymentType("Gasoline")).isEqualTo("CASH");
        assertThat(paymentType("Filter cartridges")).isEqualTo("GCASH");
        assertThat(paymentType("Water test lab")).isEqualTo("BANK_TRANSFER");
    }

    @Test
    void anExpenseWithoutAPaymentTypeIsCash() throws Exception {
        saveExpense("Ice", "50.00", null);
        assertThat(paymentType("Ice")).isEqualTo("CASH");

        // Rows written before the column existed, or by the old WAR, read as Cash too.
        this.jdbc.update("INSERT INTO expense (amount, description, created_at) VALUES (20.00, 'Tape', '2026-03-15')");
        assertThat(paymentType("Tape")).isEqualTo("CASH");
    }

    @Test
    void anUnknownPaymentTypeIsRefused() throws Exception {
        this.mvc.perform(post("/expenses/save").param("description", "Odd").param("amount", "10.00")
                .param("createdAt", "2026-03-15").param("paymentType", "CHEQUE"));

        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM expense", Integer.class)).isZero();
    }

    @Test
    void onlyCashExpensesLowerTheDaysNetCash() throws Exception {
        this.jdbc.update("INSERT INTO payment (customer_id, amount, wh_tax, payment_type, remarks, created_at) "
                + "VALUES (?, 1000.00, 0, 'CASH', 'cash', '2026-03-15')", insertCustomer("Payer"));
        saveExpense("Gasoline", "100.00", "CASH");
        saveExpense("Filter cartridges", "300.00", "GCASH");
        saveExpense("Water test lab", "250.50", "BANK_TRANSFER");

        Map<String, Object> model = this.mvc.perform(get("/orders/daily").param("d", "2026-03-15"))
                .andReturn().getModelAndView().getModel();

        assertThat(model.get("totalCashPayments")).isEqualTo("1000.00");
        assertThat(model.get("totalExpenses")).as("every expense").isEqualTo("650.50");
        assertThat(model.get("totalCashExpenses")).isEqualTo("100.00");
        assertThat(model.get("totalOtherExpenses")).as("GCash and bank transfers").isEqualTo("550.50");
        assertThat(model.get("netCash")).isEqualTo("900.00");
    }

    @Test
    void theExpensesListShowsEachTypeAndTheDaysTotalsByType() throws Exception {
        saveExpense("Gasoline", "100.00", "CASH");
        saveExpense("Ice", "50.00", "CASH");
        saveExpense("Filter cartridges", "300.00", "GCASH");

        Map<String, Object> model = this.mvc.perform(get("/expenses/list").param("d", "2026-03-15"))
                .andReturn().getModelAndView().getModel();

        assertThat((List<?>) model.get("expenses")).hasSize(3);
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> byType = (Map<String, BigDecimal>) model.get("totalsByType");
        assertThat(byType).containsOnlyKeys("CASH", "GCASH", "BANK_TRANSFER");
        assertThat(byType.get("CASH")).isEqualByComparingTo("150.00");
        assertThat(byType.get("GCASH")).isEqualByComparingTo("300.00");
        assertThat(byType.get("BANK_TRANSFER")).isEqualByComparingTo("0");
    }
}
