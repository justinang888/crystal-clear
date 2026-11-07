import { useMemo, useState, type FormEvent } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { Pencil, Plus, Undo2 } from 'lucide-react'
import { Link } from 'react-router'

import { Count, DateFilter } from '@/components/bits'
import { DataTable } from '@/components/data-table'
import { Field, PageHeader, PageState, PrintButton, Stat } from '@/components/page'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { TableCell, TableRow } from '@/components/ui/table'
import { useFormValues } from '@/hooks/use-form'
import { fieldErrors, usePage, useSave } from '@/hooks/use-page'
import type { PageData } from '@/lib/api'
import { isoDate, peso } from '@/lib/format'
import type { ContainerActivity, Customer, Product } from '@/lib/types'
import { FormButtons } from './customers'

function CustomerLink({ customer }: { customer: Pick<Customer, 'customerId' | 'name'> }) {
  return (
    <Link to={`/customers/view?customerId=${customer.customerId}`} className="font-medium text-primary hover:underline">
      {customer.name}
    </Link>
  )
}

// ---------- Products ----------

export function ProductsPage() {
  const { data, error } = usePage<PageData & { products: Product[] }>()
  const columns = useMemo<ColumnDef<Product>[]>(
    () => [
      { accessorKey: 'name', header: 'Product' },
      { accessorKey: 'description', header: 'Description' },
      { accessorKey: 'sellingPrice', header: 'Selling price', cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
      {
        id: 'edit', header: '', enableSorting: false,
        cell: ({ row }) => (
          <div className="flex justify-end">
            <Button size="sm" variant="outline" asChild>
              <Link to={`/products/edit/${row.original.productId}`}><Pencil /> Edit</Link>
            </Button>
          </div>
        ),
      },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Products"
        description="List prices. A customer's own prices are set on their page."
        actions={<Button asChild><Link to="/products/new"><Plus /> New product</Link></Button>}
      />
      <DataTable columns={columns} data={data.products} initialSort={[{ id: 'name', desc: false }]} />
    </>
  )
}

export function ProductFormPage({ mode }: { mode: 'new' | 'edit' }) {
  const { data, error } = usePage<PageData & { product: Product }>()
  const save = useSave('/products/save', { formView: mode === 'new' ? 'new-product' : 'edit-product' })
  const { values, bind } = useFormValues<Record<string, unknown>>(data?.product as unknown as Record<string, unknown> | undefined)
  const errors = fieldErrors(save)
  if (!data || !values) return <PageState error={error} />

  function submit(e: FormEvent) {
    e.preventDefault()
    const v = values!
    save.mutate({
      productId: v.productId as number, name: v.name as string, description: v.description as string, sellingPrice: v.sellingPrice as string,
      ...(mode === 'edit' && v.createdAt ? { createdAt: isoDate(v.createdAt as string) } : {}),
    })
  }

  return (
    <div className="max-w-2xl">
      <PageHeader title={mode === 'new' ? 'New product' : `Edit ${data.product.name}`} />
      <Card>
        <CardContent>
          <form onSubmit={submit} className="grid gap-5 sm:grid-cols-2">
            <Field label="Product name" htmlFor="name" error={errors.name} className="sm:col-span-2">
              <Input {...bind('name')} required autoFocus={mode === 'new'} />
            </Field>
            <Field label="Description" htmlFor="description" error={errors.description} className="sm:col-span-2">
              <Input {...bind('description')} required />
            </Field>
            <Field label="Selling price (PHP)" htmlFor="sellingPrice" error={errors.sellingPrice ?? errors.initialPrice}>
              <Input {...bind('sellingPrice')} inputMode="decimal" placeholder="0.00" required />
            </Field>
            <FormButtons busy={save.isPending} back="/products/list" label={mode === 'new' ? 'Create product' : 'Save changes'} />
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

// ---------- A customer's own prices ----------

interface CustomerPriceRow {
  priceId: number
  product: Product
  sellingPrice: number
}

export function CustomerPricesPage() {
  const { data, error } = usePage<PageData & { customer: Customer; products: Product[]; prices: CustomerPriceRow[] }>()
  const save = useSave('/prices/save', { formView: 'new-price' })
  const errors = fieldErrors(save)
  const [productId, setProductId] = useState('')
  const [price, setPrice] = useState('')
  if (!data) return <PageState error={error} />
  const selected = productId || String(data.products[0]?.productId ?? '')

  function submit(e: FormEvent) {
    e.preventDefault()
    save.mutate({ customerId: data!.customer.customerId, productId: selected, sellingPrice: price }, { onSuccess: () => setPrice('') })
  }

  return (
    <>
      <PageHeader
        title="Products & prices"
        description={<CustomerLink customer={data.customer} />}
        actions={<Button variant="outline" asChild><Link to={`/orders/neworder?customerId=${data.customer.customerId}`}><Plus /> New order</Link></Button>}
      />
      <div className="grid gap-6 lg:grid-cols-[22rem_1fr]">
        <Card className="h-fit">
          <CardHeader><CardTitle>Set a price</CardTitle></CardHeader>
          <CardContent>
            <form onSubmit={submit} className="grid gap-4">
              <Field label="Product" error={errors.productId}>
                <Select value={selected} onValueChange={setProductId}>
                  <SelectTrigger className="w-full"><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {data.products.map((p) => (
                      <SelectItem key={p.productId} value={String(p.productId)}>
                        {p.name} · list {peso(p.sellingPrice)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
              <Field label="This customer's price (PHP)" htmlFor="price" error={errors.sellingPrice}>
                <Input id="price" inputMode="decimal" placeholder="0.00" value={price} onChange={(e) => setPrice(e.target.value)} required />
              </Field>
              <Button type="submit" disabled={save.isPending}>{save.isPending ? 'Saving…' : 'Set price'}</Button>
            </form>
          </CardContent>
        </Card>
        <DataTable
          searchable={false}
          data={data.prices}
          emptyText="This customer pays the list prices."
          columns={[
            { id: 'product', header: 'Product', accessorFn: (p) => p.product.name },
            { id: 'list', header: 'List price', accessorFn: (p) => p.product.sellingPrice, cell: ({ getValue }) => peso(getValue()), meta: { numeric: true } },
            { accessorKey: 'sellingPrice', header: "Customer's price", cell: ({ getValue }) => <strong>{peso(getValue())}</strong>, meta: { numeric: true } },
          ]}
        />
      </div>
    </>
  )
}

// ---------- Containers ----------

export function ReturnContainersPage() {
  const { data, error } = usePage<PageData & { container: ContainerActivity; customer: Customer }>()
  const save = useSave('/container/save', { formView: 'return-container' })
  const { values, bind } = useFormValues<Record<string, unknown>>(data?.container as unknown as Record<string, unknown> | undefined)
  const errors = fieldErrors(save)
  if (!data || !values) return <PageState error={error} />

  function submit(e: FormEvent) {
    e.preventDefault()
    const v = values!
    save.mutate({ containerId: v.containerId as number, customerId: v.customerId as number, status: v.status as string, roundCount: v.roundCount as string, slimCount: v.slimCount as string })
  }

  return (
    <div className="max-w-xl">
      <PageHeader title="Return containers" description={`${data.customer.name} has ${data.customer.totalRound} round and ${data.customer.totalSlim} slim borrowed.`} />
      <Card>
        <CardContent>
          <form onSubmit={submit} className="grid gap-5 sm:grid-cols-2">
            <Field label="Round containers returned" htmlFor="roundCount" error={errors.roundCount}>
              <Input {...bind('roundCount')} type="number" min={0} className="tint-round" autoFocus />
            </Field>
            <Field label="Slim containers returned" htmlFor="slimCount" error={errors.slimCount}>
              <Input {...bind('slimCount')} type="number" min={0} className="tint-slim" />
            </Field>
            <FormButtons busy={save.isPending} back={`/customers/view?customerId=${data.customer.customerId}`} label="Record return" />
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

export function ContainerBalancesPage() {
  const { data, error } = usePage<PageData & { containersList: Customer[]; runningRound: number; runningSlim: number; dateToday: string }>()
  const columns = useMemo<ColumnDef<Customer>[]>(
    () => [
      { accessorKey: 'name', header: 'Customer', cell: ({ row }) => <CustomerLink customer={row.original} /> },
      { accessorKey: 'totalRound', header: 'Round', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-round' } },
      { accessorKey: 'totalSlim', header: 'Slim', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-slim' } },
      {
        id: 'return', header: '', enableSorting: false,
        cell: ({ row }) => (
          <div className="flex justify-end print:hidden">
            <Button size="sm" variant="outline" asChild>
              <Link to={`/container/return?cid=${row.original.customerId}`}><Undo2 /> Return</Link>
            </Button>
          </div>
        ),
      },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="Container balances" description="Containers each customer has borrowed right now." actions={<PrintButton />} />
      <div className="mb-6 grid max-w-xl gap-4 sm:grid-cols-2">
        <Stat label="Round out" value={data.runningRound} tone="round" />
        <Stat label="Slim out" value={data.runningSlim} tone="slim" />
      </div>
      <DataTable
        columns={columns}
        data={data.containersList}
        initialSort={[{ id: 'totalRound', desc: true }]}
        footer={
          <TableRow>
            <TableCell className="font-semibold">Total</TableCell>
            <TableCell className="tint-round text-right tabular font-semibold">{data.runningRound}</TableCell>
            <TableCell className="tint-slim text-right tabular font-semibold">{data.runningSlim}</TableCell>
            <TableCell />
          </TableRow>
        }
      />
    </>
  )
}

interface ContainerDay {
  customer: Customer
  roundTotalDelivered: number
  slimTotalDelivered: number
  roundTotalReturned: number
  slimTotalReturned: number
}

export function ContainerHistoryPage() {
  const { data, error } = usePage<PageData & {
    containerDaily: ContainerDay[]; datePicked: string
    totalRoundDelivered: number; totalSlimDelivered: number; totalRoundReturned: number; totalSlimReturned: number
  }>()
  const columns = useMemo<ColumnDef<ContainerDay>[]>(
    () => [
      { id: 'name', header: 'Customer', accessorFn: (r) => r.customer.name, cell: ({ row }) => <CustomerLink customer={row.original.customer} /> },
      { id: 'address', header: 'Address', accessorFn: (r) => r.customer.address ?? '' },
      { accessorKey: 'roundTotalDelivered', header: 'Round delivered', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-round' } },
      { accessorKey: 'slimTotalDelivered', header: 'Slim delivered', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-slim' } },
      { accessorKey: 'roundTotalReturned', header: 'Round returned', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-round' } },
      { accessorKey: 'slimTotalReturned', header: 'Slim returned', cell: ({ getValue }) => <Count value={getValue<number>()} />, meta: { numeric: true, className: 'tint-slim' } },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader title="Container history" description={data.datePicked} actions={<><DateFilter /><PrintButton /></>} />
      <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Round delivered" value={data.totalRoundDelivered} tone="round" />
        <Stat label="Slim delivered" value={data.totalSlimDelivered} tone="slim" />
        <Stat label="Round returned" value={data.totalRoundReturned} tone="round" />
        <Stat label="Slim returned" value={data.totalSlimReturned} tone="slim" />
      </div>
      <DataTable columns={columns} data={data.containerDaily} emptyText="No containers went out or came back on this day." />
    </>
  )
}
