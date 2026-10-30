package com.spring.aurora;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Base class for tests that run the whole application against a real PostgreSQL 16.
 * Flyway creates the schema from src/main/resources/db/migration, as it does in production.
 * One database serves the whole test run; every table is emptied before each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class PostgresIntegrationTest {

    private static final EmbeddedPostgres POSTGRES = startPostgres();

    private static EmbeddedPostgres startPostgres() {
        try {
            return EmbeddedPostgres.builder().start();
        } catch (Exception e) {
            throw new IllegalStateException("Could not start the embedded PostgreSQL test database", e);
        }
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void emptyAllTables() {
        this.jdbc.execute("TRUNCATE ar_summary, container, customer, customer_price, debt, expense, "
                + "order_product, orders, payment, products, users RESTART IDENTITY");
    }

    /** The first two of the shop's products, at their list prices. */
    protected void insertProducts() {
        this.jdbc.update("INSERT INTO products (product_id, name, initial_price, selling_price) VALUES "
                + "(1, 'Round Refill', 55.00, 55.00), (2, 'Slim Refill', 60.00, 60.00)");
    }

    protected long insertCustomer(String name) {
        return this.jdbc.queryForObject("INSERT INTO customer (type, name, order_interval) "
                + "VALUES ('Residential', ?, 7) RETURNING customer_id", Long.class, name);
    }

    /** A GET as a logged-in user. */
    protected static MockHttpServletRequestBuilder get(String url) {
        return MockMvcRequestBuilders.get(url).with(user("tester").roles("ADMIN"));
    }

    /** A form POST as a logged-in user, with a CSRF token like the real forms send. */
    protected static MockHttpServletRequestBuilder post(String url) {
        return MockMvcRequestBuilders.post(url).with(user("tester").roles("ADMIN")).with(csrf());
    }
}
