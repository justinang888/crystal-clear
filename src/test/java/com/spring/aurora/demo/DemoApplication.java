package com.spring.aurora.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.spring.aurora.AuroraApplication;
import com.spring.aurora.sms.SmsGateway;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Runs the app on http://localhost:8081/aurora against a throwaway PostgreSQL filled with
 * made-up customers and orders, for trying out screens without the shop's data:
 *
 *   mvnw spring-boot:test-run -Dspring-boot.test-run.main-class=com.spring.aurora.demo.DemoApplication
 *
 * Log in as demo / demo. The database lives in memory and is gone when the app stops.
 */
public class DemoApplication {

    public static void main(String[] args) throws Exception {
        // A devtools restart would run main() again and start a second database.
        System.setProperty("spring.devtools.restart.enabled", "false");
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                postgres.close();
            } catch (Exception ignored) {
                // The JVM is exiting anyway.
            }
        }));

        SpringApplication app = new SpringApplication(AuroraApplication.class);
        app.setAdditionalProfiles("prod");
        // A fake phone for the texting screens: accepts every text, sends nothing.
        app.addInitializers(context -> ((GenericApplicationContext) context)
                .registerBean(SmsGateway.class, FakeSmsGateway::new));
        ConfigurableApplicationContext context = app.run(
                "--server.port=8081",
                "--spring.datasource.url=" + postgres.getJdbcUrl("postgres", "postgres"),
                "--spring.datasource.username=postgres",
                "--spring.datasource.password=");

        seed(context.getBean(JdbcTemplate.class), context.getBean(PasswordEncoder.class));
        System.out.println("Demo data loaded. Open http://localhost:8081/aurora and log in as demo / demo.");
        System.out.println("Demo database: " + postgres.getJdbcUrl("postgres", "postgres"));
    }

    private static void seed(JdbcTemplate jdbc, PasswordEncoder encoder) {
        jdbc.update("INSERT INTO users (email, enabled, first_name, last_name, password, role, username) "
                + "VALUES ('demo@example.test', true, 'Demo', 'User', ?, 'ROLE_ADMIN', 'demo')", encoder.encode("demo"));

        jdbc.update("INSERT INTO products (product_id, name, description, initial_price, selling_price) VALUES "
                + "(1, 'Round Refill', '5 gallon round', 55.00, 55.00), (2, 'Slim Refill', '5 gallon slim', 60.00, 60.00), "
                + "(3, 'Round Container', 'New round container', 250.00, 250.00), (4, 'Slim Container', 'New slim container', 280.00, 280.00)");
        // The ids above were given by hand; move the id counter past them so new products can be added.
        jdbc.queryForObject("SELECT setval(pg_get_serial_sequence('products', 'product_id'), 4)", Long.class);

        String[][] customers = {
            {"Business", "Sample Bakery", "12 Example St.", "Ana Reyes", "0917 000 0001", "7"},
            {"Business", "Demo Hardware", "4 Placeholder Ave.", "Ben Cruz", "0917 000 0002", "3"},
            {"Residential", "Carla Santos", "88 Test Village", "Carla Santos", "0917 000 0003", "7"},
            {"Residential", "Dino Garcia", "21 Mock Rd.", "Dino Garcia", "0917 000 0004", "14"},
            {"Business", "Fictional Clinic", "1 Sandbox Blvd.", "Eli Torres", "0917 000 0005", "5"},
            {"Residential", "Faye Lim", "7 Sample Subd.", "Faye Lim", "0917 000 0006", "10"},
            {"Business", "Pretend Cafe", "33 Mockup St.", "Gino Ramos", "0917 000 0007", "2"},
            {"Residential", "Hana Dela Cruz", "5 Demo Heights", "Hana Dela Cruz", "0917 000 0008", "7"},
        };
        for (String[] c : customers) {
            jdbc.update("INSERT INTO customer (type, name, address, contact_name, main_number, order_interval, total_round, total_slim) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 2, 1)", c[0], c[1], c[2], c[3], c[4], Integer.parseInt(c[5]));
        }
        jdbc.update("INSERT INTO customer_price (customer_id, product_id, selling_price) VALUES (1, 1, 50.00), (1, 2, 55.00), (2, 1, 52.00)");

        // Orders over the last 40 days, a few still pending today.
        LocalDate today = LocalDate.now();
        int receipt = 1000;
        for (int day = 40; day >= 0; day--) {
            for (int customer = 1; customer <= customers.length; customer++) {
                if ((day + customer) % 3 != 0 && day != 0) {
                    continue;
                }
                int round = 1 + (day + customer) % 4;
                int slim = (day * customer) % 3;
                double total = round * 55.0 + slim * 60.0;
                double paid = (customer % 3 == 0) ? total - 55.0 : total;
                String status = (day == 0 && customer % 2 == 0) ? "Pending" : "Delivered";
                LocalDateTime at = today.minusDays(day).atTime(8 + customer, 15);
                long orderId = jdbc.queryForObject("INSERT INTO orders (customer_id, delivery_receipt_num, status, amount_paid, "
                        + "total_amount, round_refill_only_count, slim_refill_only_count, slim_free_count, round_free_count, "
                        + "cont_round_returned, cont_slim_returned, created_at, remarks) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, ?, 0, ?, ?) RETURNING order_id", Long.class,
                        customer, String.valueOf(receipt++), status, paid, total, round, slim, round > 1 ? 1 : 0, at,
                        paid < total ? "Will pay the rest next delivery" : null);
                jdbc.update("INSERT INTO order_product (order_id, product_id, quantity) VALUES (?, 1, ?)", orderId, round);
                if (slim > 0) {
                    jdbc.update("INSERT INTO order_product (order_id, product_id, quantity) VALUES (?, 2, ?)", orderId, slim);
                }
                jdbc.update("INSERT INTO container (customer_id, order_id, status, round_count, slim_count, created_at) "
                        + "VALUES (?, ?, 'B', ?, ?, ?)", customer, orderId, round, slim, at);
                if (paid > 0 && status.equals("Delivered")) {
                    jdbc.update("INSERT INTO payment (customer_id, amount, payment_type, remarks, created_at) "
                            + "VALUES (?, ?, ?, ?, ?)", customer, paid, customer % 4 == 0 ? "CHECK" : "CASH", "DR " + (receipt - 1), at);
                }
                if (paid < total) {
                    jdbc.update("INSERT INTO debt (customer_id, amount, remarks, created_at, linked_order_id) VALUES (?, ?, ?, ?, ?)",
                            customer, total - paid, "Balance on DR " + (receipt - 1), at, orderId);
                }
            }
            if (day % 2 == 0) {
                jdbc.update("INSERT INTO expense (amount, description, created_at) VALUES (?, ?, ?)",
                        day % 4 == 0 ? 350.00 : 120.50, day % 4 == 0 ? "Delivery fuel" : "Filter cartridges", today.minusDays(day).atTime(17, 0));
            }
        }
        jdbc.update("INSERT INTO ar_summary (ar_amount, customer_id, updated_at) "
                + "SELECT sum(amount), customer_id, localtimestamp FROM debt GROUP BY customer_id");

        seedLapsedCustomers(jdbc, today, receipt);
    }

    /**
     * Customers who stopped ordering, for the texting screens (Inactive Customers > Text overdue
     * customers): one of each case the compose screen shows. Last deliveries are days ago.
     */
    private static void seedLapsedCustomers(JdbcTemplate jdbc, LocalDate today, int receipt) {
        // type, name, contact, main number, interval, last delivery (days ago, -1 = never), opted out
        Object[][] lapsed = {
            {"Business", "Lapsed Laundry", "Iris Tan", "0917 000 0009", 7, 18, false},     // texted
            {"Residential", "Joel Villanueva", "Joel Villanueva", "0917-000-0010", 14, 21, false},
            {"Residential", "Kai Navarro", "Kai Navarro", "(02) 8000 0011", 7, 15, false}, // landline: skipped
            {"Residential", "Lito Mendoza", "Lito Mendoza", "0917 000 0000", 7, 12, false}, // the fake phone fails it
            {"Residential", "Mara Uy", "Mara Uy", "0917 000 0013", 10, 30, true},           // opted out: skipped
            {"Residential", "Nina Ocampo", "Nina Ocampo", "0917 000 0014", 7, -1, false},   // never ordered: not listed
        };
        for (Object[] c : lapsed) {
            long id = jdbc.queryForObject("INSERT INTO customer (type, name, address, contact_name, main_number, order_interval, "
                    + "total_round, total_slim, sms_opt_out) VALUES (?, ?, 'Sample address', ?, ?, ?, 1, 0, ?) RETURNING customer_id",
                    Long.class, c[0], c[1], c[2], c[3], c[4], c[6]);
            int daysAgo = (int) c[5];
            if (daysAgo < 0) {
                continue;
            }
            for (int extra : new int[] {(int) c[4], 0}) {   // the delivery before the last one, then the last
                LocalDateTime at = today.minusDays(daysAgo + extra).atTime(10, 0);
                long orderId = jdbc.queryForObject("INSERT INTO orders (customer_id, delivery_receipt_num, status, amount_paid, "
                        + "total_amount, round_refill_only_count, slim_refill_only_count, slim_free_count, round_free_count, "
                        + "cont_round_returned, cont_slim_returned, created_at) "
                        + "VALUES (?, ?, 'Delivered', 55, 55, 1, 0, 0, 0, 1, 0, ?) RETURNING order_id", Long.class,
                        id, String.valueOf(receipt++), at);
                jdbc.update("INSERT INTO order_product (order_id, product_id, quantity) VALUES (?, 1, 1)", orderId);
            }
        }
    }
}
