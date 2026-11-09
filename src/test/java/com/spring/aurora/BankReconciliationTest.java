package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * The owner login and the monthly bank reconciliation only it can see.
 * See docs/specs/owner-role-bank-reconciliation.md.
 */
class BankReconciliationTest extends PostgresIntegrationTest {

    private static MockHttpServletRequestBuilder ownerGet(String url) {
        return MockMvcRequestBuilders.get(url).with(user("boss").roles("OWNER"));
    }

    private static MockHttpServletRequestBuilder ownerPost(String url) {
        return MockMvcRequestBuilders.post(url).with(user("boss").roles("OWNER")).with(csrf());
    }

    private void expense(String day, String amount, String type) {
        this.jdbc.update("INSERT INTO expense (amount, description, created_at, payment_type) VALUES (?::numeric, 'x', ?::timestamp, ?)",
                amount, day, type);
    }

    private Map<String, Object> page(String month) throws Exception {
        return this.mvc.perform(ownerGet("/expenses/reconciliation").param("d", month)).andReturn().getModelAndView().getModel();
    }

    // --- Who may see it ---

    @Test
    void bookkeepersCantOpenOrSaveTheReconciliation() throws Exception {
        for (String role : new String[] {"ADMIN", "USER"}) {
            this.mvc.perform(MockMvcRequestBuilders.get("/expenses/reconciliation").with(user("clerk").roles(role))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
            this.mvc.perform(MockMvcRequestBuilders.post("/expenses/reconciliation/save").with(user("clerk").roles(role)).with(csrf())
                    .param("month", "2026-03").param("statementAmount", "1.00"))
                    .andExpect(status().isForbidden());
        }
        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM bank_reconciliation", Integer.class)).isZero();
    }

    @Test
    void theOwnerCanOpenItAndEveryOtherScreen() throws Exception {
        this.mvc.perform(ownerGet("/expenses/reconciliation").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        for (String url : new String[] {"/dashboard", "/customers/list", "/orders/daily?d=today", "/expenses/list?d=today"}) {
            this.mvc.perform(ownerGet(url).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        }
    }

    @Test
    void bookkeepersKeepEveryOtherScreen() throws Exception {
        for (String url : new String[] {"/dashboard", "/customers/list", "/orders/daily?d=today", "/expenses/list?d=today", "/expenses/new"}) {
            this.mvc.perform(get(url).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        }
    }

    @Test
    void theSessionSaysWhetherToShowReconciliation() throws Exception {
        this.mvc.perform(get("/session").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.canReconcile").value(false));
        this.mvc.perform(ownerGet("/session").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("boss"))
                .andExpect(jsonPath("$.canReconcile").value(true));
    }

    // --- The month's figures and the owner's entry ---

    @Test
    void aMonthWithoutAnEntryShowsItsExpensesByType() throws Exception {
        expense("2026-03-01 00:00:00", "100.00", "CASH");
        expense("2026-03-31 17:00:00", "50.25", "CASH");          // the last day, late in the day
        expense("2026-03-15 00:00:00", "300.00", "GCASH");
        expense("2026-03-20 00:00:00", "2500.00", "BANK_TRANSFER");
        expense("2026-04-01 00:00:00", "999.00", "BANK_TRANSFER"); // next month
        expense("2026-02-28 23:00:00", "999.00", "CASH");          // last month

        Map<String, Object> model = page("03-2026");

        assertThat(model.get("month")).isEqualTo(LocalDate.of(2026, 3, 1));
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> byType = (Map<String, BigDecimal>) model.get("totalsByType");
        assertThat(byType.get("CASH")).isEqualByComparingTo("150.25");
        assertThat(byType.get("GCASH")).isEqualByComparingTo("300.00");
        assertThat(byType.get("BANK_TRANSFER")).isEqualByComparingTo("2500.00");
        assertThat((BigDecimal) model.get("total")).isEqualByComparingTo("2950.25");
        assertThat(model.get("entry")).isNull();
        assertThat(model.get("difference")).isNull();
    }

    @Test
    void theOwnerSavesTheStatementAmountAndANoteAndSeesTheDifference() throws Exception {
        expense("2026-03-20 00:00:00", "2500.00", "BANK_TRANSFER");

        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("month", "2026-03")
                .param("statementAmount", "2650.00").param("note", "BDO statement; 150 bank charge not recorded"))
                .andExpect(redirectedUrl("/expenses/reconciliation?d=03-2026"))
                .andExpect(flash().attribute("msg", "Reconciliation for March 2026 saved."));

        Map<String, Object> model = page("03-2026");
        @SuppressWarnings("unchecked")
        Map<String, Object> entry = (Map<String, Object>) model.get("entry");
        assertThat((BigDecimal) entry.get("statementAmount")).isEqualByComparingTo("2650.00");
        assertThat(entry.get("note")).isEqualTo("BDO statement; 150 bank charge not recorded");
        assertThat(entry.get("updatedBy")).isEqualTo("boss");
        assertThat(entry.get("updatedAt")).isNotNull();
        // Statement minus the Bank Transfer expenses on record.
        assertThat((BigDecimal) model.get("difference")).isEqualByComparingTo("150.00");
    }

    @Test
    void savingAgainReplacesTheMonthsEntry() throws Exception {
        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("month", "2026-03").param("statementAmount", "100.00"));
        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("month", "2026-03").param("statementAmount", "120.00")
                .param("note", ""));

        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM bank_reconciliation", Integer.class)).isEqualTo(1);
        assertThat(this.jdbc.queryForObject("SELECT statement_amount FROM bank_reconciliation", BigDecimal.class))
                .isEqualByComparingTo("120.00");
        assertThat(this.jdbc.queryForObject("SELECT note FROM bank_reconciliation", String.class)).isNull();
    }

    @Test
    void aMissingNegativeOrMalformedEntryIsRefused() throws Exception {
        for (String amount : new String[] {"", "-5", "abc"}) {
            this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("month", "2026-03").param("statementAmount", amount))
                    .andExpect(redirectedUrl("/expenses/reconciliation?d=03-2026"))
                    .andExpect(flash().attributeExists("failmsg"));
        }
        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("month", "2026-03").param("statementAmount", "1")
                .param("note", "x".repeat(1001)))
                .andExpect(flash().attributeExists("failmsg"));
        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("month", "March").param("statementAmount", "1"))
                .andExpect(flash().attributeExists("failmsg"));
        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM bank_reconciliation", Integer.class)).isZero();
    }

    @Test
    void savingNeedsTheCsrfToken() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.post("/expenses/reconciliation/save").with(user("boss").roles("OWNER"))
                .param("month", "2026-03").param("statementAmount", "1.00"))
                .andExpect(status().isForbidden());
        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM bank_reconciliation", Integer.class)).isZero();
    }

    // --- By day ---

    private Map<String, Object> dayPage(String day) throws Exception {
        return this.mvc.perform(ownerGet("/expenses/reconciliation").param("day", day)).andReturn().getModelAndView().getModel();
    }

    @Test
    void aDayShowsOnlyThatDaysExpenses() throws Exception {
        expense("2026-03-15 00:00:00", "100.00", "CASH");
        expense("2026-03-15 17:30:00", "300.00", "GCASH");          // late in the day
        expense("2026-03-15 09:00:00", "2500.00", "BANK_TRANSFER");
        expense("2026-03-14 23:59:59", "999.00", "BANK_TRANSFER"); // the day before
        expense("2026-03-16 00:00:00", "999.00", "CASH");          // the day after

        Map<String, Object> model = dayPage("2026-03-15");

        assertThat(model.get("period")).isEqualTo("DAY");
        assertThat(model.get("periodStart")).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(model.get("month")).as("for the month picker").isEqualTo(LocalDate.of(2026, 3, 1));
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> byType = (Map<String, BigDecimal>) model.get("totalsByType");
        assertThat(byType.get("CASH")).isEqualByComparingTo("100.00");
        assertThat(byType.get("GCASH")).isEqualByComparingTo("300.00");
        assertThat(byType.get("BANK_TRANSFER")).isEqualByComparingTo("2500.00");
        assertThat((BigDecimal) model.get("total")).isEqualByComparingTo("2900.00");
        assertThat(model.get("entry")).isNull();
    }

    @Test
    void aDayHasItsOwnEntrySeparateFromItsMonth() throws Exception {
        expense("2026-03-15 00:00:00", "2500.00", "BANK_TRANSFER");
        expense("2026-03-20 00:00:00", "1000.00", "BANK_TRANSFER");
        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("month", "2026-03").param("statementAmount", "3600.00"));

        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("day", "2026-03-15")
                .param("statementAmount", "2520.00").param("note", "20.00 transfer fee"))
                .andExpect(redirectedUrl("/expenses/reconciliation?day=2026-03-15"))
                .andExpect(flash().attribute("msg", "Reconciliation for Sunday, March 15, 2026 saved."));

        Map<String, Object> day = dayPage("2026-03-15");
        @SuppressWarnings("unchecked")
        Map<String, Object> dayEntry = (Map<String, Object>) day.get("entry");
        assertThat((BigDecimal) dayEntry.get("statementAmount")).isEqualByComparingTo("2520.00");
        assertThat(dayEntry.get("note")).isEqualTo("20.00 transfer fee");
        assertThat((BigDecimal) day.get("difference")).isEqualByComparingTo("20.00");   // that day's bank transfers only

        Map<String, Object> month = page("03-2026");
        @SuppressWarnings("unchecked")
        Map<String, Object> monthEntry = (Map<String, Object>) month.get("entry");
        assertThat((BigDecimal) monthEntry.get("statementAmount")).as("the month's entry is untouched").isEqualByComparingTo("3600.00");
        assertThat((BigDecimal) month.get("difference")).isEqualByComparingTo("100.00");
        assertThat(this.jdbc.queryForList("SELECT period FROM bank_reconciliation ORDER BY period", String.class))
                .containsExactly("DAY", "MONTH");
    }

    @Test
    void aMalformedDayIsRefused() throws Exception {
        this.mvc.perform(ownerPost("/expenses/reconciliation/save").param("day", "15-03-2026").param("statementAmount", "1"))
                .andExpect(flash().attributeExists("failmsg"));
        assertThat(this.jdbc.queryForObject("SELECT count(*) FROM bank_reconciliation", Integer.class)).isZero();
    }

    @Test
    void bookkeepersCantOpenOrSaveADayEither() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/expenses/reconciliation").param("day", "2026-03-15")
                .with(user("clerk").roles("ADMIN")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        this.mvc.perform(MockMvcRequestBuilders.post("/expenses/reconciliation/save").with(user("clerk").roles("ADMIN")).with(csrf())
                .param("day", "2026-03-15").param("statementAmount", "1.00"))
                .andExpect(status().isForbidden());
    }

    @Test
    void theDatabaseKeepsAMonthEntryOnTheFirst() {
        // A month entry must start on the 1st; a day entry may be any day.
        this.jdbc.update("INSERT INTO bank_reconciliation (period, period_start, statement_amount, updated_by) VALUES ('DAY', '2026-03-15', 1, 'x')");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> this.jdbc.update(
                "INSERT INTO bank_reconciliation (period, period_start, statement_amount, updated_by) VALUES ('MONTH', '2026-03-15', 1, 'x')"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void theDefaultMonthIsThisMonth() throws Exception {
        Map<String, Object> model = this.mvc.perform(ownerGet("/expenses/reconciliation")).andReturn().getModelAndView().getModel();
        assertThat(model.get("month")).isEqualTo(LocalDate.now().withDayOfMonth(1));
    }
}
