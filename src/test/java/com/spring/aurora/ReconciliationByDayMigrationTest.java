package com.spring.aurora;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/** V4 turns the monthly reconciliation into day-or-month entries; entries saved before it must survive. */
class ReconciliationByDayMigrationTest {

    @Test
    void monthlyEntriesSavedBeforeV4KeepTheirAmountAndNote() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            Flyway.configure().dataSource(postgres.getPostgresDatabase()).target("3").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(postgres.getPostgresDatabase());
            jdbc.update("INSERT INTO bank_reconciliation (month, statement_amount, note, updated_by) "
                    + "VALUES ('2026-03-01', 2650.00, 'March statement', 'boss')");

            Flyway.configure().dataSource(postgres.getPostgresDatabase()).load().migrate();

            Map<String, Object> row = jdbc.queryForMap("SELECT * FROM bank_reconciliation");
            assertThat(row.get("period")).isEqualTo("MONTH");
            assertThat(row.get("period_start").toString()).isEqualTo("2026-03-01");
            assertThat((BigDecimal) row.get("statement_amount")).isEqualByComparingTo("2650.00");
            assertThat(row.get("note")).isEqualTo("March statement");
            assertThat(row.get("updated_by")).isEqualTo("boss");
        }
    }
}
