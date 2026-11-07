# ADR-0002: Replace the JSP pages with a React app, all at once

## Status
Accepted

## Date
2026-11-07

## Context
The screens were JSP pages with Bootstrap 3, jQuery and DataTables, and printed values without
escaping them (finding 5 in `MODERNIZATION_ASSESSMENT.md`). The assessment's Phase 4 suggested
either Bootstrap 5 on the JSPs or a move to Thymeleaf, and advised against a full React rewrite
because it would double the moving parts.

The owner asked for the UI to be rebuilt with modern libraries and chose React, replacing every
page in one change rather than screen by screen, with shadcn/ui and Tailwind CSS. A Bootstrap 5
restyle of the JSPs had been started and was discarded.

## Decision
A React 19 + TypeScript app in `frontend/`, built with Vite, styled with Tailwind CSS 4 and
shadcn/ui components, using React Router, TanStack Query and TanStack Table. Every old address
opens the same screen. The app is served from the same WAR (ADR-0003) and gets its data from
the existing controllers (ADR-0004).

## Alternatives Considered

### Bootstrap 5 on the existing JSPs
- Pros: smallest change; no build step for the frontend.
- Cons: keeps unescaped JSP output and jQuery; not what the owner asked for.
- Rejected by the owner.

### Thymeleaf
- Pros: escapes by default; the server-rendered path Spring recommends.
- Cons: still a page-by-page rewrite of every view, without the component libraries the owner wanted.

### Migrate screen by screen
- Pros: smaller steps.
- Cons: two UI stacks side by side for a while. The owner chose all at once.

## Consequences
- React escapes everything it shows, which closes finding 5.
- The build needs Node; Maven downloads its own, so only the build PC needs internet.
- Pages are tested through the controllers' JSON (`ReactAppTest`) and by hand against the demo
  app; there are no automated browser tests yet.
