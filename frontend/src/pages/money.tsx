import { useMemo, type FormEvent } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { Plus } from 'lucide-react'
import { Link } from 'react-router'

import { DateFilter } from '@/components/bits'
import { DataTable } from '@/components/data-table'
import { Field, PageHeader, PageState, PrintButton, Stat } from '@/components/page'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { TableCell, TableRow } from '@/components/ui/table'
import { useFormValues } from '@/hooks/use-form'
import { fieldErrors, usePage, useSave } from '@/hooks/use-page'
import type { PageData } from '@/lib/api'
import { displayDate, isoDate, peso, toNumber } from '@/lib/format'
import type { Customer, Debt, Expense, Payment } from '@/lib/types'
import { FormButtons } from './customers'

const moneyCell = { cell: ({ getValue }: { getValue: () => unknown }) => peso(getValue()), meta: { numeric: true } }

function customerHome(customer: Customer) {
  return `/customers/view?customerId=${customer.customerId}`
}

// ---------- Payments ----------

export function PaymentFormPage() {
  const { data, error } = usePage<PageData & { payment: Payment; customer: Customer }>()
  const save = useSave('/payments/save', { formView: 'new-payment' })
  const initial = useMemo(() => (data ? { ...data.payment, createdAt: isoDate(data.payment.createdAt) } : undefined), [data])
  const { values, setValue, bind } = useFormValues<Record<string, unknown>>(initial as Record<string, unknown> | undefined)
  const errors = fieldErrors(save)
  if (!data || !values) return <PageState error={error} />

  function submit(e: FormEvent) {
    e.preventDefault()
    const v = values!
    save.mutate({
      paymentId: v.paymentId as number, customerId: v.customerId as number, createdAt: v.createdAt as string,
      remarks: v.remarks as string, amount: v.amount as string, withholdingTax: v.withholdingTax as string, paymentType: v.paymentType as string,
    })
  }

  return (
    <div className="max-w-2xl">
      <PageHeader title="New payment" description={data.customer.name} />
      <Card>
        <CardContent>
          <form onSubmit={submit} className="grid gap-5 sm:grid-cols-2">
            <Field label="Date" htmlFor="createdAt" error={errors.createdAt}>
              <Input {...bind('createdAt')} type="date" required />
            </Field>
            <Field label="Paid by" error={errors.paymentType}>
              <RadioGroup value={String(values.paymentType ?? 'CASH')} onValueChange={(v) => setValue('paymentType', v)} className="flex h-8 items-center gap-6">
                {[['CASH', 'Cash'], ['CHECK', 'Check']].map(([value, label]) => (
                  <div key={value} className="flex items-center gap-2">
                    <RadioGroupItem value={value} id={`type-${value}`} />
                    <Label htmlFor={`type-${value}`} className="font-normal">{label}</Label>
                  </div>
                ))}
              </RadioGroup>
            </Field>
            <Field label="Amount (PHP)" htmlFor="amount" error={errors.amount}>
              <Input {...bind('amount')} inputMode="decimal" placeholder="0.00" required autoFocus />
            </Field>
            <Field label="Withholding tax (PHP)" htmlFor="withholdingTax" error={errors.withholdingTax}>
              <Input {...bind('withholdingTax')} inputMode="decimal" placeholder="0.00" />
            </Field>
            <Field label="Remarks" htmlFor="remarks" error={errors.remarks} className="sm:col-span-2" hint="For example the DR or check number.">
              <Input {...bind('remarks')} />
            </Field>
            <FormButtons busy={save.isPending} back={customerHome(data.customer)} label="Save payment" />
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

export function PaymentsPage() {
  const { data, error } = usePage<PageData & { payments: Payment[]; paymentsTotal: number; customer: Customer }>()
  const columns = useMemo<ColumnDef<Payment>[]>(
    () => [
      { accessorKey: 'createdAt', header: 'Date', cell: ({ getValue }) => displayDate(getValue<string>()) },
      { accessorKey: 'remarks', header: 'Remarks' },
      { accessorKey: 'paymentType', header: 'Type', cell: ({ getValue }) => <Badge variant="outline">{getValue<string>()}</Badge> },
      { accessorKey: 'amount', header: 'Amount', ...moneyCell },
      { accessorKey: 'withholdingTax', header: 'Withholding tax', ...moneyCell },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Payments"
        description={<Link to={customerHome(data.customer)} className="text-primary hover:underline">{data.customer.name}</Link>}
        actions={
          <>
            <PrintButton />
            <Button asChild><Link to={`/payments/new?cid=${data.customer.customerId}`}><Plus /> New payment</Link></Button>
          </>
        }
      />
      <div className="mb-6 max-w-xs"><Stat label="Total paid" value={peso(data.paymentsTotal)} /></div>
      <DataTable columns={columns} data={data.payments} initialSort={[{ id: 'createdAt', desc: true }]} pageSize={25} />
    </>
  )
}

// ---------- A/R entries ----------

export function DebtFormPage() {
  const { data, error } = usePage<PageData & { debt: Debt; customer: Customer }>()
  const save = useSave('/debts/save', { formView: 'new-debt' })
  const initial = useMemo(() => (data ? { ...data.debt, createdAt: isoDate(data.debt.createdAt), amount: data.debt.amount || '' } : undefined), [data])
  const { values, bind } = useFormValues<Record<string, unknown>>(initial as Record<string, unknown> | undefined)
  const errors = fieldErrors(save)
  if (!data || !values) return <PageState error={error} />

  function submit(e: FormEvent) {
    e.preventDefault()
    const v = values!
    save.mutate({ debtId: v.debtId as number, customerId: v.customerId as number, createdAt: v.createdAt as string, remarks: v.remarks as string, amount: v.amount as string })
  }

  return (
    <div className="max-w-2xl">
      <PageHeader title="New A/R entry" description={data.customer.name} />
      <Card>
        <CardContent>
          <form onSubmit={submit} className="grid gap-5 sm:grid-cols-2">
            <Field label="Date" htmlFor="createdAt" error={errors.createdAt}>
              <Input {...bind('createdAt')} type="date" required />
            </Field>
            <Field label="Amount (PHP)" htmlFor="amount" error={errors.amount}>
              <Input {...bind('amount')} inputMode="decimal" placeholder="0.00" required autoFocus />
            </Field>
            <Field label="Remarks" htmlFor="remarks" error={errors.remarks} className="sm:col-span-2">
              <Input {...bind('remarks')} />
            </Field>
            <FormButtons busy={save.isPending} back={customerHome(data.customer)} label="Save entry" />
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

export function DebtsPage() {
  const { data, error } = usePage<PageData & { debts: Debt[]; debtsTotal: number; customer: Customer }>()
  const columns = useMemo<ColumnDef<Debt>[]>(
    () => [
      { accessorKey: 'createdAt', header: 'Date', cell: ({ getValue }) => displayDate(getValue<string>()) },
      { accessorKey: 'remarks', header: 'Remarks' },
      {
        accessorKey: 'amount', header: 'Amount', meta: { numeric: true },
        cell: ({ getValue }) => <span className={toNumber(getValue()) < 0 ? 'text-emerald-700 dark:text-emerald-400' : ''}>{peso(getValue())}</span>,
      },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="A/R entries"
        description={<Link to={customerHome(data.customer)} className="text-primary hover:underline">{data.customer.name}</Link>}
        actions={
          <>
            <PrintButton />
            <Button asChild><Link to={`/debts/new?cid=${data.customer.customerId}`}><Plus /> New entry</Link></Button>
          </>
        }
      />
      <div className="mb-6 max-w-xs"><Stat label="Running total" value={peso(data.debtsTotal)} /></div>
      <DataTable columns={columns} data={data.debts} initialSort={[{ id: 'createdAt', desc: true }]} pageSize={25} />
    </>
  )
}

interface ArRow {
  customerId: number
  customer: Customer
  arAmount: number
}

export function ArSummaryPage() {
  const { data, error } = usePage<PageData & { debtsMap: ArRow[]; arTotal: number; dateToday: string }>()
  const columns = useMemo<ColumnDef<ArRow>[]>(
    () => [
      {
        id: 'customer', header: 'Customer', accessorFn: (r) => r.customer.name,
        cell: ({ row }) => <Link to={`/debts/list?cid=${row.original.customerId}`} className="font-medium text-primary hover:underline">{row.original.customer.name}</Link>,
      },
      { id: 'number', header: 'Contact number', accessorFn: (r) => r.customer.mainNumber ?? '' },
      { accessorKey: 'arAmount', header: 'Total A/R', ...moneyCell },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="A/R summary" description={`What each customer owes as of ${displayDate(data.dateToday)}`} actions={<PrintButton />} />
      <div className="mb-6 max-w-xs"><Stat label="Total owed" value={peso(data.arTotal)} tone={data.arTotal > 0 ? 'danger' : undefined} /></div>
      <DataTable
        columns={columns}
        data={data.debtsMap}
        initialSort={[{ id: 'arAmount', desc: true }]}
        emptyText="No customer owes anything."
        footer={
          <TableRow>
            <TableCell className="font-semibold" colSpan={2}>Total</TableCell>
            <TableCell className="text-right tabular font-semibold">{peso(data.arTotal)}</TableCell>
          </TableRow>
        }
      />
    </>
  )
}

// ---------- Expenses ----------

export function ExpensesPage() {
  const { data, error } = usePage<PageData & { expenses: Expense[]; expensesTotal: number; date: string }>()
  const columns = useMemo<ColumnDef<Expense>[]>(
    () => [
      { accessorKey: 'createdAt', header: 'Date', cell: ({ getValue }) => displayDate(getValue<string>()) },
      { accessorKey: 'description', header: 'Description' },
      { accessorKey: 'amount', header: 'Amount', ...moneyCell },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Expenses"
        description={data.date}
        actions={
          <>
            <DateFilter />
            <Button asChild><Link to="/expenses/new"><Plus /> New expense</Link></Button>
          </>
        }
      />
      <div className="mb-6 max-w-xs"><Stat label="Total" value={peso(data.expensesTotal)} /></div>
      <DataTable columns={columns} data={data.expenses} emptyText="No expenses on this day." />
    </>
  )
}

export function ExpenseFormPage() {
  const { data, error } = usePage<PageData & { expense: Expense }>()
  const save = useSave('/expenses/save', { formView: 'new-expense' })
  const initial = useMemo(() => (data ? { ...data.expense, createdAt: isoDate(data.expense.createdAt) } : undefined), [data])
  const { values, bind } = useFormValues<Record<string, unknown>>(initial as Record<string, unknown> | undefined)
  const errors = fieldErrors(save)
  if (!data || !values) return <PageState error={error} />

  function submit(e: FormEvent) {
    e.preventDefault()
    const v = values!
    save.mutate({ expenseId: v.expenseId as number, createdAt: v.createdAt as string, description: v.description as string, amount: v.amount as string })
  }

  return (
    <div className="max-w-2xl">
      <PageHeader title="New expense" />
      <Card>
        <CardContent>
          <form onSubmit={submit} className="grid gap-5 sm:grid-cols-2">
            <Field label="Date" htmlFor="createdAt" error={errors.createdAt}>
              <Input {...bind('createdAt')} type="date" required />
            </Field>
            <Field label="Amount (PHP)" htmlFor="amount" error={errors.amount}>
              <Input {...bind('amount')} inputMode="decimal" placeholder="0.00" required />
            </Field>
            <Field label="Description" htmlFor="description" error={errors.description} className="sm:col-span-2">
              <Input {...bind('description')} required autoFocus maxLength={45} />
            </Field>
            <FormButtons busy={save.isPending} back="/expenses/list?d=today" label="Save expense" />
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
