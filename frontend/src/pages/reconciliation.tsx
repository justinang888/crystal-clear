import { useState, type FormEvent } from 'react'
import { Landmark, Receipt } from 'lucide-react'
import { Link, useNavigate } from 'react-router'

import { Field, PageHeader, PageState, Stat } from '@/components/page'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { usePage, useSave } from '@/hooks/use-page'
import type { PageData } from '@/lib/api'
import { displayDateTime, peso, toNumber } from '@/lib/format'
import { EXPENSE_PAYMENT_TYPES, type ExpensePaymentType } from '@/lib/types'

interface ReconciliationData extends PageData {
  /** The first of the month, "2026-03-01". */
  month: string
  monthLabel: string
  totalsByType: Record<ExpensePaymentType, number>
  total: number
  noteMaxLength: number
  entry: { statementAmount: number; note: string | null; updatedAt: string; updatedBy: string } | null
  /** Statement amount minus the Bank Transfer expenses on record; null until an entry is saved. */
  difference: number | null
}

/** The owner's monthly bank reconciliation. The server refuses everyone else. */
export function BankReconciliationPage() {
  const { data, error } = usePage<ReconciliationData>()
  if (!data) return <PageState error={error} />
  // A fresh form for each month and after each save.
  return <Reconciliation key={`${data.month}:${data.entry?.updatedAt ?? ''}`} data={data} />
}

function Reconciliation({ data }: { data: ReconciliationData }) {
  const navigate = useNavigate()
  const save = useSave('/expenses/reconciliation/save')
  const monthValue = data.month.slice(0, 7) // "2026-03"
  const [amount, setAmount] = useState(data.entry ? String(data.entry.statementAmount) : '')
  const [note, setNote] = useState(data.entry?.note ?? '')
  const bankTransfers = toNumber(data.totalsByType.BANK_TRANSFER)
  const difference = data.difference === null ? null : toNumber(data.difference)

  function pickMonth(value: string) {
    if (/^\d{4}-\d{2}$/.test(value)) navigate(`/expenses/reconciliation?d=${value.slice(5, 7)}-${value.slice(0, 4)}`)
  }

  function submit(e: FormEvent) {
    e.preventDefault()
    save.mutate({ month: monthValue, statementAmount: amount, note })
  }

  return (
    <>
      <PageHeader
        title="Bank reconciliation"
        description={`${data.monthLabel}: the bank statement against the expenses recorded in Aurora.`}
        actions={
          <>
            <Input
              type="month"
              value={monthValue}
              onChange={(e) => pickMonth(e.target.value)}
              className="w-auto"
              aria-label="Month"
            />
            <Button variant="outline" asChild>
              <Link to="/expenses/list?d=today"><Receipt /> Expenses</Link>
            </Button>
          </>
        }
      />

      <div className="mb-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label={`Expenses in ${data.monthLabel}`} value={`₱${peso(data.total)}`} />
        {EXPENSE_PAYMENT_TYPES.map((t) => (
          <Stat key={t.value} label={t.label} value={`₱${peso(data.totalsByType[t.value] ?? 0)}`} />
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)] lg:items-start">
        <Card className="gap-4 py-5">
          <CardHeader className="px-5">
            <CardTitle>From the bank statement</CardTitle>
            <CardDescription>
              Enter the bank transfers out of the account in {data.monthLabel}, as the statement shows them.
            </CardDescription>
          </CardHeader>
          <CardContent className="px-5">
            <form onSubmit={submit} className="grid gap-5">
              <Field label="Bank statement amount (PHP)" htmlFor="statementAmount">
                <Input
                  id="statementAmount"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  inputMode="decimal"
                  placeholder="0.00"
                  required
                  className="max-w-48"
                />
              </Field>
              <Field label="Note" htmlFor="note" hint={`${note.length} of ${data.noteMaxLength} characters`}>
                <Textarea
                  id="note"
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  rows={4}
                  maxLength={data.noteMaxLength}
                  placeholder="For example: bank charges, transfers not yet recorded, statement reference."
                />
              </Field>
              <div className="flex flex-wrap items-center gap-3">
                <Button type="submit" disabled={save.isPending || amount.trim() === ''}>
                  <Landmark /> {data.entry ? 'Update reconciliation' : 'Save reconciliation'}
                </Button>
                {data.entry && (
                  <span className="text-sm text-muted-foreground">
                    Last saved by {data.entry.updatedBy}, {displayDateTime(data.entry.updatedAt)}
                  </span>
                )}
              </div>
            </form>
          </CardContent>
        </Card>

        <Card className="gap-3 py-5">
          <CardHeader className="px-5">
            <CardTitle>Difference</CardTitle>
            <CardDescription>The statement amount minus the Bank Transfer expenses recorded in Aurora.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 px-5">
            {difference === null ? (
              <p className="text-sm text-muted-foreground">Save the statement amount to see the difference.</p>
            ) : (
              <>
                <p className="text-3xl font-semibold tracking-tight tabular">
                  {difference < 0 ? '−' : ''}₱{peso(Math.abs(difference))}
                </p>
                <p className="text-sm text-muted-foreground">
                  {difference === 0
                    ? 'The statement matches the Bank Transfer expenses recorded in Aurora.'
                    : difference > 0
                      ? `The bank shows ₱${peso(difference)} more going out than Aurora has as Bank Transfer expenses. Look for transfers or bank charges that weren't recorded.`
                      : `Aurora has ₱${peso(-difference)} more Bank Transfer expenses than the statement shows. Look for expenses marked Bank Transfer that were paid another way, or entered twice.`}
                </p>
                <p className="text-sm text-muted-foreground">
                  Statement ₱{peso(data.entry?.statementAmount)} · Bank Transfer expenses ₱{peso(bankTransfers)}
                </p>
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  )
}
