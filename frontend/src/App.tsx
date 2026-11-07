import type { ReactNode } from 'react'
import { createBrowserRouter, Link, Navigate, RouterProvider, useLocation } from 'react-router'
import { CircleHelp, SearchX } from 'lucide-react'

import { AppLayout } from '@/components/app-layout'
import { PageHeader } from '@/components/page'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { BASE } from '@/lib/api'
import {
  ContainerBalancesPage,
  ContainerHistoryPage,
  CustomerPricesPage,
  ProductFormPage,
  ProductsPage,
  ReturnContainersPage,
} from '@/pages/catalog'
import {
  ContainerBalanceFormPage,
  CustomerFormPage,
  CustomerPage,
  CustomersPage,
  DailyDueDatesPage,
  DueDatesPage,
  InactiveCustomersPage,
  MonthlyActivePage,
} from '@/pages/customers'
import { DashboardPage } from '@/pages/dashboard'
import { LoginPage } from '@/pages/login'
import {
  ArSummaryPage,
  DebtFormPage,
  DebtsPage,
  ExpenseFormPage,
  ExpensesPage,
  PaymentFormPage,
  PaymentsPage,
} from '@/pages/money'
import { DailySalesPage, MonthlySummaryPage, OrderFormPage, OrdersPage, PendingOrdersPage } from '@/pages/orders'

/** Keyed by address so moving between two records of the same kind starts a fresh form. */
function Keyed({ children }: { children: (key: string) => ReactNode }) {
  const location = useLocation()
  return <>{children(location.pathname + location.search)}</>
}

// Every address the old JSP pages had still opens the same screen.
const router = createBrowserRouter(
  [
    { path: '/login', element: <LoginPage /> },
    {
      element: <AppLayout />,
      children: [
        { index: true, element: <Navigate to="/dashboard" replace /> },
        { path: 'dashboard', element: <DashboardPage /> },

        { path: 'customers/list', element: <CustomersPage /> },
        { path: 'customers/view', element: <Keyed>{(k) => <CustomerPage key={k} />}</Keyed> },
        { path: 'customers/new', element: <CustomerFormPage mode="new" /> },
        { path: 'customers/edit', element: <Keyed>{(k) => <CustomerFormPage key={k} mode="edit" />}</Keyed> },
        { path: 'customers/containerBalances', element: <Keyed>{(k) => <ContainerBalanceFormPage key={k} />}</Keyed> },
        { path: 'customers/duedates', element: <DueDatesPage /> },
        { path: 'customers/dailyduedates', element: <DailyDueDatesPage /> },
        { path: 'customers/listinactive', element: <InactiveCustomersPage /> },
        { path: 'customers/listactive', element: <MonthlyActivePage /> },

        { path: 'orders/list', element: <OrdersPage /> },
        { path: 'orders/pending', element: <PendingOrdersPage /> },
        { path: 'orders/neworder', element: <Keyed>{(k) => <OrderFormPage key={k} mode="new" />}</Keyed> },
        { path: 'orders/edit', element: <Keyed>{(k) => <OrderFormPage key={k} mode="edit" />}</Keyed> },
        { path: 'orders/daily', element: <DailySalesPage /> },
        { path: 'orders/monthly', element: <MonthlySummaryPage /> },

        { path: 'payments/new', element: <Keyed>{(k) => <PaymentFormPage key={k} />}</Keyed> },
        { path: 'payments/list', element: <PaymentsPage /> },
        { path: 'debts/new', element: <Keyed>{(k) => <DebtFormPage key={k} />}</Keyed> },
        { path: 'debts/list', element: <DebtsPage /> },
        { path: 'debts/listAll', element: <ArSummaryPage /> },
        { path: 'expenses/list', element: <ExpensesPage /> },
        { path: 'expenses/new', element: <ExpenseFormPage /> },

        { path: 'products/list', element: <ProductsPage /> },
        { path: 'products/new', element: <ProductFormPage mode="new" /> },
        { path: 'products/edit/:productId', element: <Keyed>{(k) => <ProductFormPage key={k} mode="edit" />}</Keyed> },
        { path: 'prices/list/:customerId', element: <Keyed>{(k) => <CustomerPricesPage key={k} />}</Keyed> },

        { path: 'container/return', element: <Keyed>{(k) => <ReturnContainersPage key={k} />}</Keyed> },
        { path: 'container/listAll', element: <ContainerBalancesPage /> },
        { path: 'container/history', element: <ContainerHistoryPage /> },

        { path: 'help', element: <HelpPage /> },
        { path: '*', element: <NotFoundPage /> },
      ],
    },
  ],
  { basename: BASE },
)

export default function App() {
  return <RouterProvider router={router} />
}

function HelpPage() {
  return (
    <>
      <PageHeader title="Help" />
      <Card className="max-w-2xl">
        <CardContent className="flex gap-4">
          <CircleHelp className="mt-0.5 size-5 shrink-0 text-muted-foreground" />
          <div className="space-y-2 text-sm">
            <p>Start an order from <Link to="/customers/list" className="text-primary hover:underline">Customers</Link>: pick the customer, then <strong>Order</strong>.</p>
            <p>Deliver or cancel pending orders from <Link to="/orders/list" className="text-primary hover:underline">Orders</Link>. Only pending orders can be cancelled.</p>
            <p>Every report has a <strong>Print</strong> button; the menu and buttons are left off the printout.</p>
          </div>
        </CardContent>
      </Card>
    </>
  )
}

function NotFoundPage() {
  return (
    <div className="flex flex-col items-center py-24 text-center">
      <SearchX className="mb-4 size-10 text-muted-foreground" />
      <h1 className="text-xl font-semibold">This page doesn't exist</h1>
      <p className="mt-1 text-sm text-muted-foreground">The address may be old or mistyped.</p>
      <Button asChild className="mt-6"><Link to="/dashboard">Go to the dashboard</Link></Button>
    </div>
  )
}
