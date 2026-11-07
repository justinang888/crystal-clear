# Aurora frontend

The React app for Crystal Clear's back office. See the repository README for how it is built into
the WAR and how it gets its data from the Spring controllers.

- `src/pages/` – one file per area (customers, orders, money, catalog), each screen at its old address
- `src/components/` – the app shell, data table and shared pieces; `ui/` holds the shadcn/ui components
- `src/lib/api.ts` – loading screen data and posting forms; `format.ts` – pesos and Manila time
