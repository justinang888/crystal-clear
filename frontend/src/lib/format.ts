/** Money in pesos and dates in shop time (Asia/Manila), whatever the browser's own time zone. */

const TIME_ZONE = 'Asia/Manila'

const money = new Intl.NumberFormat('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

/** 1234.5 -> "1,234.50"; accepts the server's numbers and pre-formatted strings such as "605.00". */
export function peso(value: unknown): string {
  if (value === null || value === undefined || value === '') return ''
  const n = typeof value === 'number' ? value : Number(String(value).replace(/,/g, ''))
  return Number.isFinite(n) ? money.format(n) : String(value)
}

export function toNumber(value: unknown): number {
  if (value === null || value === undefined || value === '') return 0
  const n = typeof value === 'number' ? value : Number(String(value).replace(/,/g, ''))
  return Number.isFinite(n) ? n : 0
}

function parts(iso: string | number | Date) {
  const formatted = new Intl.DateTimeFormat('en-CA', {
    timeZone: TIME_ZONE,
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23',
  }).formatToParts(new Date(iso))
  const get = (type: string) => formatted.find((p) => p.type === type)?.value ?? '00'
  return { y: get('year'), m: get('month'), d: get('day'), h: get('hour'), min: get('minute'), s: get('second') }
}

/** "2026-11-07" in shop time: the value for <input type="date"> and for java.sql.Date fields. */
export function isoDate(value: string | number | Date | null | undefined = new Date()): string {
  if (value === null || value === undefined) return ''
  const p = parts(value)
  return `${p.y}-${p.m}-${p.d}`
}

/** "2026-11-07 09:15:00" in shop time: how an order's createdAt (java.sql.Timestamp) is posted back. */
export function timestampParam(value: string | number | Date | null | undefined): string {
  if (value === null || value === undefined) return ''
  const p = parts(value)
  return `${p.y}-${p.m}-${p.d} ${p.h}:${p.min}:${p.s}`
}

/** "2026-11" for <input type="month">. */
export function isoMonth(value: Date = new Date()): string {
  return isoDate(value).slice(0, 7)
}

/** "Nov 7, 2026". */
export function displayDate(value: string | number | Date | null | undefined): string {
  if (value === null || value === undefined || value === '') return ''
  return new Intl.DateTimeFormat('en-PH', { timeZone: TIME_ZONE, month: 'short', day: 'numeric', year: 'numeric' })
    .format(new Date(value))
}

/** "Nov 7, 2026, 9:15 AM". */
export function displayDateTime(value: string | number | Date | null | undefined): string {
  if (value === null || value === undefined || value === '') return ''
  return new Intl.DateTimeFormat('en-PH', {
    timeZone: TIME_ZONE, month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit',
  }).format(new Date(value))
}
