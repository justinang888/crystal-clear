# Spec: Text reminders to inactive customers

Status: **approved 2026-11-08**, including the database change below. Questions the owner
hasn't answered yet are built with the defaults listed under "Open questions", each easy to
change later.

## Objective

Customers who stop ordering are easy to lose track of. The owner wants to send them a short
text ("it's been a while, reply or call to order") from Aurora, without retyping numbers into
a phone.

**User:** the owner or counter staff, at the shop PC.

**Success looks like:** from the Inactive Customers screen, the user picks who to text, sees
each exact message before it goes, clicks Send once, and later can see who was texted, when,
and whether the phone accepted each message. Nobody is texted twice in a short time, and a
customer who asked not to be texted never is.

### How texts leave the shop

Through an Android phone with its own SIM, on the shop's Wi-Fi, running
[SMS Gateway for Android](https://docs.sms-gate.app/) (open source, Apache 2.0) in
**local server mode**: the phone serves a small web API on the local network and sends each
text through its SIM. No internet and no outside SMS company is involved, so the PRODUCT.md
principles "works offline on one PC" and "customer data stays private" still hold: numbers go
only to the phone network, the same as texting by hand.

From the app's docs (https://docs.sms-gate.app/getting-started/local-server/):

- Turn on "Local Server" on the app's Home tab and tap "Start Service". The app shows the
  phone's local IP and a username and password for basic authentication.
- Send: `POST http://<phone-ip>:8080/message` with basic auth and
  `{"textMessage": {"text": "..."}, "phoneNumbers": ["+639..."]}`.
- Message states: Pending, Processed, Sent, Delivered, Failed.
- The phone works without internet in this mode. Android 5.0 or later.
- Delays and per-minute, per-hour and per-day limits are set on the phone (Settings > Messages).

**To verify on the real phone before coding the client:** the exact local send path and its
response body (the docs show `/message` locally but `/3rdparty/v1/messages` for the cloud
API), and the status-lookup path. The phone serves its own API reference at
`http://<phone-ip>:8080/docs`.

## Who counts as inactive for texting (decided)

A customer is offered for a text when **their last delivered order is more than their usual
order interval plus 2 days ago**. Customers who have never had an order delivered are left
out.

- "Last delivered order" is the newest order with status Delivered; pending and cancelled
  orders don't count.
- The interval is the customer's "orders every N days" setting. Example, on 20 November:
  a 7-day customer whose last delivery was 10 November (10 days ago, more than 7 + 2) is
  included; one whose last delivery was 12 November (8 days ago) is not yet.
- `{days}` in the message is the days since that last delivered order.
- Assumption to confirm: a customer whose interval is 0 or blank is treated as 30 days
  (so 32 days with the 2-day grace).

The existing Inactive Customers report keeps its current rule (no order in 30 days) unless the
owner decides otherwise; see the open questions.

## What it does

1. **Inactive Customers** gets a **Text overdue customers** button that opens the compose
   screen with everyone who matches the rule above.
2. **Compose screen** (`/sms/compose`): each matching customer with a checkbox (ticked by
   default) and a message template with blanks, for example
   `Hi {name} po, it's been {days} days since your last refill from Aurora Crystal Clear at
   Aurora Blvd., Cubao. Call or text us to order.`
   - Blanks: `{name}` (customer name, or contact person when set), `{days}` (days since the
     last delivered order).
   - A preview of every actual message, with its number.
   - Customers that won't be texted are listed with the reason: no usable mobile number,
     opted out, or texted in the last 14 days.
   - A character count and the number of texts each message will take (160 characters per
     text, but only 70 if the message has a character outside the GSM alphabet, such as ₱
     or an emoji; the screen warns when that happens).
3. **Send** (a POST with the CSRF token): Aurora sends one request per customer, one after
   another, and records each result. One failure doesn't stop the rest. The result screen
   shows sent, failed and skipped, by customer.
4. **Text messages log** (`/sms/list`): everything sent, newest first: when, customer,
   number, the text, and the state the phone reported.
5. **Opt-out:** a "Don't send texts" switch on the customer's page, saved by its own POST.
   Replies like STOP arrive on the phone, not in Aurora, so staff set it by hand in this
   version. (Not a field in the customer edit form: that form and the container balances form
   post a whole customer, and a field missing from either would reset the opt-out.)

### Phone numbers

Numbers are stored as typed (for example `0917 000 0003`). Before sending they're turned into
`+639XXXXXXXXX`: spaces, dashes and brackets removed, then `09XXXXXXXXX`, `9XXXXXXXXX`,
`639XXXXXXXXX` and `+639XXXXXXXXX` accepted. Anything else (landlines, short or long numbers)
is skipped and shown as "no usable mobile number". The main number is used, else the
alternate. Stored numbers are never changed.

## Database changes (needs approval)

A Flyway migration `V2__text_messages.sql`:

```sql
ALTER TABLE customer ADD COLUMN sms_opt_out boolean NOT NULL DEFAULT false;

CREATE TABLE sms_message (
    sms_message_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    customer_id    integer     NOT NULL REFERENCES customer (customer_id),
    phone_number   varchar(20) NOT NULL,          -- as sent, +639...
    body           text        NOT NULL,
    state          varchar(20) NOT NULL,          -- Pending, Processed, Sent, Delivered, Failed, Error
    gateway_id     varchar(64),                   -- the phone's message id, when it gave one
    error          text,                          -- why it failed, when it did
    created_at     timestamp   NOT NULL DEFAULT now(),
    created_by     varchar(50) NOT NULL           -- the Aurora user who clicked Send
);
CREATE INDEX sms_message_customer_created_idx ON sms_message (customer_id, created_at);
```

The names and key style follow `V1__initial_schema.sql` (`customer.customer_id` is an
`integer` identity). Nothing existing is changed or removed, so the cutover and a rollback to the old WAR are
unaffected: the old app ignores the new column and table.

## Configuration

Like the database password, the phone's address and login come from the environment and are
never written to the repo, a file or a log:

| Variable | Example | Notes |
|---|---|---|
| `AURORA_SMS_URL` | `http://192.168.1.50:8080` | The phone's address on the shop Wi-Fi. Give the phone a fixed IP on the router. |
| `AURORA_SMS_USERNAME` | (shown in the app) | |
| `AURORA_SMS_PASSWORD` | (shown in the app) | Set once as a Windows system variable (`setx /M`), like `AURORA_DB_PASSWORD`; `start_app.cmd` passes it on unchanged. |

When `AURORA_SMS_URL` isn't set, the texting screens still open but Send is disabled with
"Texting isn't set up on this PC". The demo app uses a fake phone that records messages and
sends nothing.

## Tech and structure

Java 21 and Spring Boot 4 (Spring's `RestClient` for the phone's API, so no new dependency),
React pages in `frontend/src/pages/`. Following the existing pattern: controllers fill a
`Model`, saves are form posts.

```
src/main/resources/db/migration/V2__text_messages.sql
src/main/java/com/spring/aurora/sms/
    SmsGateway.java            interface: send(number, text) -> result
    AndroidSmsGateway.java     the phone's local API over RestClient
    PhoneNumbers.java          09xx... -> +639..., or empty when unusable
    SmsMessage*.java           entity and repository
    SmsService.java            who to text, the template, cooldown, opt-out, logging
src/main/java/com/spring/aurora/controller/SmsController.java   /sms/compose, /sms/send, /sms/list
frontend/src/pages/sms.tsx      compose, results, log
src/test/java/com/spring/aurora/ SmsTest.java, sms/PhoneNumbersTest.java
src/test/java/com/spring/aurora/demo/FakeSmsGateway.java
```

## Testing

- `PhoneNumbersTest`: every accepted shape, landlines and junk rejected.
- `SmsTest` (PostgreSQL, through the controllers, with a fake gateway): who is selected, at
  the edges of the rule (interval + 2 days exactly, one day over, pending and cancelled orders
  ignored, never-delivered customers left out, interval 0);
  opted-out, cooldown and no-number customers skipped with the right reason; the filled-in
  template; one failure doesn't stop the rest; every attempt logged with state and user;
  Send only by POST with the CSRF token; Send disabled when the phone isn't configured.
- The client against the real phone's request and response shapes, once verified on the
  phone (a test against a local stub HTTP server).
- Manual: send to one staff number from the demo with a real phone before the shop uses it.

Commands as in CLAUDE.md: `./mvnw test -Dskip.frontend=true`, `cd frontend && npm run build`,
`npm run lint`.

## Boundaries

- **Always:** the preview shows exactly what is sent; every attempt is logged; opted-out
  customers are never texted; POST + CSRF for sending.
- **Ask first:** the migration above; sending on a schedule; texting anyone who isn't on the
  inactive list.
- **Never:** real numbers, names or message logs in the repo, tests, screenshots or demo data;
  the phone's password in any file or log; an outside SMS company; sending without a person
  clicking Send.

## Success criteria

1. With the demo and the fake phone: the compose screen lists exactly the customers whose last
   delivered order is more than their interval plus 2 days ago, and no one who never had a
   delivery. Of 3 picked, one opted out and one without a mobile number, Send texts exactly
   one, logs it, and lists the other two with reasons.
2. Sending again within 14 days skips the customer, with the reason.
3. With a real phone on the same Wi-Fi: a test text to a staff number arrives, and its log
   row shows the state the phone reported.
4. With the phone off: every message is logged as Error with a readable reason, and the
   screen says the phone couldn't be reached.
5. All Java tests, lint and build pass; CI is green.

## Not in this version

Receiving replies or STOP automatically (the phone's webhooks need HTTPS on the local
network), scheduled or automatic sending, delivery reports, texting other groups (for example
customers due today), Tagalog templates, and the cloud mode of the gateway app.

## Open questions for the owner

Decided: the inactive rule (above) and the database change (approved). Still open, with the
default this version is built with:

1. **Cooldown:** don't text the same customer again within 14 days? *Default: 14 days.*
2. **Interval 0 or blank:** treat as 30 days, or leave those customers out? *Default: 30
   days.*
3. **The Inactive Customers report:** keep its "no order in 30 days" rule, or switch it to the
   texting rule too so the two lists match? *Default: unchanged.*
4. **Consent:** *Default: not tracked in Aurora; the shop handles it.* the Data Privacy Act generally expects customers to have agreed to these
   texts. How will the shop get and record that? (For example a line on the delivery
   receipt; this spec doesn't track consent.) Worth confirming with someone who knows the law.
5. **The message wording**, and whether it should be in English, Tagalog or both. *Default:
   the English example above, editable on the compose screen before each send.*
