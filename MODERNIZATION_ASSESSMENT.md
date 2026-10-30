# Aurora – Modernization Assessment

_Assessed 2026-10-31. Read-only review; no code was changed._

> **Status (2026-10-31):** Phase 0 is done. The database
> has also moved from MySQL to PostgreSQL; see `migration/postgres/README.md` for the cutover
> runbook. Findings 1, 4 and the vulnerable MySQL driver are fixed there. Findings 2, 3 and 5
> and the other dependencies remain for Phase 2.
>
> Phase 1 is done: `./mvnw test` runs 29 integration and unit tests against a real PostgreSQL,
> plus 5 tests for the dump converter. They confirmed the three bugs listed under
> "Found by the Phase 1 tests" below.

## 1. What Aurora is today

| Area | Current state |
|---|---|
| Purpose | Water-station back office: customers, per-customer prices, orders, container borrow/return tracking, payments, debts (AR), expenses, daily/monthly sales reports |
| Runtime | Java 8 (Temurin 8u372 on the production box, per `hs_err_pid7944.log`) running `java -jar aurora.war` from `C:\Apps` |
| Framework | Spring Boot **2.2.6** (Spring 5.2, Spring Security 5.2, Hibernate 5.4, Tomcat 9.0.33) |
| Views | 47 JSP pages + JSTL, Bootstrap **3**, jQuery, DataTables, bootstrap-datepicker (all vendored under `static/resources`) |
| Data | MySQL via Spring Data JPA (`CrudRepository` + JPQL), schema managed by `ddl-auto=update` |
| Size | ~5,100 lines Java (`OrderController` alone is 1,041), ~4,500 lines JSP |
| Tests | None (`src/test` does not exist) |
| Source control | None. Releases are kept as dated WAR copies; backups are daily ~77 MB SQL dumps |

## 2. Critical issues (fix before or alongside any upgrade)

1. **Production runs the `dev` profile.** The deployed `aurora.war` contains `spring.profiles.active=dev` (the `dev` Maven profile is `activeByDefault`). `application-dev.properties` turns on Spring Security DEBUG, Hibernate SQL DEBUG, and **`BasicBinder=TRACE`**, so every bound SQL parameter (customer data, amounts, login lookups) goes to the console/logs. It also adds per-request overhead.
   _Fix:_ build with `-Pprod`, or set `spring.profiles.active=prod` in `start_app.cmd`.

2. **The custom `SecurityConfig` is never loaded.** It has no `@Configuration`/`@EnableWebSecurity`, so Spring Boot's default security is what actually runs. The intended success URL (`/customers/list`), the asset exclusions, and the frame-options settings are dead code. Login still works, but only because Boot auto-wires `AuroraUserDetailsService`.

3. **No authorization by role.** Users have `ROLE_ADMIN` / `ROLE_USER`, but no URL or method checks for them anywhere. Any logged-in user can do everything, including the user-management page.

4. **Secrets in source and scripts.**
   - `AuroraApplication.main()` hard-codes a plaintext password and prints its BCrypt hash to stdout on every start. Remove it and change that password.
   - The DB root password is in plain text in `application*.properties` and in `start_app.cmd`. The app connects as MySQL `root`.
   - `data-mysql.sql` runs on **every startup** (`initialization-mode=always`) and seeds two accounts that share one password hash.

5. **Stored XSS risk.** The JSPs print about 500 raw `${...}` expressions and use `<c:out>` only 6 times, so customer names, addresses, remarks and flash messages are output unescaped. Anyone who can enter a customer record can inject script into the other users' pages.

6. **Known-vulnerable dependencies** (all bundled in the current WAR):
   | Library | Version | Issue |
   |---|---|---|
   | Spring Boot / Framework | 2.2.6 / 5.2.5 | EOL since 2021; affected by Spring4Shell (CVE-2022-22965: JDK 9+ and WAR on Tomcat, so lower risk on Java 8 with an embedded jar, but still unpatched) and many later CVEs |
   | Tomcat embed | 9.0.33 | Dozens of CVEs since 2020 |
   | commons-fileupload | 1.3.3 | CVE-2023-24998 (DoS), and it is unused: there is no upload code |
   | commons-io | 2.4 | CVE-2021-29425 |
   | taglibs `standard` | 1.1.2 | CVE-2015-0254 (XXE) |
   | jackson-databind | 2.10.3 | Multiple deserialization CVEs |
   | snakeyaml | 1.25 | CVE-2022-1471 and others |
   | mysql-connector-java | 8.0.19 | Old; artifact renamed to `com.mysql:mysql-connector-j` |
   | Bootstrap / jQuery (vendored) | 3.x / old | Bootstrap 3 EOL 2019; jQuery < 3.5 XSS CVEs |

## 3. Correctness and data-integrity risks

- **Money is stored as `double`.** 10 model fields use `Double`/`double` for prices, amounts paid and debts, and the code compares them with `deficit != 0.0`. Floating-point rounding can create phantom ₱0.01 debts or miss real ones. Use `BigDecimal` with `DECIMAL(12,2)` columns.
- **Multi-step business operations are not atomic.** Editing an order (`OrderController` ~L800–900) updates the order, container activity, debt, and customer container totals through separate service calls, each in its own transaction. A failure halfway leaves totals and AR out of sync. This logic belongs in one `@Transactional` service method.
- **Business logic lives in controllers.** `OrderController` (1,041 lines) and `CustomerController` (639 lines) hold most of the domain rules. That makes them hard to test or change safely.
- **`ddl-auto=update` on production.** Hibernate changes the live schema silently on startup and never drops or renames anything. Schema history currently lives only in the ad-hoc files under `sql/`.
- **Legacy date types.** The code mixes `java.sql.Date`, `Timestamp` and `LocalDate`. The DB URL in `start_app.cmd` pins `serverTimezone=Asia/Manila`, but the property files don't, so dev and prod can disagree on dates near midnight.
- **Found by the Phase 1 tests** (each is pinned by a test marked KNOWN BUG):
  - Editing an order never deletes its container activity: the old borrow/buy/return rows stay
    and a second set is added for the new order id, so container history double-counts.
  - Cancelling an order doesn't undo the container balance added when the order was created.
  - `ReportUtil.applyCurrencyFormat`, used for the daily sales totals, drops the cents when a
    sum of doubles has a long decimal tail (55.10 + 60.20 shows as "115") and breaks on
    amounts of 10 million or more, which Java prints in E notation.
- **Broken link.** `users.jsp` links to `/user/delete/{username}` (a destructive GET), but no controller maps it.
- **JVM crash on record.** `hs_err_pid7944.log` shows a C2 JIT compiler crash on Java 8u372. That is another reason to move to a current LTS JDK.

## 4. Recommended modernization path

The app is small, has a clear domain, and is in daily use, so I recommend an **incremental platform upgrade with the same screens and schema**, not a rewrite. Each phase should ship on its own.

### Phase 0 – Safety net (½–1 day)
- Put the source under git (excluding `target/`, WARs and dumps) and tag the current production build.
- Fix the production profile, remove the password line from `main()`, rotate credentials, and create a dedicated MySQL user with only the privileges the app needs.
- Turn off `initialization-mode=always`.
- Automate and verify DB backups (the dumps exist; check that a restore actually works).

### Phase 1 – Tests on the money paths (2–3 days)
- Add integration tests (Spring Boot Test + Testcontainers MySQL) for order create/edit, payment, debt/AR calculation, container balances, and daily/monthly totals.
- Use an anonymized copy of a recent dump as fixture data. These tests are what make Phases 2–3 safe.

### Phase 2 – Platform upgrade (3–5 days)
- Java 8 → **21 LTS**; Spring Boot 2.2 → 2.7 (stepping stone) → **3.x**.
- `javax.*` → `jakarta.*` (persistence, servlet, validation). OpenRewrite's `UpgradeSpringBoot_3_x` recipe automates most of it.
- Rewrite `SecurityConfig` as a `SecurityFilterChain` bean (`WebSecurityConfigurerAdapter` was removed), actually register it, and add role rules (for example `/users/**`, product/price edits → `ROLE_ADMIN`).
- JSTL: `jakarta.servlet.jsp.jstl` + `glassfish-jstl` in place of `taglibs:standard` and `javax.servlet:jstl`; taglib URIs move to `jakarta.tags.core`.
- Drop commons-fileupload/commons-io; switch to `com.mysql:mysql-connector-j`; Maven wrapper 3.6.3 → 3.9.x.
- Add Flyway, with a baseline from the current schema, and set `ddl-auto=validate`.
- Externalize config with environment variables or `application-prod.properties` outside the WAR.
- Package as an executable **jar** if no external Tomcat is used. JSP inside an executable jar is unsupported, so keep WAR packaging while the views are JSP.

### Phase 3 – Code health (ongoing, 1–2 weeks)
- Move money to `BigDecimal`/`DECIMAL(12,2)` with a Flyway migration.
- Move order/payment/debt workflows from the controllers into transactional services.
- Replace `java.sql.Date`/`Timestamp` with `LocalDate`/`LocalDateTime`; use `@GetMapping`/`@PostMapping`; use constructor injection in place of field `@Autowired`; use SLF4J in place of `System.out` (15 occurrences).
- Make the destructive actions POST + CSRF only.

### Phase 4 – UI refresh (optional, 1–2 weeks)
- Either keep JSP and upgrade Bootstrap 3 → 5 (jQuery can stay for DataTables), escaping every output with `<c:out>`/`fn:escapeXml`; **or** migrate views to **Thymeleaf**, which escapes by default, removes the JSP/WAR packaging constraint, and is the path Spring recommends.
- Make the order-entry and daily-sales pages mobile-friendly for counter and delivery use.

### Not recommended now
A full SPA rewrite (React plus a REST API). It would double the moving parts for a small single-site team. The data model and workflows are the valuable parts, and they survive the phases above intact.

## 5. Effort summary

| Phase | Effort | Risk | Value |
|---|---|---|---|
| 0 Safety net | ½–1 day | Very low | High: closes data-leak and credential issues |
| 1 Tests | 2–3 days | Low | High: enables everything else |
| 2 Platform upgrade | 3–5 days | Medium | High: supported, patched stack |
| 3 Code health | 1–2 weeks | Medium | Medium-high: fixes money and consistency bugs |
| 4 UI refresh | 1–2 weeks | Low-medium | Medium: usability and XSS-safe views |
