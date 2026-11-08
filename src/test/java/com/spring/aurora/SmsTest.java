package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.spring.aurora.demo.FakeSmsGateway;
import com.spring.aurora.model.Customer;
import com.spring.aurora.sms.SmsGateway;
import com.spring.aurora.sms.SmsMessage;
import com.spring.aurora.sms.SmsMessageRepository;
import com.spring.aurora.sms.SmsService;
import com.spring.aurora.sms.SmsService.Candidate;
import com.spring.aurora.sms.SmsService.Outcome;

/** Text reminders to inactive customers: see docs/specs/sms-inactive-customers.md. */
class SmsTest extends PostgresIntegrationTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private SmsMessageRepository smsMessages;

    @MockitoBean
    private SmsGateway gateway;

    private FakeSmsGateway phone;
    private long customerId;

    @BeforeEach
    void setUp() {
        this.customerId = insertCustomer("Text Customer");
        this.phone = new FakeSmsGateway();
        when(this.gateway.isConfigured()).thenReturn(true);
        when(this.gateway.send(anyString(), anyString()))
                .thenAnswer(call -> this.phone.send(call.getArgument(0), call.getArgument(1)));
    }

    private long customer(String name, String contactName, String mainNumber, int interval) {
        return this.jdbc.queryForObject("INSERT INTO customer (type, name, contact_name, main_number, order_interval) "
                + "VALUES ('Residential', ?, ?, ?, ?) RETURNING customer_id", Long.class, name, contactName, mainNumber, interval);
    }

    private void order(long customer, String status, LocalDate day) {
        this.jdbc.update("INSERT INTO orders (customer_id, delivery_receipt_num, status, total_amount, amount_paid, "
                + "round_refill_only_count, slim_refill_only_count, cont_round_returned, cont_slim_returned, "
                + "slim_free_count, round_free_count, created_at) VALUES (?, 'DR', ?, 55, 55, 1, 0, 0, 0, 0, 0, ?)",
                customer, status, Timestamp.valueOf(day.atTime(12, 0)));
    }

    private boolean optedOut(long id) {
        return this.jdbc.queryForObject("SELECT sms_opt_out FROM customer WHERE customer_id = ?", Boolean.class, id);
    }

    @SuppressWarnings("unchecked")
    private List<Candidate> candidates() throws Exception {
        Map<String, Object> model = this.mvc.perform(get("/sms/compose")).andReturn().getModelAndView().getModel();
        return (List<Candidate>) model.get("candidates");
    }

    private static String ids(long... ids) {
        StringBuilder s = new StringBuilder();
        for (long id : ids) {
            s.append(s.isEmpty() ? "" : ",").append(id);
        }
        return s.toString();
    }

    @SuppressWarnings("unchecked")
    private List<Outcome> send(String template, long... customerIds) throws Exception {
        return (List<Outcome>) this.mvc.perform(post("/sms/send").param("customerIds", ids(customerIds)).param("template", template))
                .andExpect(redirectedUrl("/sms/list"))
                .andReturn().getFlashMap().get("outcomes");
    }

    // --- The opt-out flag and the log (the schema) ---

    @Test
    void newCustomersMayBeTexted() {
        assertThat(optedOut(this.customerId)).isFalse();
    }

    @Test
    void editingACustomerKeepsTheirTextOptOut() throws Exception {
        this.jdbc.update("UPDATE customer SET sms_opt_out = true WHERE customer_id = ?", this.customerId);

        // The edit form and the container balances form post a whole customer; neither carries the opt-out.
        this.mvc.perform(post("/customers/update")
                .param("customerId", String.valueOf(this.customerId))
                .param("type", "Residential")
                .param("name", "Text Customer")
                .param("orderInterval", "7"));
        this.mvc.perform(post("/customers/updateContainerBalances")
                .param("customerId", String.valueOf(this.customerId))
                .param("type", "Residential")
                .param("name", "Text Customer")
                .param("totalRound", "2"));

        assertThat(optedOut(this.customerId)).isTrue();
    }

    @Test
    void theTextLogKeepsEveryField() {
        SmsMessage message = new SmsMessage();
        message.setCustomerId(this.customerId);
        message.setPhoneNumber("+639170000001");
        message.setBody("Hi, it's been a while.");
        message.setState("Pending");
        message.setGatewayId("abc-123");
        message.setCreatedAt(LocalDateTime.of(2026, 11, 8, 9, 30));
        message.setCreatedBy("tester");
        long id = this.smsMessages.save(message).getSmsMessageId();

        SmsMessage saved = this.smsMessages.findById(id).orElseThrow();
        assertThat(saved).usingRecursiveComparison().isEqualTo(message);
        assertThat(saved.getError()).isNull();
    }

    // --- Who is offered a text ---

    @Test
    void onlyCustomersPastTheirIntervalPlusTwoDaysAreOffered() throws Exception {
        long overdue = customer("Ana", null, "0917 000 0001", 7);
        order(overdue, "Delivered", TODAY.minusDays(10));
        order(overdue, "Cancelled", TODAY.minusDays(1));   // cancelled and pending orders don't count
        order(overdue, "Pending", TODAY);
        long notYet = customer("Ben", null, "0917 000 0002", 7);
        order(notYet, "Delivered", TODAY.minusDays(9));
        long neverDelivered = customer("Cora", null, "0917 000 0003", 7);
        order(neverDelivered, "Pending", TODAY.minusDays(40));
        long blankInterval = customer("Dan", null, "0917 000 0004", 0);
        order(blankInterval, "Delivered", TODAY.minusDays(33));
        long blankIntervalNotYet = customer("Eve", null, "0917 000 0005", 0);
        order(blankIntervalNotYet, "Delivered", TODAY.minusDays(32));

        List<Candidate> offered = candidates();

        assertThat(offered).extracting(Candidate::customerId).containsExactly(blankInterval, overdue);  // most overdue first
        Candidate ana = offered.get(1);
        assertThat(ana.daysSince()).isEqualTo(10);
        assertThat(ana.lastDelivered()).isEqualTo(TODAY.minusDays(10));
        assertThat(ana.mobile()).isEqualTo("+639170000001");
        assertThat(ana.skipReason()).isNull();
    }

    @Test
    void customersWhoCantBeTextedAreShownWithTheReason() throws Exception {
        long optedOut = customer("Fe", null, "0917 000 0006", 7);
        order(optedOut, "Delivered", TODAY.minusDays(20));
        this.jdbc.update("UPDATE customer SET sms_opt_out = true WHERE customer_id = ?", optedOut);
        long landline = customer("Gil", null, "(02) 8123 4567", 7);
        order(landline, "Delivered", TODAY.minusDays(20));
        long alternate = customer("Hana", null, "(02) 8123 4567", 7);
        this.jdbc.update("UPDATE customer SET alternate_number = '0918 000 0008' WHERE customer_id = ?", alternate);
        order(alternate, "Delivered", TODAY.minusDays(20));

        assertThat(candidates()).extracting(Candidate::name, Candidate::mobile, Candidate::skipReason).containsExactly(
                org.assertj.core.groups.Tuple.tuple("Fe", "+639170000006", SmsService.SKIP_OPTED_OUT),
                org.assertj.core.groups.Tuple.tuple("Gil", null, SmsService.SKIP_NO_MOBILE),
                org.assertj.core.groups.Tuple.tuple("Hana", "+639180000008", null));
    }

    // --- Sending ---

    @Test
    void sendTextsTheChosenCustomersAndLogsEachText() throws Exception {
        long ana = customer("Ana Cruz", null, "0917 000 0001", 7);
        order(ana, "Delivered", TODAY.minusDays(10));
        long shop = customer("Demo Hardware", "Ben", "0917-000-0002", 14);
        order(shop, "Delivered", TODAY.minusDays(20));

        List<Outcome> outcomes = send("Hi {name}, it's been {days} days.", ana, shop);

        assertThat(outcomes).extracting(Outcome::sent).containsExactly(true, true);
        assertThat(this.phone.sent()).containsExactly(
                new FakeSmsGateway.Sent("+639170000001", "Hi Ana Cruz, it's been 10 days."),
                new FakeSmsGateway.Sent("+639170000002", "Hi Ben, it's been 20 days."));   // the contact person, when set
        List<SmsMessage> log = this.smsMessages.findAllByOrderByCreatedAtDescSmsMessageIdDesc();
        assertThat(log).hasSize(2);
        SmsMessage first = log.get(1);
        assertThat(first.getCustomerId()).isEqualTo(ana);
        assertThat(first.getPhoneNumber()).isEqualTo("+639170000001");
        assertThat(first.getBody()).isEqualTo("Hi Ana Cruz, it's been 10 days.");
        assertThat(first.getState()).isEqualTo("Pending");
        assertThat(first.getGatewayId()).startsWith("demo-");
        assertThat(first.getCreatedBy()).isEqualTo("tester");
        assertThat(first.getError()).isNull();
    }

    @Test
    void sendWorksOutWhoMayBeTextedAgainInsteadOfTrustingTheScreen() throws Exception {
        long ok = customer("Ana", null, "0917 000 0001", 7);
        order(ok, "Delivered", TODAY.minusDays(10));
        long optedOut = customer("Fe", null, "0917 000 0006", 7);
        order(optedOut, "Delivered", TODAY.minusDays(10));
        this.jdbc.update("UPDATE customer SET sms_opt_out = true WHERE customer_id = ?", optedOut);
        long orderedSince = customer("Ben", null, "0917 000 0002", 7);
        order(orderedSince, "Delivered", TODAY.minusDays(1));

        List<Outcome> outcomes = send("Hi {name}", ok, optedOut, orderedSince, ok);

        assertThat(outcomes).extracting(Outcome::customerId, Outcome::state, Outcome::detail).containsExactly(
                org.assertj.core.groups.Tuple.tuple(ok, "Pending", null),
                org.assertj.core.groups.Tuple.tuple(optedOut, "Skipped", SmsService.SKIP_OPTED_OUT),
                org.assertj.core.groups.Tuple.tuple(orderedSince, "Skipped", "No longer overdue"));
        assertThat(this.phone.sent()).hasSize(1);   // the repeated id texted once
        assertThat(this.smsMessages.count()).isEqualTo(1);
    }

    @Test
    void nobodyIsTextedTwiceWithinFourteenDays() throws Exception {
        long ana = customer("Ana", null, "0917 000 0001", 7);
        order(ana, "Delivered", TODAY.minusDays(10));
        send("Hi {name}", ana);

        assertThat(candidates().get(0).skipReason()).isEqualTo(SmsService.SKIP_RECENT);
        assertThat(send("Hi again", ana)).extracting(Outcome::detail).containsExactly(SmsService.SKIP_RECENT);
        assertThat(this.phone.sent()).hasSize(1);

        // Fifteen days later the cooldown is over.
        this.jdbc.update("UPDATE sms_message SET created_at = created_at - interval '15 days'");
        assertThat(candidates().get(0).skipReason()).isNull();
    }

    @Test
    void oneFailureDoesntStopTheRestAndCanBeRetried() throws Exception {
        long unreachable = customer("Ana", null, "0917 000 0000", 7);   // the fake phone fails numbers ending 0000
        order(unreachable, "Delivered", TODAY.minusDays(10));
        long fine = customer("Ben", null, "0917 000 0002", 7);
        order(fine, "Delivered", TODAY.minusDays(10));

        this.mvc.perform(post("/sms/send").param("customerIds", ids(unreachable, fine)).param("template", "Hi {name}"))
                .andExpect(redirectedUrl("/sms/list"))
                .andExpect(flash().attribute("failmsg", "1 text handed to the phone, 1 failed."));

        List<SmsMessage> log = this.smsMessages.findAllByOrderByCreatedAtDescSmsMessageIdDesc();
        assertThat(log).extracting(SmsMessage::getCustomerId, SmsMessage::getState)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(fine, "Pending"),
                        org.assertj.core.groups.Tuple.tuple(unreachable, SmsMessage.ERROR));
        assertThat(log.get(1).getError()).startsWith("Couldn't reach the phone");
        // A text that never reached the phone doesn't start the cooldown.
        assertThat(candidates()).filteredOn(c -> c.customerId() == unreachable)
                .extracting(Candidate::skipReason).containsOnlyNulls();
    }

    @Test
    void sendingNeedsAPostWithTheCsrfToken() throws Exception {
        long ana = customer("Ana", null, "0917 000 0001", 7);
        order(ana, "Delivered", TODAY.minusDays(10));

        this.mvc.perform(MockMvcRequestBuilders.post("/sms/send").with(user("tester").roles("ADMIN"))
                .param("customerIds", ids(ana)).param("template", "Hi"))
                .andExpect(status().isForbidden());
        this.mvc.perform(get("/sms/send").param("customerIds", ids(ana)).param("template", "Hi"));

        assertThat(this.phone.sent()).isEmpty();
        assertThat(this.smsMessages.count()).isZero();
    }

    @Test
    void nothingIsSentWhenTextingIsntSetUp() throws Exception {
        when(this.gateway.isConfigured()).thenReturn(false);
        long ana = customer("Ana", null, "0917 000 0001", 7);
        order(ana, "Delivered", TODAY.minusDays(10));

        Map<String, Object> model = this.mvc.perform(get("/sms/compose")).andReturn().getModelAndView().getModel();
        assertThat(model.get("configured")).isEqualTo(false);
        this.mvc.perform(post("/sms/send").param("customerIds", ids(ana)).param("template", "Hi"))
                .andExpect(redirectedUrl("/sms/compose"))
                .andExpect(flash().attribute("failmsg", "Texting isn't set up on this PC, so nothing was sent."));

        assertThat(this.phone.sent()).isEmpty();
        assertThat(this.smsMessages.count()).isZero();
    }

    @Test
    void anEmptyOrTooLongMessageIsRefused() throws Exception {
        long ana = customer("Ana", null, "0917 000 0001", 7);
        order(ana, "Delivered", TODAY.minusDays(10));

        for (String template : List.of("   ", "x".repeat(SmsService.MAX_LENGTH + 1))) {
            this.mvc.perform(post("/sms/send").param("customerIds", ids(ana)).param("template", template))
                    .andExpect(redirectedUrl("/sms/compose"));
        }
        this.mvc.perform(post("/sms/send").param("template", "Hi"))
                .andExpect(redirectedUrl("/sms/compose"))
                .andExpect(flash().attribute("failmsg", "Pick at least one customer to text."));
        assertThat(this.phone.sent()).isEmpty();
    }

    @Test
    void theOptOutSwitchOnTheCustomersPage() throws Exception {
        this.mvc.perform(post("/sms/optOut").param("customerId", String.valueOf(this.customerId)).param("optOut", "true"))
                .andExpect(redirectedUrl("/customers/view?customerId=" + this.customerId));
        assertThat(optedOut(this.customerId)).isTrue();
        Customer shown = (Customer) this.mvc.perform(get("/customers/view").param("customerId", String.valueOf(this.customerId)))
                .andReturn().getModelAndView().getModel().get("customer");
        assertThat(shown.isSmsOptOut()).isTrue();

        this.mvc.perform(post("/sms/optOut").param("customerId", String.valueOf(this.customerId)).param("optOut", "false"));
        assertThat(optedOut(this.customerId)).isFalse();
    }

    @Test
    void theLogShowsEachTextWithTheCustomersName() throws Exception {
        long ana = customer("Ana", null, "0917 000 0001", 7);
        order(ana, "Delivered", TODAY.minusDays(10));
        send("Hi {name}", ana);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) this.mvc.perform(get("/sms/list"))
                .andReturn().getModelAndView().getModel().get("messages");

        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row).containsEntry("customerName", "Ana").containsEntry("phoneNumber", "+639170000001")
                    .containsEntry("body", "Hi Ana").containsEntry("state", "Pending").containsEntry("createdBy", "tester");
        });
    }
}
