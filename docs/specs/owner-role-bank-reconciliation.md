# Spec: Owner login, bank reconciliation and expense payment types

Status: **approved 2026-11-09**, including the database change and the new owner login.

## Objective

1. **Expenses record how they were paid**: Cash, GCash or Bank Transfer. Only cash expenses come
   out of the drawer, so only they lower the daily report's net cash.
2. **A new Owner login** that can do everything the current logins can, plus **bank
   reconciliation**: once a month, the owner enters the amount on the bank statement and a note,
   and Aurora shows it beside that month's expenses by payment type and the difference from the
   Bank Transfer expenses Aurora has on record.
3. **Every existing login becomes a bookkeeper**: no change to anything they do today, but bank
   reconciliation is hidden from them and refused if they open its address.

Decided with the owner on 2026-11-09: the new user has everything plus reconciliation; all
current logins are bookkeepers; reconciliation is a monthly bank amount plus a note; old expenses
become Cash; only Cash expenses lower net cash.

## Who can do what

| | Bookkeeper (every login today) | Owner (new) |
|---|---|---|
| Every existing screen, including recording expenses | Yes | Yes |
| Expense payment type (Cash / GCash / Bank Transfer) | Yes | Yes |
| Bank reconciliation: see it, fill it in | No: not in the menu, its address answers 403 | Yes |

- Roles live in the existing `users.role` column. The owner's role is `ROLE_OWNER`; any other
  value (today's `ROLE_ADMIN`, `ROLE_USER`) is a bookkeeper. **Existing rows are not changed.**
- The check is on the server (Spring Security on the reconciliation addresses), not only in the
  menu. The app learns whether to show the menu entry from the session endpoint, which gains a
  `canReconcile` flag next to the username.

### Adding the owner login

No password is ever written into the repo, a migration or a file. A small command-line tool in
the WAR, like the existing `HashPassword`, adds a user: it asks for the username, the role
(owner or bookkeeper) and the password (hidden as it is typed), and inserts the row using the
same `AURORA_DB_*` settings the app uses. The README gets the exact command. The owner runs it
once on the shop PC (and on the trial PC).

## Expense payment type

- A required choice on the New expense form: **Cash** (preselected), **GCash**, **Bank
  Transfer**.
- Shown as a column on the expenses list, with the day's total split by type.
- **Daily sales:** net cash = cash received minus **cash** expenses. The report still shows the
  day's total expenses, now with the cash part and the rest listed separately.
- **Monthly summary:** unchanged (it shows all expenses).
- Existing expenses become Cash, which keeps every past report's numbers exactly as they are.

## Bank reconciliation (owner only)

A screen at `/expenses/reconciliation?d=MM-yyyy` (default: this month), linked from Expenses for
the owner:

- **This month's expenses:** total, and the totals for Cash, GCash and Bank Transfer.
- **The owner's entry:** *Bank statement amount* (the bank transfers out of the account that month,
  as the statement shows them) and a *Note* (free text, up to 1,000 characters).
- **Difference:** statement amount minus the Bank Transfer expenses on record, so a missing or
  mistyped bank-transfer expense shows up as a gap.
- Saving is a POST with the CSRF token, may be repeated (it replaces the month's entry), and
  records who saved it and when, shown under the form.
- A month with no entry shows the totals and an empty form.

## Database changes (needs approval)

A Flyway migration `V3__expense_payment_and_reconciliation.sql`:

```sql
ALTER TABLE expense ADD COLUMN payment_type varchar(20) NOT NULL DEFAULT 'CASH'
  CHECK (payment_type IN ('CASH', 'GCASH', 'BANK_TRANSFER'));

CREATE TABLE bank_reconciliation (
  month            date          PRIMARY KEY,   -- the first day of the month
  statement_amount numeric(12,2) NOT NULL,
  note             varchar(1000),
  updated_at       timestamp(0)  NOT NULL DEFAULT LOCALTIMESTAMP(0),
  updated_by       varchar(45)   NOT NULL       -- the owner's username
);
```

Only adds. The `DEFAULT 'CASH'` marks every existing expense Cash. The old WAR ignores both, so
rolling back is still a WAR swap (expenses added there are saved as Cash).

## Testing

Tests first, through the controllers against PostgreSQL, as the existing suites do:

- Payment type: saved from the form, required, only the three values; old rows read as Cash.
- Daily sales: a GCash and a Bank Transfer expense don't lower net cash; a Cash one does; the
  existing daily-sales tests keep passing unchanged.
- Roles: a bookkeeper (`ROLE_ADMIN`, `ROLE_USER`) gets 403 on the reconciliation page and its
  save, and `canReconcile: false`; an owner can open, save and change the month's entry and gets
  `canReconcile: true`; every other screen works for both.
- Reconciliation: month totals by type; difference; who saved and when; a month with no entry.
- The add-user tool: inserts the row with a BCrypt hash; refuses an existing username; never
  prints the password.
- Demo: an owner login (`owner` / `owner`, demo only) alongside `demo` / `demo`, and demo expenses
  of all three types.

## Boundaries

- **Always:** permission checked on the server; tests first; existing report numbers unchanged
  for existing data.
- **Ask first:** changing what bookkeepers can do beyond hiding reconciliation; a user-management
  screen.
- **Never:** a password in the repo, a migration, a log or a test fixture other than the demo's
  throwaway logins; changing existing `users.role` values.

## Not in this version

A screen to manage logins (the command-line tool covers adding one), importing bank statements,
reconciling individual expenses, GCash reconciliation, and audit history beyond "last saved by".

## Open questions

1. **What goes in "Bank statement amount"?** Built as *the total of bank transfers out of the
   account that month*, so it can be compared with Bank Transfer expenses. If the owner instead
   wants the account's closing balance, the screen shows it with the note but no difference.
2. **Is the new user's username decided?** The tool asks for it, so this isn't needed to build.
