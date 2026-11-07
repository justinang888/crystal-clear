# ADR-0003: Ship the React app inside the WAR

## Status
Accepted

## Date
2026-11-07

## Context
Aurora runs on the shop's one desktop PC with `java -jar aurora.war`, which may not have internet.
The React app needs a Node build, and its files have to reach that PC.

## Decision
Maven builds the frontend with `frontend-maven-plugin`: it downloads Node 22 into `frontend/node`,
runs `npm ci` and `npm run build`, and Vite writes into `target/classes/static`, so the files are
packaged in the WAR and served by Spring Boot at `/aurora`. Fonts are bundled (fontsource), not
loaded from a CDN. `-Dskip.frontend=true` skips the step for Java-only builds.

## Alternatives Considered

### Serve the frontend separately (its own web server or a CDN)
- Pros: frontend and backend deploy independently.
- Cons: a second thing to install and run on the shop PC; a CDN needs internet.
- Rejected: one file to deploy and roll back is what the shop can manage.

### Commit the built files
- Pros: no Node at build time.
- Cons: generated files in git, easy to forget to rebuild.

## Consequences
- Deploying and rolling back stay one WAR swap (`deploy/start_app.cmd`).
- The build PC needs internet the first time (Node and npm packages); the shop PC never does.
- The app and its data share one origin, so there is no CORS to configure.
