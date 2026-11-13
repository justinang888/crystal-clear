package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.spring.aurora.entity.OrderCustomerEntity;

import jakarta.persistence.EntityManagerFactory;

/**
 * The Pending orders screen lists every pending order, from any day, with its customer's name.
 * It asks the database only for pending orders and gets the names in one query: with the
 * database online, every extra query is a trip over the internet.
 */
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=WARN" })
class PendingOrdersTest extends PostgresIntegrationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        long ana = insertCustomer("Ana");
        long ben = insertCustomer("Ben");
        long cora = insertCustomer("Cora");
        order(ana, "DR-1", "Pending", "2025-12-30 09:00:00");
        order(ben, "DR-2", "Delivered", "2026-03-01 08:00:00");
        order(ben, "DR-3", "Pending", "2026-03-02 10:00:00");
        order(cora, "DR-4", "pending", "2026-03-03 11:00:00");   // old rows may differ in case
        order(cora, "DR-5", "Cancelled", "2026-03-03 12:00:00");
    }

    private void order(long customer, String dr, String status, String at) {
        this.jdbc.update("INSERT INTO orders (customer_id, delivery_receipt_num, status, total_amount, amount_paid, "
                + "round_refill_only_count, slim_refill_only_count, cont_round_returned, cont_slim_returned, "
                + "slim_free_count, round_free_count, created_at) VALUES (?, ?, ?, 55, 55, 1, 0, 0, 0, 0, 0, ?::timestamp)",
                customer, dr, status, at);
    }

    private Map<String, Object> page() throws Exception {
        return this.mvc.perform(get("/orders/pending")).andReturn().getModelAndView().getModel();
    }

    @Test
    void listsEveryPendingOrderWithItsCustomer() throws Exception {
        Map<String, Object> model = page();

        @SuppressWarnings("unchecked")
        List<OrderCustomerEntity> orders = (List<OrderCustomerEntity>) model.get("orders");
        assertThat(orders).extracting(o -> o.getOrder().getDeliveryReceiptNum() + " " + o.getCustomerName())
                .containsExactly("DR-1 Ana", "DR-3 Ben", "DR-4 Cora");
        assertThat(model.get("pendingCount")).isEqualTo(3);
    }

    @Test
    void asksTheDatabaseTwiceHoweverManyOrdersArePending() throws Exception {
        Statistics stats = this.entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        page();

        // One query for the pending orders, one for the customers' names.
        assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
    }
}
