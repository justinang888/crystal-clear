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
  /** Asked not to be texted (the switch on the customer's page). */
  smsOptOut: boolean
}

/** An overdue customer on the text reminders screen; skipReason is null when they can be texted. */
export interface SmsCandidate {
  customerId: number
  name: string
  /** The name the message greets: the contact person when set. */
  greetingName: string
  mobile: string | null
  lastDelivered: string
  daysSince: number
  orderInterval: number
  lastTexted: string | null
  skipReason: string | null
}

/** One row of the texting log. */
export interface SmsLogRow {
  smsMessageId: number
  createdAt: string
  customerId: number
  customerName: string | null
  phoneNumber: string
  body: string
  state: string
  error: string | null
  createdBy: string
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

export type ExpensePaymentType = 'CASH' | 'GCASH' | 'BANK_TRANSFER'

export const EXPENSE_PAYMENT_TYPES: { value: ExpensePaymentType; label: string }[] = [
  { value: 'CASH', label: 'Cash' },
  { value: 'GCASH', label: 'GCash' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
]

export function paymentTypeLabel(value: string | null | undefined) {
  return EXPENSE_PAYMENT_TYPES.find((t) => t.value === value)?.label ?? 'Cash'
}

/** How a customer paid. An order's amount paid on delivery is cash. */
export const CUSTOMER_PAYMENT_TYPES: { value: string; label: string }[] = [
  { value: 'CASH', label: 'Cash' },
  { value: 'CHECK', label: 'Check' },
  { value: 'GCASH', label: 'GCash' },
  { value: 'BANK_TRANSFER', label: 'Bank Transfer' },
]

export function customerPaymentTypeLabel(value: string | null | undefined) {
  return CUSTOMER_PAYMENT_TYPES.find((t) => t.value === value?.toUpperCase())?.label ?? value ?? ''
}

export interface Expense {
  expenseId: number
  amount: number | null
  description: string | null
  createdAt: string
  /** How it was paid; only Cash comes out of the drawer. */
  paymentType: ExpensePaymentType
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
  /** The days shown, both included (the Orders screen): "2026-03-01". */
  from?: string
  to?: string
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
  paidGcash: number | null
  paidBankTransfer: number | null
  expenseAmount: number | null
  /** For an expense row: "Cash", "GCash" or "Bank Transfer". */
  expensePaymentType: string | null
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
  /** The expenses paid in cash: only these lower net cash. */
  totalCashExpenses: string
  /** GCash and bank transfer expenses. */
  totalOtherExpenses: string
  totalGcashExpenses: string
  totalBankTransferExpenses: string
  totalCashPayments: string
  totalCheckPayments: string
  totalGcashPayments: string
  totalBankTransferPayments: string
  totalDebt: string
  /** Each method's money in minus its expenses: the drawer, the GCash wallet, the bank account. */
  netCash: string
  netGcash: string
  netBankTransfer: string
  /** The days shown, both included: "2026-03-01". */
  from?: string
  to?: string
  /** Customers with any activity in the days shown (delivered order, payment, returned containers). */
  totalCustomers?: number
  /** Customers with a delivered order in the days shown. */
  customersServed?: number
}

/**
 * The sales totals Daily Sales and the Monthly Summary share (SalesTotals on the server): money is a
 * string with two decimals, counts are numbers.
 */
export interface SalesTotalsFields {
  totalCashPayments: string
  totalCheckPayments: string
  totalGcashPayments: string
  totalBankTransferPayments: string
  totalCashExpenses: string
  totalGcashExpenses: string
  totalBankTransferExpenses: string
  totalOtherExpenses: string
  totalExpenses: string
  netCash: string
  netGcash: string
  netBankTransfer: string
  totalDebt: string
  totalRoundDelivered: number
  totalSlimDelivered: number
  totalRoundReturned: number
  totalSlimReturned: number
  totalCustomers: number
  customersServed: number
}

/** A range of days (a month by default), on Daily Sales' rules: each row equals that day's Daily Sales. */
export interface MonthlyData extends PageData, SalesTotalsFields {
  /** "Mar 2026" for a whole month, else "Mar 15 2026 – Mar 17 2026". */
  monthYear: string
  /** The days shown, both included: "2026-03-01". */
  from: string
  to: string
  /** One row per day, from first to last. */
  days: (SalesTotalsFields & { date: string })[]
}
