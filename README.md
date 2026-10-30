# aurora
Water Station System

Requires Java 21. Built on Spring Boot 4 with JSP views and PostgreSQL.

## Running the tests

The tests start their own PostgreSQL 16 (no Docker or local install needed) and build it
with the Flyway migrations in `src/main/resources/db/migration`:

```bash
./mvnw test
```

The dump converter has its own tests:

```bash
cd migration/postgres && python -m unittest test_mysqldump_to_pg
```

Tests marked **KNOWN BUG** pin current behavior that is wrong, so upgrades cannot change it
silently. When fixing one of those bugs, update its test to the correct behavior.
