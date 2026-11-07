import { useMemo, useState, type FormEvent } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { Check, Hourglass, Pencil, X } from 'lucide-react'
import { Link, useSearchParams } from 'react-router'

import { Count, DateFilter, StatusBadge } from '@/components/bits'
import { DataTable } from '@/components/data-table'
import { Field, PageHeader, PageState, PrintButton, Stat } from '@/components/page'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { TableCell, TableRow } from '@/components/ui/table'
import { Textarea } from '@/components/ui/textarea'
import { fieldErrors, usePage, useSave } from '@/hooks/use-page'
import { displayDate, peso, timestampParam, toNumber } from '@/lib/format'
import type { DailySale, DailySalesData, MonthlyData, OrderFormData, OrderRow, OrdersData } from '@/lib/types'
import { containerColumns } from './customers'

// ---------- Order lists ----------

function OrderActions({ row, deliverPath }: { row: OrderRow; deliverPath: string }) {
  const deliver = useSave(deliverPath)
  const cancel = useSave('/orders/cancel')
  const o = row.order
  const open = o.status === 'Pending'
  return (
    <div className="flex gap-1.5">
      <Button size="sm" disabled={!open || deliver.isPending} onClick={() => deliver.mutate({ orderId: o.orderId })}>
        <Check /> Deliver
      </Button>
      <AlertDialog>
        <AlertDialogTrigger asChild>
          <Button size="sm" variant="outline" disabled={!open || cancel.isPending}>
            <X /> Cancel
          </Button>
        </AlertDialogTrigger>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cancel order {o.deliveryReceiptNum}?</AlertDialogTitle>
            <AlertDialogDescription>
              {row.CustomerName}'s order for {peso(o.totalAmount)} will be cancelled and the containers it added to their
              balance taken back. This can't be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Keep the order</AlertDialogCancel>
            <AlertDialogAction variant="destructive" onClick={() => cancel.mutate({ orderId: o.orderId })}>
              Cancel order
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}

function useOrderColumns(deliverPath: string, withStatus: boolean) {
  return useMemo<ColumnDef<OrderRow>[]>(
    () => [
      { id: 'actions', header: '', enableSorting: false, cell: ({ row }) => <OrderActions row={row.original} deliverPath={deliverPath} /> },
      ...(withStatus
        ? [{ id: 'status', header: 'Status', accessorFn: (r: OrderRow) => r.order.status, cell: ({ getValue }: { getValue: () => unknown }) => <StatusBadge status={String(getValue())} /> }]
        : []),
      {
        id: 'customer', header: 'Customer', accessorFn: (r) => r.CustomerName,
        cell: ({ row }) => (
          <Link to={`/customers/view?customerId=${row.original.order.customerId}`} className="font-medium text-primary hover:underline">
            {row.original.CustomerName}
          </Link>
        ),
      },
      { id: 'dr', header: 'DR #', accessorFn: (r) => r.order.deliveryReceiptNum },
      { id: 'paid', header: 'Paid', accessorFn: (r) => r.order.amountPaid, cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { id: 'total', header: 'Total', accessorFn: (r) => r.order.totalAmount, cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      ...containerColumns<OrderRow>((r) => r.order),
      { id: 'date', header: 'Time', accessorFn: (r) => r.order.createdAt, cell: ({ row }) => row.original.formattedDate },
      {
        id: 'edit', header: '', enableSorting: false,
        cell: ({ row }) => (
          <Button size="icon-sm" variant="ghost" asChild aria-label={`Edit order ${row.original.order.deliveryReceiptNum}`}>
            <Link to={`/orders/edit?orderId=${row.original.order.orderId}`}><Pencil /></Link>
          </Button>
        ),
      },
    ],
    [deliverPath, withStatus],
  )
}

export function OrdersPage() {
  const { data, error } = usePage<OrdersData>()
  const columns = useOrderColumns('/orders/deliver', true)
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Orders"
        description={data.datePicked}
        actions={
          <>
            {data.pendingCount > 0 && (
              <Button variant="outline" asChild>
                <Link to="/orders/pending"><Hourglass /> {data.pendingCount} pending</Link>
              </Button>
            )}
            <DateFilter />
          </>
        }
      />
      <DataTable columns={columns} data={data.orders} initialSort={[{ id: 'dr', desc: true }]} emptyText="No orders on this day." />
    </>
  )
}

export function PendingOrdersPage() {
  const { data, error } = usePage<OrdersData>()
  const columns = useOrderColumns('/orders/deliverPending', false)
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="Pending orders" description="Orders waiting for delivery, from every day." />
      <DataTable columns={columns} data={data.orders} initialSort={[{ id: 'date', desc: false }]} emptyText="Nothing is waiting for delivery." />
    </>
  )
}

// ---------- New / edit order ----------

/** Which order count each product's quantity goes into (the shop's product ids, as in the old form). */
const COUNT_FOR_PRODUCT: Record<number, string> = {
  1: 'roundRefillOnlyCount',
  2: 'slimRefillOnlyCount',
  3: 'roundContainerOnlyCount',
  4: 'slimContainerOnlyCount',
  5: 'roundRefillWithContainerCount',
  6: 'slimRefillWithContainerCount',
  22: 'roundFreeCount',
  23: 'slimFreeCount',
}

export function OrderFormPage({ mode }: { mode: 'new' | 'edit' }) {
  const { data, error } = usePage<OrderFormData>()
  if (!data) return <PageState error={error} />
  return <OrderForm data={data} mode={mode} />
}

function OrderForm({ data, mode }: { data: OrderFormData; mode: 'new' | 'edit' }) {
  const [params] = useSearchParams()
  const order = data.opeForm.order
  const lines = data.opeForm.opList
  const save = useSave(mode === 'new' ? '/orders/save' : '/orders/update', { formView: mode === 'new' ? 'new-order' : 'edit-order' })
  const errors = fieldErrors(save)

  const [quantities, setQuantities] = useState<string[]>(() => lines.map((l) => (l.quantity ? String(l.quantity) : '')))
  const [dr, setDr] = useState(mode === 'edit' ? (data.drNumber ?? order.deliveryReceiptNum) : '')
  const [interval, setInterval] = useState(mode === 'new' ? String(data.opeForm.orderInterval || '') : '')
  const [amountPaid, setAmountPaid] = useState(mode === 'edit' ? String(order.amountPaid ?? '') : '')
  const [roundReturned, setRoundReturned] = useState(order.roundReturned ?? '0')
  const [slimReturned, setSlimReturned] = useState(order.slimReturned ?? '0')
  const [remarks, setRemarks] = useState(order.remarks ?? '')

  const products = lines.map((line) => data.productList.find((p) => p.productId === line.productId))
  const total = lines.reduce((sum, _line, i) => sum + toNumber(quantities[i]) * toNumber(products[i]?.sellingPrice), 0)
  const changed = quantities.some((q, i) => q !== (lines[i].quantity ? String(lines[i].quantity) : ''))
  // An edit keeps the saved total until a quantity changes, as the old form did.
  const shownTotal = mode === 'edit' && !changed ? order.totalAmount : total

  function counts() {
    const result: Record<string, number> = {}
    for (const field of Object.values(COUNT_FOR_PRODUCT)) result[field] = Number((order as unknown as Record<string, number>)[field] ?? 0)
    if (mode === 'new' || changed) {
      lines.forEach((line, i) => {
        const field = COUNT_FOR_PRODUCT[line.productId]
        if (field) result[field] = toNumber(quantities[i])
      })
    }
    return result
  }

  function submit(e: FormEvent) {
    e.preventDefault()
    const values: Record<string, string | number> = {
      'order.orderId': order.orderId,
      'order.customerId': order.customerId,
      'order.deliveryReceiptNum': dr,
      'order.totalAmount': shownTotal.toFixed(2),
      'order.amountPaid': amountPaid,
      'order.roundReturned': roundReturned,
      'order.slimReturned': slimReturned,
      'order.remarks': remarks,
    }
    for (const [field, n] of Object.entries(counts())) values[`order.${field}`] = n
    lines.forEach((line, i) => {
      values[`opList[${i}].productId`] = line.productId
      values[`opList[${i}].quantity`] = quantities[i]
      if (mode === 'edit') values[`opList[${i}].orderProductId`] = line.orderProductId
    })
    // Blank keeps the customer's interval; the server needs a number, as the old form's "0".
    if (mode === 'new') values.orderInterval = interval.trim() || '0'
    else {
      values['order.createdAt'] = timestampParam(data.createdAt ?? order.createdAt)
      values['order.status'] = data.status ?? order.status
    }
    save.mutate(values)
  }

  const balance = shownTotal - toNumber(amountPaid)
  const customerId = order.customerId || params.get('customerId')

  return (
    <form onSubmit={submit}>
      <PageHeader
        title={mode === 'new' ? 'New order' : `Edit order ${order.deliveryReceiptNum}`}
        description={`${data.customerName} · ${data.customerAddress ?? ''}`}
      />
      <div className="grid gap-6 lg:grid-cols-[1fr_22rem]">
        <div className="grid gap-6">
          <Card>
            <CardHeader><CardTitle>Delivery</CardTitle></CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-2">
              <Field label="Delivery receipt (DR) number" htmlFor="dr" error={errors['order.deliveryReceiptNum']}>
                <Input id="dr" value={dr} onChange={(e) => setDr(e.target.value)} required autoFocus={mode === 'new'} />
              </Field>
              {mode === 'new' && (
                <Field label="Order interval (days)" htmlFor="interval" hint="Leave as is to keep the customer's usual interval.">
                  <Input id="interval" type="number" min={0} value={interval} onChange={(e) => setInterval(e.target.value)} />
                </Field>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle>Products</CardTitle></CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              {lines.map((line, i) => {
                const product = products[i]
                return (
                  <Field key={line.productId} label={product?.name ?? `Product ${line.productId}`} htmlFor={`qty-${line.productId}`}>
                    <div className="flex items-center gap-2">
                      <Input
                        id={`qty-${line.productId}`}
                        type="number"
                        min={0}
                        inputMode="numeric"
                        placeholder="0"
                        value={quantities[i]}
                        onChange={(e) => setQuantities((q) => q.map((v, j) => (j === i ? e.target.value : v)))}
                        className="w-24"
                      />
                      <span className="text-sm text-muted-foreground tabular">× {peso(product?.sellingPrice)}</span>
                    </div>
                  </Field>
                )
              })}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle>Remarks</CardTitle></CardHeader>
            <CardContent>
              <Textarea value={remarks} onChange={(e) => setRemarks(e.target.value)} placeholder="Optional" rows={2} aria-label="Remarks" />
              {errors['order.remarks'] && <p className="mt-1 text-sm text-destructive">{errors['order.remarks']}</p>}
            </CardContent>
          </Card>
        </div>

        <Card className="h-fit lg:sticky lg:top-16">
          <CardHeader><CardTitle>Summary</CardTitle></CardHeader>
          <CardContent className="grid gap-4">
            <div>
              <div className="text-sm text-muted-foreground">Total amount</div>
              <div className="text-3xl font-semibold tabular">{peso(shownTotal)}</div>
              {errors['order.totalAmount'] && <p className="text-sm text-destructive">{errors['order.totalAmount']}</p>}
            </div>
            <Field label="Amount paid" htmlFor="paid" error={errors['order.amountPaid']}>
              <Input id="paid" inputMode="decimal" value={amountPaid} onChange={(e) => setAmountPaid(e.target.value)} placeholder="0.00" required className="h-10 text-lg" />
            </Field>
            {amountPaid !== '' && balance !== 0 && (
              <div className="rounded-md bg-muted px-3 py-2 text-sm">
                {balance > 0 ? <>Unpaid <strong className="tabular">{peso(balance)}</strong> goes to A/R.</> : <>Paid <strong className="tabular">{peso(-balance)}</strong> more than the total.</>}
              </div>
            )}
            <div className="grid grid-cols-2 gap-3">
              <Field label="Round returned" htmlFor="rr" error={errors['order.roundReturned']}>
                <Input id="rr" type="number" min={0} value={roundReturned} onChange={(e) => setRoundReturned(e.target.value)} className="tint-round" />
              </Field>
              <Field label="Slim returned" htmlFor="sr" error={errors['order.slimReturned']}>
                <Input id="sr" type="number" min={0} value={slimReturned} onChange={(e) => setSlimReturned(e.target.value)} className="tint-slim" />
              </Field>
            </div>
            <div className="grid gap-2">
              <Button type="submit" size="lg" disabled={save.isPending}>
                <Check /> {save.isPending ? 'Saving…' : mode === 'new' ? 'Save order' : 'Save changes'}
              </Button>
              <Button type="button" variant="outline" asChild>
                <Link to={mode === 'new' && customerId ? `/customers/view?customerId=${customerId}` : '/orders/list'}>Cancel</Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </form>
  )
}

// ---------- Daily sales ----------

export function DailySalesPage() {
  const { data, error } = usePage<DailySalesData>()
  const columns = useMemo<ColumnDef<DailySale>[]>(
    () => {
      const blank = { roundRefillOnlyCount: 0, slimRefillOnlyCount: 0, roundContainerOnlyCount: 0, slimContainerOnlyCount: 0, roundRefillWithContainerCount: 0, slimRefillWithContainerCount: 0, roundReturned: '0', slimReturned: '0' }
      const counts = containerColumns<DailySale>((d) => ({ ...(d.order ?? blank), roundReturned: d.returnedRound ?? '0', slimReturned: d.returnedSlim ?? '0' }) as never)
      return [
        { id: 'dr', header: 'DR #', accessorFn: (d) => d.order?.deliveryReceiptNum ?? '' },
        { accessorKey: 'dateAndTime', header: 'Time' },
        { accessorKey: 'customerName', header: 'Customer' },
        ...counts,
        { accessorKey: 'remarks', header: 'Remarks', cell: ({ getValue }) => <span className="block max-w-56 truncate" title={getValue<string>() ?? ''}>{getValue<string>()}</span> },
        { accessorKey: 'balanceAmount', header: 'A/R', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
        { accessorKey: 'paidCash', header: 'Cash', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
        { accessorKey: 'paidCheck', header: 'Check', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
        { accessorKey: 'expenseAmount', header: 'Expense', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      ]
    },
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="Daily sales" description={data.datePicked} actions={<><DateFilter /><PrintButton /></>} />
      <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Net cash" value={peso(data.netCash)} />
        <Stat label="Cash payments" value={peso(data.totalCashPayments)} />
        <Stat label="Check payments" value={peso(data.totalCheckPayments)} />
        <Stat label="A/R" value={peso(data.totalDebt)} tone={toNumber(data.totalDebt) > 0 ? 'danger' : undefined} />
        <Stat label="Expenses" value={peso(data.totalExpenses)} />
        <Stat label="Round delivered / returned" value={`${data.totalRoundDelivered} / ${data.totalRoundReturned}`} tone="round" />
        <Stat label="Slim delivered / returned" value={`${data.totalSlimDelivered} / ${data.totalSlimReturned}`} tone="slim" />
      </div>
      <DataTable columns={columns} data={data.dailySales} initialSort={[{ id: 'dr', desc: true }]} emptyText="No sales, payments or expenses on this day." />
    </>
  )
}

// ---------- Monthly summary ----------

type DayTotal = MonthlyData['dailyTotals'][number]

export function MonthlySummaryPage() {
  const { data, error } = usePage<MonthlyData>()
  const columns = useMemo<ColumnDef<DayTotal>[]>(
    () => [
      { accessorKey: 'date', header: 'Date', cell: ({ getValue }) => displayDate(getValue<string>()) },
      { accessorKey: 'totalCash', header: 'Cash', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { accessorKey: 'totalAr', header: 'A/R', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { accessorKey: 'totalPayments', header: 'Payments', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { accessorKey: 'totalExpenses', header: 'Expenses', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { accessorKey: 'totalDeliveredRound', header: 'Round', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-round' } },
      { accessorKey: 'totalDeliveredSlim', header: 'Slim', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-slim' } },
      { accessorKey: 'totalDeliveredContainers', header: 'Total delivered', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true } },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="Monthly summary" description={data.monthYear} actions={<><DateFilter month label="Show month" /><PrintButton /></>} />
      <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Cash" value={peso(data.grandTotalCash)} />
        <Stat label="A/R" value={peso(data.grandTotalAr)} />
        <Stat label="Payments" value={peso(data.grandTotalPayment)} />
        <Stat label="Expenses" value={peso(data.grandTotalExpense)} />
      </div>
      <DataTable
        columns={columns}
        data={data.dailyTotals}
        searchable={false}
        initialSort={[{ id: 'date', desc: false }]}
        footer={
          <TableRow>
            <TableCell className="font-semibold">Month total</TableCell>
            <TableCell className="text-right tabular font-semibold">{peso(data.grandTotalCash)}</TableCell>
            <TableCell className="text-right tabular font-semibold">{peso(data.grandTotalAr)}</TableCell>
            <TableCell className="text-right tabular font-semibold">{peso(data.grandTotalPayment)}</TableCell>
            <TableCell className="text-right tabular font-semibold">{peso(data.grandTotalExpense)}</TableCell>
            <TableCell className="tint-round text-right tabular font-semibold">{data.grandTotalRound}</TableCell>
            <TableCell className="tint-slim text-right tabular font-semibold">{data.grandTotalSlim}</TableCell>
            <TableCell className="text-right tabular font-semibold">{data.grandTotalContainers}</TableCell>
          </TableRow>
        }
      />
    </>
  )
}
