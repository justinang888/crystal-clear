# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **Owner / manager:** checks the day's sales and cash, monthly summaries, who owes money (A/R), which customers are due for a refill, and how many containers are out with customers.
- **Cashier / counter staff:** enters orders by delivery receipt (DR) number, records payments, returned containers, expenses and A/R entries, and marks orders delivered or cancelled.

Delivery staff do not use the app; deliveries are recorded at the counter.

## Product Purpose

Aurora is the back office of Crystal Clear, a single water refilling station in the Philippines. It replaces paper records for orders, payments, money owed, expenses and borrowed containers. Success means the day's sales, the cash on hand, every customer's balance and every borrowed container add up correctly, so the owner can trust the numbers without re-counting.

## Positioning

Built around how a refilling station actually works rather than a generic point-of-sale: 5-gallon **round** and **slim** containers that customers borrow, buy or return; refill-only, container-only, refill-with-container and free items; customers who reorder on a fixed interval, so the app knows who is due; and running A/R for business customers who pay later or by check with withholding tax.

## Operating Context

- One shop desktop PC at the counter, keyboard and mouse. Not used on phones or tablets.
- Runs on that PC against a local PostgreSQL database; the shop PC may not have internet, so everything the pages need ships inside the WAR.
- Orders are keyed in from paper delivery receipts; the DR number is the order's reference.
- Money is in Philippine pesos (PHP); the server runs in Asia/Manila time.
- Daily routine: enter and deliver orders, record payments and returns, then review daily sales; monthly summary and A/R at month end.

## Capabilities and Constraints

- **Customers:** Business or Residential, contact details, order interval in days, per-customer product prices, container balances (round / slim).
- **Orders:** Pending → Delivered or Cancelled; editing an order replaces it; only pending orders can be cancelled.
- **Money:** payments (cash or check, withholding tax), A/R (debt) entries linked to orders, expenses.
- **Reports:** daily sales, daily sales by month, monthly summary, A/R summary, due dates and daily due dates, inactive and monthly-active customers, container balances and history. Several have print-preview pages.
- **Stack:** Java 21, Spring Boot 4 and PostgreSQL 16, packaged as a single WAR. The interface is a React 19 + TypeScript app (Vite, Tailwind CSS 4, shadcn/ui, TanStack Query and Table) in `frontend/`, built into the WAR.
- **Language:** the interface is in English.
- **Undecided:** what a cashier may do versus the owner (all users currently have the same access); whether the printed reports are used regularly.

## Brand Commitments

- Shop name **Crystal Clear**; the system is called **Aurora**.
- No logo file and no official brand colors exist; the app's current colors and droplet icon are its own choice, not a brand requirement.

## Evidence on Hand

- Real customer and sales data lives only on the shop PC and in local database dumps that are kept out of the public repository. Never put real customer names, numbers or amounts in the repo, screenshots or samples.
- Made-up demo data for trying screens: `src/test/java/com/spring/aurora/demo/DemoApplication.java` (log in as `demo` / `demo`).
- No testimonials, case studies or other marketing material exist, and none should be invented.

## Product Principles

1. **The numbers must add up.** Totals, balances and container counts are the product; every screen that shows money or containers must be exact and easy to check against paper.
2. **Fast at the counter.** The most frequent jobs (new order, payment, return, deliver) take as few steps and as little scrolling as possible on a desktop screen.
3. **Familiar over clever.** Staff already know the screens; keep terms (DR #, round / slim, A/R, refill / container / both) and workflows recognizable when the look changes.
4. **Works offline on one PC.** No feature may depend on internet access or a hosted service.
5. **Customer data stays private.** Customer details never leave the shop PC.
