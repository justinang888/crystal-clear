# Aurora (Crystal Clear water station)

Back office for one water refilling station: orders, payments, A/R, expenses and borrowed
containers. Runs on the shop's one desktop PC. `PRODUCT.md` says who uses it and how.

## Stack

- Java 21, Spring Boot 4, Spring Security 7, Jackson 3 (`tools.jackson`), PostgreSQL 16, Flyway.
  Packaged as one WAR (`deploy/start_app.cmd` runs it on the shop PC).
- React 19 + TypeScript in `frontend/` (Vite, Tailwind CSS 4, shadcn/ui, React Router,
  TanStack Query and Table). Maven builds it into `target/classes/static`.
- The controllers still fill a `Model` and return a view name. `config/web/ModelJsonView` sends
  that model to React as JSON; `SpaPageFilter` answers page loads with `index.html`.

## Commands

| What | Command |
|---|---|
| Build the WAR (with the frontend) | `./mvnw package` |
| Java tests (start their own PostgreSQL) | `./mvnw test -Dskip.frontend=true` |
| One test | `./mvnw test -Dskip.frontend=true -Dtest=OrderLifecycleTest` |
| Frontend type check and build | `cd frontend && npm run build` |
| Frontend lint | `cd frontend && npm run lint` |
| Demo on http://localhost:8081/aurora (made-up data, `demo` / `demo`) | `./mvnw spring-boot:test-run -Dskip.frontend=true -Dspring-boot.test-run.main-class=com.spring.aurora.demo.DemoApplication` |
| Frontend dev server (start the demo first) | `cd frontend && npm run dev`, open http://localhost:5173/aurora |
| Dump converter tests | `cd migration/postgres && python -m unittest test_mysqldump_to_pg` |

`frontend/node` holds the Node that Maven downloads; put it first on `PATH` to use it for `npm`.

## Conventions

- Keep every screen's address and the controllers' model attribute names: the React pages read them.
- Saves are form posts (`application/x-www-form-urlencoded`) with the `X-XSRF-TOKEN` header.
  Anything that changes data is a POST; never accept a GET for it (Spring Security skips the CSRF check on GET).
- A bug fix starts with a test that fails without it. Tests that pin known-wrong behavior say **KNOWN BUG**.
- Money and dates are shown in pesos and Asia/Manila time (`frontend/src/lib/format.ts`).
- Commit messages: `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`, with a body that says why.
- Decisions that are expensive to reverse get a record in `docs/decisions/`.

## Boundaries

- **Never** put real customer data (names, numbers, addresses, amounts), database dumps or passwords in
  this public repository, in screenshots or in samples. Use the demo data.
  Real data lives only in `local/` and `AURORA-*/` (both gitignored) and on the shop PC.
- **Never** touch the shop's live system or database.
- Database passwords come from the environment (`AURORA_DB_PASSWORD`) or are typed by the user
  at a prompt. Never write, read back or echo one.
- No hard deletes: use the Recycle Bin or git, so anything can be restored.
- Commit, merge and push only when the user asks. `.cursor/` is the user's own; don't commit it.
- Ask first before changing how login works, the database schema, or the cutover steps.
