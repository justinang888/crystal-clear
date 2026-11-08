/** Shapes of the controllers' models, as the JSON view sends them (see ModelJsonView). */

import type { PageData } from './api'

export interface Customer {
  customerId: number
  type: string
  name: string
  address: string | null
  contactName: string | null
  mainNumber: string | null
  alternateNumber: string | null
  emailAddress: string | null
  orderInterval: number
  totalRound: number
  totalSlim: number
}

export interface Order {
  orderId: number
  customerId: number
  deliveryReceiptNum: string
  status: 'Pending' | 'Delivered' | 'Cancelled' | string
  amountPaid: number
  totalAmount: number
  roundRefillOnlyCount: number
  slimRefillOnlyCount: number
  roundContainerOnlyCount: number
  slimContainerOnlyCount: number
  roundRefillWithContainerCount: number
  slimRefillWithContainerCount: number
  roundFreeCount: number
  slimFreeCount: number
  roundReturned: string
  slimReturned: string
  createdAt: string
  remarks: string | null
}

export interface Product {
  productId: number
  name: string
  description: string | null
  initialPrice: number | null
  sellingPrice: number | null
  createdAt: string | null
  updatedAt: string | null
}

export interface Payment {
  paymentId: number
  customerId: number
  amount: number | null
  withholdingTax: number | null
  paymentType: 'CASH' | 'CHECK' | string
  remarks: string | null
  createdAt: string
}

export interface Debt {
  debtId: number
  customerId: number
  amount: number
  remarks: string | null
  createdAt: string
  orderId: number | null
}

export interface Expense {
  expenseId: number
  amount: number | null
  description: string | null
  createdAt: string
}

export interface ContainerActivity {
  containerId: number
  customerId: number
  orderId: number
  status: string
  roundCount: number
  slimCount: number
  createdAt: string
}

export interface OrderRow {
  CustomerName: string
  formattedDate: string
  order: Order
}

export interface OrderProduct {
  orderProductId: number
  orderId: number
  productId: number
  quantity: number | null
}

export interface OrderForm {
  order: Order
  opList: OrderProduct[]
  orderInterval: number
}

export interface DashboardData extends PageData {
  pendingCount: number
  runningRound: number
  runningSlim: number
  /** What all customers owe, the same total as the A/R summary. */
  arTotal: number
  /** Delivered sales for the last 7 days, 8 weeks (Monday to Sunday) and 12 months, oldest first. */
  salesByDay: SalesPeriod[]
  salesByWeek: SalesPeriod[]
  salesByMonth: SalesPeriod[]
}

/** One bar of the dashboard's sales chart: first and last day ("2026-11-02") and the total. */
export interface SalesPeriod {
  start: string
  end: string
  total: number
}

export interface CustomersData extends PageData {
  customerPrices: { customer: Customer; refillPrice: number; mostRecentOrderDate: string | null; numberOfOrdersForTheMonth: number }[]
}

export interface CustomerData extends PageData {
  customer: Customer
  orders: Order[]
  payments: Payment[]
  containerHistory: ContainerActivity[]
  prices: { priceId: number; product: Product; sellingPrice: number }[]
  dueDate: string
  dueDateText: string
  mostRecentOrderDate: string
  totalBorrowedRound: number
  totalBorrowedSlim: number
  totalDebt: number
}

export interface CustomerFormData extends PageData {
  customerForm: Customer
  types?: string[]
}

export interface OrdersData extends PageData {
  orders: OrderRow[]
  pendingCount: number
  datePicked?: string
}

export interface OrderFormData extends PageData {
  opeForm: OrderForm
  productList: Product[]
  customerName: string
  customerAddress: string
  drNumber?: string
  createdAt?: string
  status?: string
}

export interface DailySale {
  order: Order | null
  customerName: string
  dateAndTime: string
  remarks: string | null
  balanceAmount: number | null
  paidCash: number | null
  paidCheck: number | null
  expenseAmount: number | null
  returnedRound: string | null
  returnedSlim: string | null
}

export interface DailySalesData extends PageData {
  datePicked: string
  dateParam: string
  dailySales: DailySale[]
  totalRoundDelivered: number
  totalSlimDelivered: number
  totalRoundReturned: number
  totalSlimReturned: number
  totalExpenses: string
  totalCashPayments: string
  totalCheckPayments: string
  totalDebt: string
  netCash: string
}

export interface MonthlyData extends PageData {
  monthYear: string
  dailyTotals: {
    date: string
    totalCash: number
    totalAr: number
    totalPayments: number
    totalExpenses: number
    totalDeliveredRound: number
    totalDeliveredSlim: number
    totalDeliveredContainers: number
  }[]
  grandTotalCash: number
  grandTotalAr: number
  grandTotalPayment: number
  grandTotalExpense: number
  grandTotalRound: number
  grandTotalSlim: number
  grandTotalContainers: number
}
