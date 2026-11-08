# Implementation plan: text reminders to inactive customers

Spec: [docs/specs/sms-inactive-customers.md](../docs/specs/sms-inactive-customers.md) (approved 2026-11-08).

## Overview

Staff open a compose screen listing customers whose last delivered order is more than their
order interval plus 2 days ago, check the messages, and click Send. Aurora hands each text to an
Android phone on the shop Wi-Fi (SMS Gateway for Android, local mode), logs every attempt, and
skips customers who opted out, were texted in the last 14 days, or have no usable mobile number.

## Architecture decisions

- **One interface, two phones.** `SmsGateway` with `AndroidSmsGateway` (real, Spring `RestClient`,
  no new dependency) and `FakeSmsGateway` (demo and tests). The real one is used only when
  `AURORA_SMS_URL` is set; otherwise Send is disabled.
- **Selection in Java, not SQL.** One query loads each customer's last delivered order date; the
  rule (interval + 2 days, interval 0/blank -> 30) is a pure function with unit tests, like
  `SalesTrend`.
- **Sending is synchronous and sequential.** At shop scale (tens of customers) one request per
  customer in the POST is fine; the phone applies its own delays and limits. No queue or
  scheduler.
- **The log is the source of truth** for cooldown and history: `sms_message`, one row per attempt.
- **The phone's exact local API** (send path, response, status path) is coded from the docs and
  kept in one class, then checked on the real phone (Task 7) before the shop uses it.

## Tasks

### Phase 1: Foundation

- [ ] **Task 1: Schema.** `V2__text_messages.sql`: `customer.sms_opt_out`, `sms_message` table and
  index; JPA mapping for the new column and entity + repository.
  - Acceptance: Flyway applies V2 on an empty database and on one at V1; existing tests pass;
    the old WAR's tables are untouched.
  - Verify: `./mvnw test -Dskip.frontend=true`
  - Files: `db/migration/V2__text_messages.sql`, `model/Customer.java`, `sms/SmsMessage.java`,
    `sms/SmsMessageRepository.java`
- [ ] **Task 2: Phone numbers.** `PhoneNumbers.toMobile(String)` -> `Optional<String>` (+639...).
  - Acceptance: every accepted shape in the spec; landlines, short, long and junk rejected.
  - Verify: `-Dtest=PhoneNumbersTest`
  - Files: `sms/PhoneNumbers.java`, `PhoneNumbersTest.java`
- [ ] **Task 3: Who is overdue.** Last delivered order per customer (one query) and the pure rule.
  - Acceptance: interval + 2 exactly is not overdue, one day more is; pending/cancelled ignored;
    never-delivered left out; interval 0/blank uses 30.
  - Verify: `-Dtest=OverdueCustomersTest`
  - Files: `dao/OrderDao.java`, `sms/OverdueCustomers.java`, test

### Checkpoint A: tests pass, build clean.

### Phase 2: Sending

- [ ] **Task 4: Gateway.** `SmsGateway`, `FakeSmsGateway`, `AndroidSmsGateway` (basic auth,
  `POST {url}/message`, timeouts, errors turned into a readable reason; password never logged).
  - Acceptance: against a local stub HTTP server: the request shape, a success, a 401, a refused
    connection, a timeout.
  - Verify: `-Dtest=AndroidSmsGatewayTest`
  - Files: `sms/SmsGateway.java`, `sms/AndroidSmsGateway.java`, `demo/FakeSmsGateway.java`,
    config wiring, test
- [ ] **Task 5: Service and controller.** `SmsService` (candidates with skip reasons, template
  filling, cooldown, opt-out, logging with the user's name) and `SmsController`:
  `GET /sms/compose`, `POST /sms/send`, `GET /sms/list`; opt-out on the customer form.
  - Acceptance: spec success criteria 1, 2 and 4 through the controllers with the fake gateway;
    Send is POST + CSRF only; disabled when the phone isn't configured.
  - Verify: `-Dtest=SmsTest`, full suite
  - Files: `sms/SmsService.java`, `controller/SmsController.java`, `controller/CustomerController.java`, `SmsTest.java`

### Checkpoint B: all Java tests pass; spec criteria 1, 2, 4 covered by tests.

### Phase 3: Screens

- [ ] **Task 6: React pages.** Compose (checkboxes, editable template, per-customer preview,
  skipped list with reasons, character and text count with the GSM-alphabet warning), send
  result, log page, "Don't send texts" on the customer form, link from Inactive Customers,
  menu entry, demo data with a few overdue customers and a fake phone.
  - Acceptance: works end to end in the demo; lint and build clean.
  - Verify: `npm run lint`, `npm run build`, by hand in the demo
  - Files: `pages/sms.tsx`, `App.tsx`, `lib/types.ts`, `pages/customers.tsx`, `app-layout.tsx`,
    `demo/DemoApplication.java`

### Checkpoint C: demo walk-through of spec criterion 1; review with the owner.

### Phase 4: The real phone

- [ ] **Task 7: Check against the phone.** With the owner's phone: confirm the local send path
  and response on `http://<phone-ip>:8080/docs`, adjust `AndroidSmsGateway` if they differ,
  send one test text to a staff number (spec criterion 3); document the phone setup and the
  `AURORA_SMS_*` variables in the README, set once with `setx /M` like the database password.
  - Verify: a text arrives; its log row shows the phone's state
  - Files: `sms/AndroidSmsGateway.java`, `README.md`

### Checkpoint D: complete. CI green; ready to merge.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| The phone's local API differs from the docs | Med | One class to change; Task 7 checks on the device before use |
| Phone asleep or app killed by battery saver | Med | Clear "couldn't reach the phone" errors; README setup notes (keep charging, no battery optimisation) |
| Phone's IP changes | Low | Fixed IP on the router (README) |
| Carrier blocks many texts at once | Low | Phone app's own delay and rate settings; cooldown |
| Customers didn't agree to texts | Med (legal) | Opt-out flag; consent is the shop's process (spec open question) |

## Open questions

See the spec. Built with its defaults: 14-day cooldown, blank interval = 30 days, Inactive
Customers report unchanged, consent outside Aurora, English template editable per send.
