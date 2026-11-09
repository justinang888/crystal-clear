-- Expense payment types and the owner's monthly bank reconciliation
-- (docs/specs/owner-role-bank-reconciliation.md). Only adds: the old WAR ignores both.

-- How an expense was paid. Every existing expense came out of the cash drawer, as the daily
-- report has always counted it, so they all become Cash.
ALTER TABLE expense ADD COLUMN payment_type varchar(20) NOT NULL DEFAULT 'CASH'
  CHECK (payment_type IN ('CASH', 'GCASH', 'BANK_TRANSFER'));

-- One entry per month, filled in by the owner: the bank transfers out of the account that month
-- as the bank statement shows them, compared with the Bank Transfer expenses on record.
CREATE TABLE bank_reconciliation (
  month            date          PRIMARY KEY CHECK (extract(day FROM month) = 1),  -- the first of the month
  statement_amount numeric(12,2) NOT NULL,
  note             varchar(1000),
  updated_at       timestamp(0)  NOT NULL DEFAULT LOCALTIMESTAMP(0),
  updated_by       varchar(45)   NOT NULL                                           -- the owner's username
);
