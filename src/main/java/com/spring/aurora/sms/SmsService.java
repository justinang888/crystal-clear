package com.spring.aurora.sms;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.spring.aurora.dao.OrderDao;
import com.spring.aurora.model.Customer;
import com.spring.aurora.service.CustomerService;

/**
 * Text reminders to inactive customers (docs/specs/sms-inactive-customers.md): who is offered a
 * text and why some are skipped, filling in the message, sending through the phone and logging
 * every attempt.
 */
@Service
public class SmsService {

    /** Don't text the same customer again within this many days. */
    public static final int COOLDOWN_DAYS = 14;

    public static final String DEFAULT_TEMPLATE = "Hi {name} po, it's been {days} days since your last refill "
            + "from Aurora Crystal Clear at Aurora Blvd., Cubao. Call or text us to order.";

    /** Longer than any sensible reminder; keeps a pasted mistake from going out as ten texts. */
    public static final int MAX_LENGTH = 480;

    public static final String SKIP_OPTED_OUT = "Asked not to be texted";
    public static final String SKIP_NO_MOBILE = "No mobile number";
    public static final String SKIP_RECENT = "Texted in the last " + COOLDOWN_DAYS + " days";

    private final CustomerService customers;
    private final OrderDao orders;
    private final SmsMessageRepository messages;
    private final SmsGateway gateway;
    private final JdbcTemplate jdbc;

    public SmsService(CustomerService customers, OrderDao orders, SmsMessageRepository messages,
            SmsGateway gateway, JdbcTemplate jdbc) {
        this.customers = customers;
        this.orders = orders;
        this.messages = messages;
        this.gateway = gateway;
        this.jdbc = jdbc;
    }

    /**
     * An overdue customer offered for a text. {@code skipReason} is null when they can be texted;
     * otherwise it says why not and they are shown but never sent to.
     */
    public record Candidate(long customerId, String name, String greetingName, String mobile, LocalDate lastDelivered,
            long daysSince, int orderInterval, LocalDateTime lastTexted, String skipReason) {
    }

    /** What happened to one customer in a send. */
    public record Outcome(long customerId, String name, String mobile, String state, String detail, boolean sent) {
    }

    public boolean isConfigured() {
        return this.gateway.isConfigured();
    }

    /** Every customer overdue on {@code today}, most overdue first, each with a skip reason or none. */
    public List<Candidate> candidates(LocalDate today, LocalDateTime now) {
        Map<Long, LocalDate> lastDelivered = new HashMap<>();
        for (Object[] row : this.orders.findLastDeliveredPerCustomer()) {
            lastDelivered.put(((Number) row[0]).longValue(), ((Timestamp) row[1]).toLocalDateTime().toLocalDate());
        }
        Map<Long, LocalDateTime> lastTexted = new HashMap<>();
        for (SmsMessage m : this.messages.findAllByCreatedAtAfter(now.minusDays(COOLDOWN_DAYS))) {
            // An Error never reached the phone, so it may be retried straight away.
            if (!SmsMessage.ERROR.equals(m.getState())) {
                lastTexted.merge(m.getCustomerId(), m.getCreatedAt(), (a, b) -> a.isAfter(b) ? a : b);
            }
        }

        List<Candidate> result = new ArrayList<>();
        for (Customer c : this.customers.findAll()) {
            LocalDate delivered = lastDelivered.get(c.getCustomerId());
            if (!OverdueCustomers.isOverdue(delivered, c.getOrderInterval(), today)) {
                continue;
            }
            Optional<String> mobile = PhoneNumbers.toMobile(c.getMainNumber())
                    .or(() -> PhoneNumbers.toMobile(c.getAlternateNumber()));
            LocalDateTime texted = lastTexted.get(c.getCustomerId());
            String skip = c.isSmsOptOut() ? SKIP_OPTED_OUT
                    : mobile.isEmpty() ? SKIP_NO_MOBILE
                    : texted != null ? SKIP_RECENT
                    : null;
            String greeting = c.getContactName() != null && !c.getContactName().isBlank() ? c.getContactName().trim() : c.getName();
            result.add(new Candidate(c.getCustomerId(), c.getName(), greeting, mobile.orElse(null), delivered,
                    OverdueCustomers.daysSince(delivered, today), c.getOrderInterval(), texted, skip));
        }
        result.sort(Comparator.comparingLong(Candidate::daysSince).reversed().thenComparing(Candidate::name));
        return result;
    }

    /** The message one customer gets: {name} and {days} filled in. */
    public static String fill(String template, Candidate candidate) {
        return template.replace("{name}", candidate.greetingName())
                .replace("{days}", String.valueOf(candidate.daysSince()));
    }

    /**
     * Texts the chosen customers, one after another. Who may be texted is worked out again here,
     * not taken from the screen, so a stale page can't text someone who has since ordered, opted
     * out or been texted. Every attempt that reaches the gateway is logged.
     */
    public List<Outcome> send(List<Long> customerIds, String template, String user, LocalDate today, LocalDateTime now) {
        Map<Long, Candidate> byId = new HashMap<>();
        for (Candidate c : candidates(today, now)) {
            byId.put(c.customerId(), c);
        }
        List<Outcome> outcomes = new ArrayList<>();
        for (Long id : customerIds.stream().distinct().toList()) {
            Candidate c = byId.get(id);
            if (c == null) {
                outcomes.add(new Outcome(id, null, null, "Skipped", "No longer overdue", false));
                continue;
            }
            if (c.skipReason() != null) {
                outcomes.add(new Outcome(id, c.name(), c.mobile(), "Skipped", c.skipReason(), false));
                continue;
            }
            String text = fill(template, c);
            SmsGateway.Result result = this.gateway.send(c.mobile(), text);

            SmsMessage logged = new SmsMessage();
            logged.setCustomerId(id);
            logged.setPhoneNumber(c.mobile());
            logged.setBody(text);
            logged.setState(result.state());
            logged.setGatewayId(result.gatewayId());
            logged.setError(result.error());
            logged.setCreatedAt(LocalDateTime.now());
            logged.setCreatedBy(user);
            this.messages.save(logged);

            outcomes.add(new Outcome(id, c.name(), c.mobile(), result.state(), result.error(), result.ok()));
        }
        return outcomes;
    }

    /** The "Don't send texts" switch on the customer's page. */
    public void setOptOut(long customerId, boolean optOut) {
        this.jdbc.update("UPDATE customer SET sms_opt_out = ? WHERE customer_id = ?", optOut, customerId);
    }

    /** The texting log, newest first, with each customer's name. */
    public List<Map<String, Object>> log() {
        Map<Long, String> names = new HashMap<>();
        for (Customer c : this.customers.findAll()) {
            names.put(c.getCustomerId(), c.getName());
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SmsMessage m : this.messages.findAllByOrderByCreatedAtDescSmsMessageIdDesc()) {
            Map<String, Object> row = new HashMap<>();
            row.put("smsMessageId", m.getSmsMessageId());
            row.put("createdAt", m.getCreatedAt());
            row.put("customerId", m.getCustomerId());
            row.put("customerName", names.get(m.getCustomerId()));
            row.put("phoneNumber", m.getPhoneNumber());
            row.put("body", m.getBody());
            row.put("state", m.getState());
            row.put("error", m.getError());
            row.put("createdBy", m.getCreatedBy());
            rows.add(row);
        }
        return rows;
    }
}
