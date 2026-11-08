import { useMemo, type FormEvent } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import {
  Banknote,
  ChevronDown,
  MessageSquareOff,
  MessageSquareText,
  Pencil,
  Plus,
  Receipt,
  SlidersHorizontal,
  Tags,
  Undo2,
} from 'lucide-react'
import { Link, useSearchParams } from 'react-router'

import { Count, DateFilter, StatusBadge } from '@/components/bits'
import { DataTable } from '@/components/data-table'
import { Field, PageHeader, PageState, PrintButton, Stat } from '@/components/page'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useFormValues } from '@/hooks/use-form'
import { fieldErrors, usePage, useSave } from '@/hooks/use-page'
import type { PageData } from '@/lib/api'
import { displayDate, displayDateTime, peso } from '@/lib/format'
import type { Customer, CustomerData, CustomerFormData, CustomersData, Order, Payment } from '@/lib/types'

function CustomerLink({ customer }: { customer: Pick<Customer, 'customerId' | 'name'> }) {
  return (
    <Link to={`/customers/view?customerId=${customer.customerId}`} className="font-medium text-primary hover:underline">
      {customer.name}
    </Link>
  )
}

function NewOrderButton({ customerId }: { customerId: number }) {
  return (
    <Button size="sm" variant="outline" asChild>
      <Link to={`/orders/neworder?customerId=${customerId}`}>
        <Plus /> Order
      </Link>
    </Button>
  )
}

// ---------- Customer list ----------

type CustomerRow = CustomersData['customerPrices'][number]

export function CustomersPage() {
  const { data, error } = usePage<CustomersData>()
  const columns = useMemo<ColumnDef<CustomerRow>[]>(
    () => [
      { id: 'name', header: 'Customer', accessorFn: (r) => r.customer.name, cell: ({ row }) => <CustomerLink customer={row.original.customer} /> },
      { id: 'type', header: 'Type', accessorFn: (r) => r.customer.type, cell: ({ getValue }) => <Badge variant="secondary">{getValue<string>()}</Badge> },
      { id: 'refillPrice', header: 'Refill price', accessorFn: (r) => r.refillPrice, cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { id: 'address', header: 'Address', accessorFn: (r) => r.customer.address ?? '' },
      { id: 'contact', header: 'Contact person', accessorFn: (r) => r.customer.contactName ?? '' },
      { id: 'number', header: 'Contact number', accessorFn: (r) => r.customer.mainNumber ?? '' },
      {
        id: 'actions', header: '', enableSorting: false,
        cell: ({ row }) => <div className="flex justify-end print:hidden"><NewOrderButton customerId={row.original.customer.customerId} /></div>,
      },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Customers"
        description={`${data.customerPrices.length} customers`}
        actions={
          <>
            <PrintButton />
            <Button asChild>
              <Link to="/customers/new"><Plus /> New customer</Link>
            </Button>
          </>
        }
      />
      <DataTable columns={columns} data={data.customerPrices} searchPlaceholder="Search name, address or number…" initialSort={[{ id: 'name', desc: false }]} />
    </>
  )
}

// ---------- One customer ----------

export function CustomerPage() {
  const [params] = useSearchParams()
  const { data, error } = usePage<CustomerData>()
  const id = params.get('customerId')
  const textOptOut = useSave('/sms/optOut')

  const orderColumns = useMemo<ColumnDef<Order>[]>(
    () => [
      { accessorKey: 'createdAt', header: 'Date', cell: ({ getValue }) => displayDateTime(getValue<string>()) },
      { accessorKey: 'deliveryReceiptNum', header: 'DR #' },
      { accessorKey: 'status', header: 'Status', cell: ({ getValue }) => <StatusBadge status={getValue<string>()} /> },
      { accessorKey: 'amountPaid', header: 'Paid', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { accessorKey: 'totalAmount', header: 'Total', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      ...containerColumns<Order>((o) => o),
      {
        id: 'edit', header: '', enableSorting: false,
        cell: ({ row }) => (
          <Button size="icon-sm" variant="ghost" asChild aria-label={`Edit order ${row.original.deliveryReceiptNum}`}>
            <Link to={`/orders/edit?orderId=${row.original.orderId}`}><Pencil /></Link>
          </Button>
        ),
      },
    ],
    [],
  )
  const paymentColumns = useMemo<ColumnDef<Payment>[]>(
    () => [
      { accessorKey: 'createdAt', header: 'Date', cell: ({ getValue }) => displayDate(getValue<string>()) },
      { accessorKey: 'remarks', header: 'Remarks' },
      { accessorKey: 'paymentType', header: 'Type', cell: ({ getValue }) => <Badge variant="outline">{getValue<string>()}</Badge> },
      { accessorKey: 'amount', header: 'Amount', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      { accessorKey: 'withholdingTax', header: 'Withholding tax', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  const c = data.customer
  const returns = data.containerHistory

  return (
    <>
      <PageHeader
        title={c.name}
        description={
          <span className="flex flex-wrap items-center gap-2">
            <Badge variant="secondary">{c.type}</Badge>
            {data.dueDateText && <Badge variant="destructive">{data.dueDateText}</Badge>}
            {c.smsOptOut && <Badge variant="outline"><MessageSquareOff /> No texts</Badge>}
          </span>
        }
        actions={
          <>
            <Button asChild>
              <Link to={`/orders/neworder?customerId=${id}`}><Plus /> New order</Link>
            </Button>
            <Button variant="outline" asChild>
              <Link to={`/payments/new?cid=${id}`}><Banknote /> Payment</Link>
            </Button>
            <Button variant="outline" asChild>
              <Link to={`/container/return?cid=${id}`}><Undo2 /> Return containers</Link>
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline">More <ChevronDown /></Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem asChild><Link to={`/debts/new?cid=${id}`}><Receipt /> New A/R entry</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to={`/debts/list?cid=${id}`}><Receipt /> A/R entries</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to={`/payments/list?cid=${id}`}><Banknote /> Payments</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to={`/prices/list/${id}`}><Tags /> Products &amp; prices</Link></DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild><Link to={`/customers/edit?customerId=${id}`}><Pencil /> Edit customer</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to={`/customers/containerBalances?customerId=${id}`}><SlidersHorizontal /> Set container balance</Link></DropdownMenuItem>
                <DropdownMenuSeparator />
                {/* Its own POST: the edit form posts a whole customer and must not reset this. */}
                <DropdownMenuItem
                  disabled={textOptOut.isPending}
                  onSelect={() => textOptOut.mutate({ customerId: c.customerId, optOut: String(!c.smsOptOut) })}
                >
                  {c.smsOptOut ? <><MessageSquareText /> Allow texts</> : <><MessageSquareOff /> Don't send texts</>}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </>
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <Stat label="Total A/R" value={peso(data.totalDebt)} tone={data.totalDebt > 0 ? 'danger' : undefined} />
        <Stat label="Round borrowed" value={data.totalBorrowedRound} tone="round" />
        <Stat label="Slim borrowed" value={data.totalBorrowedSlim} tone="slim" />
        <Stat label="Last order" value={<span className="text-base">{data.mostRecentOrderDate || '—'}</span>} />
        <Stat label="Next due" value={<span className="text-base">{data.dueDate || '—'}</span>} />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-[18rem_1fr]">
        <Card className="h-fit gap-3 py-4">
          <CardHeader className="px-4"><CardTitle className="text-sm">Contact</CardTitle></CardHeader>
          <CardContent className="grid gap-3 px-4 text-sm">
            <Detail label="Address" value={c.address} />
            <Detail label="Contact person" value={c.contactName} />
            <Detail label="Main number" value={c.mainNumber} />
            <Detail label="Alternate number" value={c.alternateNumber} />
            <Detail label="Email" value={c.emailAddress} />
            <Detail label="Orders every" value={`${c.orderInterval} days`} />
          </CardContent>
        </Card>

        <Tabs defaultValue="orders" className="min-w-0">
          <TabsList>
            <TabsTrigger value="orders">Orders ({data.orders.length})</TabsTrigger>
            <TabsTrigger value="payments">Payments ({data.payments.length})</TabsTrigger>
            <TabsTrigger value="returns">Container returns ({returns.length})</TabsTrigger>
            <TabsTrigger value="prices">Prices ({data.prices.length})</TabsTrigger>
          </TabsList>
          <TabsContent value="orders">
            <DataTable columns={orderColumns} data={data.orders} pageSize={15} initialSort={[{ id: 'createdAt', desc: true }]} searchable={false} />
          </TabsContent>
          <TabsContent value="payments">
            <DataTable columns={paymentColumns} data={data.payments} pageSize={15} initialSort={[{ id: 'createdAt', desc: true }]} searchable={false} />
          </TabsContent>
          <TabsContent value="returns">
            <DataTable
              searchable={false}
              pageSize={15}
              data={returns}
              initialSort={[{ id: 'createdAt', desc: true }]}
              columns={[
                { accessorKey: 'createdAt', header: 'Return date', cell: ({ getValue }) => displayDateTime(getValue<string>()) },
                { accessorKey: 'roundCount', header: 'Round returned', meta: { numeric: true, className: 'tint-round' } },
                { accessorKey: 'slimCount', header: 'Slim returned', meta: { numeric: true, className: 'tint-slim' } },
                { accessorKey: 'orderId', header: 'With an order', cell: ({ getValue }) => (getValue<number>() > 0 ? 'Yes' : 'No') },
              ]}
            />
          </TabsContent>
          <TabsContent value="prices">
            <div className="mb-3 flex justify-end">
              <Button size="sm" variant="outline" asChild>
                <Link to={`/prices/list/${id}`}><Tags /> Change prices</Link>
              </Button>
            </div>
            <DataTable
              searchable={false}
              data={data.prices}
              emptyText="This customer pays the list prices."
              columns={[
                { id: 'product', header: 'Product', accessorFn: (p) => p.product.name },
                { accessorKey: 'sellingPrice', header: 'Price', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
              ]}
            />
          </TabsContent>
        </Tabs>
      </div>
    </>
  )
}

function Detail({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <div>
      <div className="text-xs text-muted-foreground">{label}</div>
      <div>{value || <span className="text-muted-foreground">—</span>}</div>
    </div>
  )
}

/** The eight round/slim count columns shared by order tables. */
export function containerColumns<T>(order: (row: T) => Order): ColumnDef<T>[] {
  const col = (id: string, header: string, get: (o: Order) => number | string, tint: 'round' | 'slim'): ColumnDef<T> => ({
    id, header, accessorFn: (r) => Number(get(order(r)) ?? 0),
    cell: ({ getValue }) => <Count value={getValue<number>()} />,
    meta: { numeric: true, className: `tint-${tint}` },
  })
  return [
    col('roundRefill', 'Round refill', (o) => o.roundRefillOnlyCount, 'round'),
    col('slimRefill', 'Slim refill', (o) => o.slimRefillOnlyCount, 'slim'),
    col('roundCont', 'Round cont', (o) => o.roundContainerOnlyCount, 'round'),
    col('slimCont', 'Slim cont', (o) => o.slimContainerOnlyCount, 'slim'),
    col('roundBoth', 'Round both', (o) => o.roundRefillWithContainerCount, 'round'),
    col('slimBoth', 'Slim both', (o) => o.slimRefillWithContainerCount, 'slim'),
    col('roundRet', 'Round ret', (o) => o.roundReturned, 'round'),
    col('slimRet', 'Slim ret', (o) => o.slimReturned, 'slim'),
  ]
}

// ---------- New / edit customer ----------

export function CustomerFormPage({ mode }: { mode: 'new' | 'edit' }) {
  const { data, error } = usePage<CustomerFormData>()
  const save = useSave(mode === 'new' ? '/customers/save' : '/customers/update', { formView: mode === 'new' ? 'new-customer' : 'edit-customer' })
  const { values, setValue, bind } = useFormValues<Record<string, unknown>>(data?.customerForm as unknown as Record<string, unknown>)
  const errors = fieldErrors(save)
  if (!data || !values) return <PageState error={error} />

  function submit(e: FormEvent) {
    e.preventDefault()
    const v = values!
    save.mutate({
      customerId: v.customerId as number, name: v.name as string, type: v.type as string, address: v.address as string,
      mainNumber: v.mainNumber as string, contactName: v.contactName as string, alternateNumber: v.alternateNumber as string,
      emailAddress: v.emailAddress as string, orderInterval: v.orderInterval as number,
      ...(mode === 'edit' ? { totalRound: v.totalRound as number, totalSlim: v.totalSlim as number } : {}),
    })
  }

  const back = mode === 'edit' ? `/customers/view?customerId=${values.customerId}` : '/customers/list'
  return (
    <div className="max-w-2xl">
      <PageHeader title={mode === 'new' ? 'New customer' : `Edit ${data.customerForm.name}`} />
      <Card>
        <CardContent>
          <form onSubmit={submit} className="grid gap-5 sm:grid-cols-2">
            <Field label="Customer name" htmlFor="name" error={errors.name} className="sm:col-span-2">
              <Input {...bind('name')} required autoFocus={mode === 'new'} />
            </Field>
            <Field label="Customer type" error={errors.type} className="sm:col-span-2">
              <RadioGroup value={String(values.type ?? '')} onValueChange={(v) => setValue('type', v)} className="flex gap-6">
                {(data.types ?? ['Business', 'Residential']).map((t) => (
                  <div key={t} className="flex items-center gap-2">
                    <RadioGroupItem value={t} id={`type-${t}`} />
                    <Label htmlFor={`type-${t}`} className="font-normal">{t}</Label>
                  </div>
                ))}
              </RadioGroup>
            </Field>
            <Field label="Address" htmlFor="address" error={errors.address} className="sm:col-span-2">
              <Input {...bind('address')} />
            </Field>
            <Field label="Contact person" htmlFor="contactName" error={errors.contactName}>
              <Input {...bind('contactName')} />
            </Field>
            <Field label="Main number" htmlFor="mainNumber" error={errors.mainNumber}>
              <Input {...bind('mainNumber')} type="tel" />
            </Field>
            <Field label="Alternate number" htmlFor="alternateNumber" error={errors.alternateNumber}>
              <Input {...bind('alternateNumber')} type="tel" />
            </Field>
            <Field label="Email address" htmlFor="emailAddress" error={errors.emailAddress}>
              <Input {...bind('emailAddress')} type="email" />
            </Field>
            <Field label="Order interval (days)" htmlFor="orderInterval" error={errors.orderInterval} hint="How often this customer usually orders; sets the due date.">
              <Input {...bind('orderInterval')} type="number" min={0} />
            </Field>
            <FormButtons busy={save.isPending} back={back} label={mode === 'new' ? 'Create customer' : 'Save changes'} />
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

export function FormButtons({ busy, back, label = 'Save' }: { busy: boolean; back: string; label?: string }) {
  return (
    <div className="flex gap-2 sm:col-span-2">
      <Button type="submit" disabled={busy}>{busy ? 'Saving…' : label}</Button>
      <Button type="button" variant="outline" asChild>
        <Link to={back}>Cancel</Link>
      </Button>
    </div>
  )
}

// ---------- Set container balance ----------

export function ContainerBalanceFormPage() {
  const { data, error } = usePage<CustomerFormData>()
  const save = useSave('/customers/updateContainerBalances', { formView: 'set-container-totals' })
  const { values, bind } = useFormValues<Record<string, unknown>>(data?.customerForm as unknown as Record<string, unknown>)
  const errors = fieldErrors(save)
  if (!data || !values) return <PageState error={error} />
  const c = data.customerForm

  function submit(e: FormEvent) {
    e.preventDefault()
    const v = values!
    // The controller saves the whole customer, so the other fields go back unchanged.
    save.mutate({
      customerId: c.customerId, type: c.type, name: c.name, address: c.address, mainNumber: c.mainNumber,
      contactName: c.contactName, alternateNumber: c.alternateNumber, emailAddress: c.emailAddress,
      orderInterval: c.orderInterval, totalRound: v.totalRound as number, totalSlim: v.totalSlim as number,
    })
  }

  return (
    <div className="max-w-xl">
      <PageHeader title="Set container balance" description={`${c.name}: the containers this customer has right now.`} />
      <Card>
        <CardContent>
          <form onSubmit={submit} className="grid gap-5 sm:grid-cols-2">
            <Field label="Round containers" htmlFor="totalRound" error={errors.totalRound}>
              <Input {...bind('totalRound')} type="number" className="tint-round" required />
            </Field>
            <Field label="Slim containers" htmlFor="totalSlim" error={errors.totalSlim}>
              <Input {...bind('totalSlim')} type="number" className="tint-slim" required />
            </Field>
            <FormButtons busy={save.isPending} back={`/customers/view?customerId=${c.customerId}`} label="Save balance" />
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

// ---------- Due dates ----------

interface DueDate {
  customer: Customer
  lastOrderDate: string
  dueDate: string
  daysRemaining: number
}

export function DueDatesPage() {
  const { data, error } = usePage<PageData & { dueDates: DueDate[] }>()
  const columns = useMemo<ColumnDef<DueDate>[]>(
    () => [
      { id: 'name', header: 'Customer', accessorFn: (r) => r.customer.name, cell: ({ row }) => <CustomerLink customer={row.original.customer} /> },
      { id: 'number', header: 'Contact number', accessorFn: (r) => r.customer.mainNumber ?? '' },
      { accessorKey: 'lastOrderDate', header: 'Last order', cell: ({ getValue }) => displayDate(getValue<string>()) },
      { accessorKey: 'dueDate', header: 'Due date', cell: ({ getValue }) => displayDate(getValue<string>()) },
      {
        accessorKey: 'daysRemaining', header: 'Days left', meta: { numeric: true },
        cell: ({ getValue }) => {
          const days = getValue<number>()
          return <span className={days < 0 ? 'font-medium text-destructive' : days <= 1 ? 'font-medium text-orange-600 dark:text-orange-400' : ''}>{days}</span>
        },
      },
      { id: 'actions', header: '', enableSorting: false, cell: ({ row }) => <div className="flex justify-end print:hidden"><NewOrderButton customerId={row.original.customer.customerId} /></div> },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="Due dates" description="When each customer is due to order again, based on their order interval." actions={<PrintButton />} />
      <DataTable columns={columns} data={data.dueDates} initialSort={[{ id: 'daysRemaining', desc: false }]} />
    </>
  )
}

export function DailyDueDatesPage() {
  const { data, error } = usePage<PageData & { dueDates: DueDate[]; datePicked: string }>()
  const columns = useMemo<ColumnDef<DueDate>[]>(
    () => [
      { id: 'name', header: 'Customer', accessorFn: (r) => r.customer.name, cell: ({ row }) => <CustomerLink customer={row.original.customer} /> },
      { id: 'address', header: 'Address', accessorFn: (r) => r.customer.address ?? '' },
      { id: 'number', header: 'Contact number', accessorFn: (r) => r.customer.mainNumber ?? '' },
      { accessorKey: 'lastOrderDate', header: 'Last order', cell: ({ getValue }) => displayDate(getValue<string>()) },
      { id: 'actions', header: '', enableSorting: false, cell: ({ row }) => <div className="flex justify-end print:hidden"><NewOrderButton customerId={row.original.customer.customerId} /></div> },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="Due today" description={`Customers due on ${data.datePicked}`} actions={<><DateFilter /><PrintButton /></>} />
      <DataTable columns={columns} data={data.dueDates} emptyText="No customers are due on this day." />
    </>
  )
}

// ---------- Inactive and monthly-active customers ----------

export function InactiveCustomersPage() {
  const { data, error } = usePage<PageData & { customerList: Customer[] }>()
  const columns = useMemo<ColumnDef<Customer>[]>(
    () => [
      { accessorKey: 'name', header: 'Customer', cell: ({ row }) => <CustomerLink customer={row.original} /> },
      { accessorKey: 'type', header: 'Type' },
      { accessorKey: 'address', header: 'Address' },
      { accessorKey: 'contactName', header: 'Contact person' },
      { accessorKey: 'mainNumber', header: 'Contact number' },
      { id: 'actions', header: '', enableSorting: false, cell: ({ row }) => <div className="flex justify-end print:hidden"><NewOrderButton customerId={row.original.customerId} /></div> },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Inactive customers"
        description="Customers who have not ordered in a while."
        actions={
          <>
            <Button variant="outline" asChild className="print:hidden">
              <Link to="/sms/compose"><MessageSquareText /> Text overdue customers</Link>
            </Button>
            <PrintButton />
          </>
        }
      />
      <DataTable columns={columns} data={data.customerList} emptyText="Every customer has ordered recently." />
    </>
  )
}

interface ActiveCustomer {
  customerId: number
  type: string
  name: string
  address: string | null
  mainNumber: string | null
  orders: number
}

export function MonthlyActivePage() {
  const { data, error } = usePage<PageData & { cwoList: Omit<ActiveCustomer, 'orders'>[]; monthYear: string }>()
  const columns = useMemo<ColumnDef<ActiveCustomer>[]>(
    () => [
      { accessorKey: 'name', header: 'Customer', cell: ({ row }) => <CustomerLink customer={row.original} /> },
      { accessorKey: 'type', header: 'Type' },
      { accessorKey: 'address', header: 'Address' },
      { accessorKey: 'mainNumber', header: 'Contact number' },
      { accessorKey: 'orders', header: 'Orders', meta: { numeric: true } },
      { id: 'actions', header: '', enableSorting: false, cell: ({ row }) => <div className="flex justify-end print:hidden"><NewOrderButton customerId={row.original.customerId} /></div> },
    ],
    [],
  )
  // The report has one row per order; show each customer once with their number of orders.
  const customers = useMemo(() => {
    const byCustomer = new Map<number, ActiveCustomer>()
    for (const c of data?.cwoList ?? []) {
      const seen = byCustomer.get(c.customerId)
      if (seen) seen.orders += 1
      else byCustomer.set(c.customerId, { ...c, orders: 1 })
    }
    return [...byCustomer.values()]
  }, [data])
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Monthly active customers"
        description={`${customers.length} customers ordered in ${data.monthYear}`}
        actions={<><DateFilter month label="Show month" /><PrintButton /></>}
      />
      <DataTable columns={columns} data={customers} initialSort={[{ id: 'name', desc: false }]} emptyText="No orders this month." />
    </>
  )
}
