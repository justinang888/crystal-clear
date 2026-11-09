import { useState, type FormEvent } from 'react'
import { Landmark, Receipt } from 'lucide-react'
import { Link, useNavigate } from 'react-router'

import { Field, PageHeader, PageState, Stat } from '@/components/page'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Textarea } from '@/components/ui/textarea'
import { usePage, useSave } from '@/hooks/use-page'
import type { PageData } from '@/lib/api'
import { displayDateTime, isoDate, peso, toNumber } from '@/lib/format'
import { EXPENSE_PAYMENT_TYPES, type ExpensePaymentType } from '@/lib/types'

interface ReconciliationData extends PageData {
  /** Reconciling one day or a whole month. */
  period: 'DAY' | 'MONTH'
  /** The day, or the first of the month: "2026-03-15", "2026-03-01". */
  periodStart: string
  /** "Sunday, March 15, 2026" or "March 2026". */
  periodLabel: string
  /** The first of the month the period falls in, "2026-03-01". */
  month: string
  monthLabel: string
  totalsByType: Record<ExpensePaymentType, number>
  total: number
  noteMaxLength: number
  entry: { statementAmount: number; note: string | null; updatedAt: string; updatedBy: string } | null
  /** Statement amount minus the Bank Transfer expenses on record; null until an entry is saved. */
  difference: number | null
}

/** The owner's bank reconciliation, by day or by month. The server refuses everyone else. */
export function BankReconciliationPage() {
  const { data, error } = usePage<ReconciliationData>()
  if (!data) return <PageState error={error} />
  // A fresh form for each day or month and after each save.
  return <Reconciliation key={`${data.period}:${data.periodStart}:${data.entry?.updatedAt ?? ''}`} data={data} />
}

const monthAddress = (yyyyMm: string) => `/expenses/reconciliation?d=${yyyyMm.slice(5, 7)}-${yyyyMm.slice(0, 4)}`
const dayAddress = (yyyyMmDd: string) => `/expenses/reconciliation?day=${yyyyMmDd}`

function Reconciliation({ data }: { data: ReconciliationData }) {
  const navigate = useNavigate()
  const save = useSave('/expenses/reconciliation/save')
  const byDay = data.period === 'DAY'
  const monthValue = data.month.slice(0, 7) // "2026-03"
  const dayValue = data.periodStart // "2026-03-15"
  const [amount, setAmount] = useState(data.entry ? String(data.entry.statementAmount) : '')
  const [note, setNote] = useState(data.entry?.note ?? '')
  const bankTransfers = toNumber(data.totalsByType.BANK_TRANSFER)
  const difference = data.difference === null ? null : toNumber(data.difference)
  // "in March 2026" / "on Sunday, March 15, 2026"
  const when = `${byDay ? 'on' : 'in'} ${data.periodLabel}`

  // Switching to Day opens the 1st of the month shown, or today when that's this month.
  function switchTo(view: string) {
    if (view === 'DAY' && !byDay) {
      const today = isoDate()
      navigate(dayAddress(today.startsWith(monthValue) ? today : `${monthValue}-01`))
    } else if (view === 'MONTH' && byDay) {
      navigate(monthAddress(monthValue))
    }
  }

  function submit(e: FormEvent) {
    e.preventDefault()
    save.mutate(byDay ? { day: dayValue, statementAmount: amount, note } : { month: monthValue, statementAmount: amount, note })
  }

  return (
    <>
      <PageHeader
        title="Bank reconciliation"
        description={`${data.periodLabel}: the bank statement against the expenses recorded in Aurora.`}
        actions={
          <>
            <Tabs value={data.period} onValueChange={switchTo}>
              <TabsList aria-label="Reconcile by">
                <TabsTrigger value="DAY">Day</TabsTrigger>
                <TabsTrigger value="MONTH">Month</TabsTrigger>
              </TabsList>
            </Tabs>
            {byDay ? (
              <Input
                type="date"
                value={dayValue}
                onChange={(e) => /^\d{4}-\d{2}-\d{2}$/.test(e.target.value) && navigate(dayAddress(e.target.value))}
                className="w-auto"
                aria-label="Day"
              />
            ) : (
              <Input
                type="month"
                value={monthValue}
                onChange={(e) => /^\d{4}-\d{2}$/.test(e.target.value) && navigate(monthAddress(e.target.value))}
                className="w-auto"
                aria-label="Month"
              />
            )}
            <Button variant="outline" asChild>
              <Link to="/expenses/list?d=today"><Receipt /> Expenses</Link>
            </Button>
          </>
        }
      />

      <div className="mb-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label={byDay ? 'Expenses that day' : `Expenses in ${data.monthLabel}`} value={`₱${peso(data.total)}`} />
        {EXPENSE_PAYMENT_TYPES.map((t) => (
          <Stat key={t.value} label={t.label} value={`₱${peso(data.totalsByType[t.value] ?? 0)}`} />
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)] lg:items-start">
        <Card className="gap-4 py-5">
          <CardHeader className="px-5">
            <CardTitle>From the bank statement</CardTitle>
            <CardDescription>
              Enter the bank transfers out of the account {when}, as the statement shows them.
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
