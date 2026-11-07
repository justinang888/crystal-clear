# ADR-0004: Send the controllers' models as JSON instead of writing a REST API

## Status
Accepted

## Date
2026-11-07

## Context
The order, payment, debt and container workflows live in the Spring MVC controllers, which fill
a `Model` and return a JSP view name. Those workflows are the valuable, tested part of the app
(`OrderLifecycleTest` and the other integration tests). The React app (ADR-0002) needs their data.

## Decision
Keep the controllers as they are and change only how their result is sent:

- `config/web/WebConfig` registers a view resolver that turns every view name into
  `ModelJsonView`, which writes the model as JSON with the chosen `view` name and any validation
  `errors` by field. `redirect:` still redirects.
- `SpaPageFilter` answers page loads (a GET that accepts HTML) with `index.html`; the app then asks
  the same address for JSON.
- Saves stay ordinary form posts. A save that worked redirects, and the app follows the redirect
  to the next screen's data and its flash message.
- Login stays Spring Security form login against the users table, answering 200/401. CSRF uses
  Spring Security's SPA mode: an `XSRF-TOKEN` cookie sent back in the `X-XSRF-TOKEN` header.

## Alternatives Considered

### A separate REST API (`/api/...`) for the React app
- Pros: a designed, documented contract; JSON in and out.
- Cons: every workflow rewritten or duplicated, with new tests, before the UI could ship.
- Rejected: it would change the logic this phase was meant to leave alone.

## Consequences
- No workflow was rewritten for the new UI, so the existing tests still cover them. (Two bug fixes
  in `OrderController` landed alongside: the price overwrite and POST-only deliver/cancel.)
- The JSON contract is the controllers' model attribute names, which were never designed as an
  API. Renaming one breaks a screen, so they are typed in `frontend/src/lib/types.ts`.
- The model is sent whole, so nothing sensitive may be put in a model (no user records or
  password hashes).
- Anything that changes data must be a POST: a GET is not checked for the CSRF token.
