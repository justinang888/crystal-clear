# aurora
Water Station System

Requires Java 21. Built on Spring Boot 4 and PostgreSQL, with a React interface in `frontend/`.

## Building

```bash
./mvnw package
```

This builds the React app too (Maven downloads its own Node into `frontend/node` the first time, so
the build PC needs internet; the shop PC running the WAR does not). Add `-Dskip.frontend=true` when
only Java code changed.

## How the interface works

The React app (React 19, TypeScript, Vite, Tailwind CSS 4, shadcn/ui, TanStack Query and Table) is
served by Spring Boot at `/aurora`. Every screen keeps its old address, such as `/aurora/orders/list?d=today`:

- Opening an address in the browser gets the app (`index.html`), which shows that screen.
- The app asks the same address for JSON. The controller runs exactly as it did for the JSP pages,
  and the model it fills comes back as JSON (`config/web/ModelJsonView`), with `view` and any form `errors`.
- Saves are the same form posts as before; the controller's redirect answers with the next screen's
  data and its message. Logging in and out are posts to `/login` and `/logout`; CSRF uses the `XSRF-TOKEN` cookie.

To work on the screens with live reload, start the demo (below), then:

```bash
cd frontend && npm install && npm run dev
```

and open http://localhost:5173/aurora.

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

When a test pins behavior that is known to be wrong, mark it **KNOWN BUG** so upgrades
can't change it silently, and update it to the correct behavior when the bug is fixed.

## Trying the screens with made-up data

This starts the app on http://localhost:8081/aurora with its own throwaway PostgreSQL, filled with
made-up customers and orders (`src/test/java/com/spring/aurora/demo/DemoApplication.java`).
Log in as `demo` / `demo`; everything is gone when it stops. Build the app first (`./mvnw package`)
so the React files are in `target/classes/static`.

```bash
./mvnw spring-boot:test-run -Dskip.frontend=true -Dspring-boot.test-run.main-class=com.spring.aurora.demo.DemoApplication
```

## Text reminders

Reports > **Text Reminders** (also on Inactive Customers) lists customers whose last delivered
order is more than their usual interval plus 2 days ago, shows each message before it goes, and
sends them through an Android phone on the shop Wi-Fi. Every text is logged under **Sent texts**.
The spec is [docs/specs/sms-inactive-customers.md](docs/specs/sms-inactive-customers.md).

In the demo a fake phone accepts every text and sends nothing; numbers ending in `0000` always
fail, to show what a failure looks like.

### Setting up the shop phone

1. On an Android phone (5.0 or later) with a SIM that has load, install
   [SMS Gateway for Android](https://docs.sms-gate.app/) (open source).
2. On its Home tab, turn on **Local Server** and tap **Start Service**. It shows the phone's
   address on the Wi-Fi and a username and password.
3. Give the phone a fixed address on the router, keep it charging, and turn off battery
   optimisation for the app, or Android may stop it.
4. In the app's Settings > Messages, set a short delay between texts (for example 5 to 10
   seconds) so the network doesn't treat a batch as spam.
5. On the shop PC, as administrator, set the phone's details once, like the database password.
   Never put them in a file:

   ```bat
   setx /M AURORA_SMS_URL "http://<phone address>:8080"
   setx /M AURORA_SMS_USERNAME "<username shown in the app>"
   setx /M AURORA_SMS_PASSWORD "<password shown in the app>"
   ```

6. Restart Aurora. Before texting customers, open `http://<phone address>:8080/docs` on the PC:
   it shows the phone's API and can send a test text. Send one to a staff number, and check that
   sending is `POST /message` with `textMessage.text` and `phoneNumbers`, which is what Aurora
   sends (`AndroidSmsGateway.java`).

Without `AURORA_SMS_URL` the screens still open, but Send stays off.

## Logins: owner and bookkeepers

Every login is either a **bookkeeper** or the **owner**. Bookkeepers (every login from before
this change, stored as `ROLE_ADMIN` or `ROLE_USER`) can use every screen except **Bank
Reconciliation**. The owner (`ROLE_OWNER`) can use everything, including the monthly bank
reconciliation under Expenses. The server enforces this; the menu only hides what would be
refused. The spec is
[docs/specs/owner-role-bank-reconciliation.md](docs/specs/owner-role-bank-reconciliation.md).

To add a login, run this from the folder with `aurora.war`. It asks for the username, the role
(`owner` or `bookkeeper`) and the password, hidden as you type; it also asks for the database
password if `AURORA_DB_PASSWORD` isn't set. Only the password's hash is stored.

```bat
java -cp aurora.war -Dloader.path=WEB-INF/classes,WEB-INF/lib -Dloader.main=com.spring.aurora.tools.AddUser org.springframework.boot.loader.launch.PropertiesLauncher
```

In the demo, `demo` / `demo` is a bookkeeper and `owner` / `owner` is the owner.
