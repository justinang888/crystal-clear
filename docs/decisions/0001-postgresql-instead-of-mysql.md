# ADR-0001: Move the database from MySQL to PostgreSQL

## Status
Accepted

## Date
2026-10-31

## Context
Aurora ran on MySQL, connecting as `root` with the password in plain text, and the schema was
managed by Hibernate's `ddl-auto=update`. The modernization started by putting a safety net in
place (Phase 0 of `MODERNIZATION_ASSESSMENT.md`), and the database moved in the same phase.

The owner decided on PostgreSQL. The reasons for that choice were not written down at the time;
this record captures the decision and how it was made safe.

## Decision
Use PostgreSQL 16, with the schema owned by Flyway (`src/main/resources/db/migration`, starting
from `V1__initial_schema.sql`, column for column the same as the MySQL tables).

Existing data moves with `migration/postgres/mysqldump_to_pg.py`, which loads a mysqldump in one
transaction and rolls back unless every table's row count and every money column's sum match the
dump.

## Alternatives Considered

### Stay on MySQL and only fix its configuration
- Pros: no data move.
- Cons: none recorded; the owner chose to move.

## Consequences
- The MySQL database is never changed by the cutover, so rolling back is starting the old WAR.
- Tests run against a real PostgreSQL 16 that they start themselves (no Docker needed).
- The database password comes from `AURORA_DB_PASSWORD`, never from a file in the repo.
