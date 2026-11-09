-- The owner can reconcile a single day as well as a month (asked for on 2026-11-09).
-- Each entry is now for a period: 'MONTH' starting on the 1st (every entry so far), or 'DAY'.
-- Existing monthly entries keep their amounts and notes.

ALTER TABLE bank_reconciliation DROP CONSTRAINT bank_reconciliation_month_check;
ALTER TABLE bank_reconciliation DROP CONSTRAINT bank_reconciliation_pkey;
ALTER TABLE bank_reconciliation RENAME COLUMN month TO period_start;

ALTER TABLE bank_reconciliation ADD COLUMN period varchar(5) NOT NULL DEFAULT 'MONTH'
  CHECK (period IN ('DAY', 'MONTH'));
ALTER TABLE bank_reconciliation ADD PRIMARY KEY (period, period_start);
ALTER TABLE bank_reconciliation ADD CONSTRAINT bank_reconciliation_month_starts_on_first
  CHECK (period = 'DAY' OR extract(day FROM period_start) = 1);
