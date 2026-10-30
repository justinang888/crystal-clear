# Moving Aurora from MySQL to PostgreSQL

This runbook moves the production database from MySQL to PostgreSQL and starts the new
build. The MySQL database is never changed, so rolling back means starting the old WAR again.

Plan for about 30 minutes of downtime. Most of that is installing PostgreSQL; the data load
itself takes under a minute for the October 2026 dump (about 1.46 million rows).

## What's in this folder

| File | Purpose |
|---|---|
| `schema.sql` | PostgreSQL tables, column-for-column the same as MySQL, plus lookup indexes |
| `mysqldump_to_pg.py` | Turns a mysqldump file into a psql load script (Python 3, standard library only) |
| `../../deploy/start_app.cmd` | New start script: `prod` profile, PostgreSQL, password from the environment |

The load script runs in one transaction. After loading, it compares every table's row count
and the sum of every money column with the values read from the dump. On any mismatch it
raises an error and rolls everything back.

## 1. One-time setup on the server

1. Install PostgreSQL 16 for Windows from https://www.postgresql.org/download/windows/
   (the EDB installer includes `psql`). Keep the default port, 5432.
2. Pick a strong password for the `aurora` database user. Store it in your password manager,
   **not** in any file. Then create the user and database (`psql` prompts for the
   `postgres` superuser password you set during install):

   ```bat
   psql -U postgres -c "CREATE ROLE aurora LOGIN PASSWORD 'CHANGE_ME'"
   psql -U postgres -c "CREATE DATABASE aurora OWNER aurora"
   ```

3. Store the same password for the app, in an **administrator** command prompt:

   ```bat
   setx /M AURORA_DB_PASSWORD "CHANGE_ME"
   ```

   Open a new command prompt afterwards so the variable is visible.

## 2. Cutover

1. **Stop Aurora** (close the window running `start_app.cmd`), so nothing writes to MySQL.
2. **Take a final dump** the same way as the daily backups, for example
   `mysqldump -u root -p aurora > Dump_final.sql`.
3. **Create the schema and load the data:**

   ```bat
   psql -v ON_ERROR_STOP=1 -U aurora -d aurora -f schema.sql
   python mysqldump_to_pg.py Dump_final.sql aurora_data.sql
   psql -v ON_ERROR_STOP=1 -U aurora -d aurora -f aurora_data.sql
   ```

   The converter prints each table's row count and money totals. `psql` must finish with
   `COMMIT` and `ANALYZE`, with no `ERROR`.
4. **Deploy:** copy the new `aurora.war` (built with `mvnw clean package`) and
   `deploy/start_app.cmd` to `C:\Apps`. Keep the old WAR and old `start_app.cmd`
   under different names for rollback.
5. **Start** with the new `start_app.cmd`. The log must show
   `The following profiles are active: prod` and `PostgreSQL10Dialect`.
6. **Smoke test:** log in, open a customer, then create and deliver one order and check the
   customer's container balance and AR. Then check daily sales, monthly totals and the AR
   summary against yesterday's figures.

**Rollback:** stop the app and start the old WAR with the old `start_app.cmd`. MySQL is
exactly as it was at step 2, but any orders entered on PostgreSQL after the cutover would
have to be re-entered.

## 3. Credentials

- **Change both app logins' passwords.** Both accounts share one password hash, and an
  earlier build printed a password from source code on every start, so treat the current
  password as exposed. To make a new hash (it prompts and doesn't echo the password):

  ```bat
  java -cp aurora.war -Dloader.path=WEB-INF/classes,WEB-INF/lib -Dloader.main=com.spring.aurora.tools.HashPassword org.springframework.boot.loader.PropertiesLauncher
  ```

  ```sql
  UPDATE users SET password = '<hash>' WHERE username = 'admin';
  ```

- Usernames still match case-insensitively at login, as they did on MySQL.
- After a few weeks without needing rollback, uninstall MySQL or at least change its root
  password. The old `start_app.cmd` contains it in plain text.

## 4. Backups

Replace the MySQL dumps with a nightly `pg_dump` (Task Scheduler, daily). `pg_dump` reads the
password from `%APPDATA%\postgresql\pgpass.conf` (one line: `localhost:5432:aurora:aurora:<password>`,
readable only by the account the task runs as):

```powershell
pg_dump -U aurora -d aurora -F c -f "D:\Backups\aurora_$(Get-Date -Format yyyyMMdd).dump"
```

**Test a restore once a month.** A backup that has never been restored isn't proven:

```bat
createdb -U postgres aurora_restore_test
pg_restore -U postgres -d aurora_restore_test --no-owner D:\Backups\<latest>.dump
psql -U postgres -d aurora_restore_test -c "SELECT count(*), max(created_at) FROM orders"
dropdb -U postgres aurora_restore_test
```

The order count and latest order date should match production as of the backup time.

## Notes for developers

- Local runs need `AURORA_DB_PASSWORD` set (and `AURORA_DB_URL` / `AURORA_DB_USERNAME` if
  they differ from the defaults in `application-*.properties`). Build with `-Pdev` for SQL logging.
- Hibernate no longer changes the schema (`ddl-auto=none`). Make schema changes as SQL scripts.
- Money columns are `numeric` in the database but `Double` in Java. Moving to `BigDecimal`
  is planned for Phase 3 of `MODERNIZATION_ASSESSMENT.md`.
