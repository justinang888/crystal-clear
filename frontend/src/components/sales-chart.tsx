import { useState } from 'react'

import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { peso, toNumber } from '@/lib/format'
import type { SalesPeriod } from '@/lib/types'

type Range = 'day' | 'week' | 'month'

const RANGES: Record<Range, { tab: string; span: string }> = {
  day: { tab: 'Days', span: 'last 7 days' },
  week: { tab: 'Weeks', span: 'last 8 weeks' },
  month: { tab: 'Months', span: 'last 12 months' },
}

const STORAGE_KEY = 'aurora-sales-range'

function savedRange(): Range {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    if (value === 'day' || value === 'week' || value === 'month') return value
  } catch {
    // Storage can be blocked; the chart then opens on days.
  }
  return 'day'
}

// The server sends plain dates ("2026-11-02"). Reading and printing them in UTC keeps the day as sent.
const asDate = (iso: string) => new Date(`${iso}T00:00:00Z`)
const fmt = (options: Intl.DateTimeFormatOptions) => new Intl.DateTimeFormat('en-PH', { ...options, timeZone: 'UTC' })
const weekday = fmt({ weekday: 'short' })
const dayOfMonth = fmt({ day: 'numeric' })
const monthDay = fmt({ month: 'short', day: 'numeric' })
const monthDayYear = fmt({ month: 'short', day: 'numeric', year: 'numeric' })
const fullDay = fmt({ weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })
const shortMonth = fmt({ month: 'short' })
const monthYear = fmt({ month: 'long', year: 'numeric' })

/** The label under a bar; the last bar is the current period. */
function axisLabel(range: Range, period: SalesPeriod, index: number, last: boolean) {
  const start = asDate(period.start)
  if (range === 'day') return last ? 'Today' : `${weekday.format(start)} ${dayOfMonth.format(start)}`
  if (range === 'week') return last ? 'This week' : monthDay.format(start)
  const month = shortMonth.format(start)
  // Name the year where the months cross into a new one, and on the first bar.
  return index === 0 || start.getUTCMonth() === 0 ? `${month} ’${String(start.getUTCFullYear()).slice(2)}` : month
}

/** The period in full, for the tooltip and the table. */
function periodName(range: Range, period: SalesPeriod) {
  const start = asDate(period.start)
  if (range === 'day') return fullDay.format(start)
  if (range === 'week') return `${monthDay.format(start)} – ${monthDayYear.format(asDate(period.end))}`
  return monthYear.format(start)
}

/** About four round steps (1, 2, 2.5 or 5 x 10^n) from zero to just above the biggest value. */
function ticks(max: number) {
  if (max <= 0) return [0]
  const rough = max / 4
  const power = 10 ** Math.floor(Math.log10(rough))
  const step = [1, 2, 2.5, 5, 10].map((m) => m * power).find((s) => s >= rough) ?? rough
  const top = Math.ceil(max / step) * step
  return Array.from({ length: Math.round(top / step) + 1 }, (_, i) => i * step)
}

const fullNumber = new Intl.NumberFormat('en-PH', { maximumFractionDigits: 0 })
const compactNumber = new Intl.NumberFormat('en-PH', { notation: 'compact', maximumFractionDigits: 1 })

export function SalesChart({ byDay, byWeek, byMonth }: { byDay: SalesPeriod[]; byWeek: SalesPeriod[]; byMonth: SalesPeriod[] }) {
  const [range, setRange] = useState<Range>(savedRange)
  const series = { day: byDay, week: byWeek, month: byMonth }[range]
  const values = series.map((p) => toNumber(p.total))
  const total = values.reduce((sum, v) => sum + v, 0)
  const scale = ticks(Math.max(...values, 0))
  const top = scale[scale.length - 1]
  const axisText = (n: number) => (top >= 10_000 ? compactNumber : fullNumber).format(n)

  function choose(value: string) {
    const next = value as Range
    setRange(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      // Not remembered; nothing else changes.
    }
  }

  // Each period owns an equal slot; its point sits in the middle, so points line up with the labels.
  const x = (i: number) => ((i + 0.5) / series.length) * 100
  const y = (value: number) => (top ? (value / top) * 100 : 0)
  const line = values.map((v, i) => `${i ? 'L' : 'M'}${x(i)},${100 - y(v)}`).join(' ')
  const area = `${line} L${x(values.length - 1)},100 L${x(0)},100 Z`

  return (
    <Card className="gap-5 py-5">
      <CardHeader className="px-5">
        <CardTitle>Sales</CardTitle>
        <CardDescription>Delivered orders, {RANGES[range].span}</CardDescription>
        <p className="mt-1 text-2xl font-semibold tracking-tight">{peso(total)}</p>
        <CardAction>
          <Tabs value={range} onValueChange={choose}>
            <TabsList aria-label="Show sales by">
              {(Object.keys(RANGES) as Range[]).map((r) => (
                <TabsTrigger key={r} value={r}>
                  {RANGES[r].tab}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>
        </CardAction>
      </CardHeader>
      <CardContent className="px-5">
        {/* pt-2 leaves room for the top axis label, which is centred on its gridline. */}
        <div className="grid grid-cols-[auto_1fr] gap-x-3 pt-2" aria-hidden="true">
          {/* Y axis: round numbers, the same scale as the hairline grid beside it. */}
          <div className="relative h-52 text-right text-xs text-muted-foreground tabular">
            {scale.map((tick) => (
              <span key={tick} className="absolute right-0 block -translate-y-1/2 leading-none" style={{ top: `${100 - y(tick)}%` }}>
                {axisText(tick)}
              </span>
            ))}
            {/* Keeps the column wide enough for the longest label. */}
            <span className="invisible block h-0 overflow-hidden">{axisText(top)}</span>
          </div>

          <div className="relative h-52">
            {scale.map((tick) => (
              <div key={tick} className="absolute inset-x-0 border-t border-border" style={{ top: `${100 - y(tick)}%` }} />
            ))}

            {/* The line and its 10% wash; the viewBox stretches to the box, the stroke stays 2px. */}
            <svg className="absolute inset-0 size-full overflow-visible" viewBox="0 0 100 100" preserveAspectRatio="none">
              <path d={area} className="fill-chart-1/10" />
              <path
                d={line}
                fill="none"
                className="stroke-chart-1"
                strokeWidth={2}
                strokeLinejoin="round"
                strokeLinecap="round"
                vectorEffect="non-scaling-stroke"
              />
            </svg>

            {total === 0 && (
              <p className="absolute inset-0 flex items-center justify-center text-sm text-muted-foreground">No delivered orders yet in this period</p>
            )}

            <div className="absolute inset-0 flex">
              {series.map((period, i) => {
                const current = i === series.length - 1
                return (
                  <Tooltip key={period.start}>
                    <TooltipTrigger asChild>
                      {/* The whole slot is the hover and focus target, not just the point. */}
                      <div tabIndex={0} className="group relative h-full flex-1 outline-none">
                        {/* Crosshair: a hairline down from the point being read. */}
                        <span className="absolute inset-y-0 left-1/2 hidden w-px -translate-x-1/2 bg-muted-foreground/40 group-hover:block group-focus-visible:block" />
                        {/* The point: 10px, with a 2px ring in the card color where it crosses the line. */}
                        <span
                          className="absolute left-1/2 size-2.5 -translate-x-1/2 translate-y-1/2 rounded-full bg-chart-1 ring-2 ring-card transition-transform group-hover:scale-125 group-focus-visible:scale-125 group-focus-visible:ring-ring"
                          style={{ bottom: `${y(values[i])}%` }}
                        />
                      </div>
                    </TooltipTrigger>
                    <TooltipContent side="top" className="flex-col items-start gap-0.5">
                      <span>
                        {periodName(range, period)}
                        {current && ' (so far)'}
                      </span>
                      <span className="text-sm font-semibold tabular">{peso(period.total)}</span>
                    </TooltipContent>
                  </Tooltip>
                )
              })}
            </div>
          </div>

          <div />
          <div className="mt-3 flex text-center text-xs text-muted-foreground">
            {series.map((period, i) => (
              <span key={period.start} className="flex-1 truncate px-0.5">
                {axisLabel(range, period, i, i === series.length - 1)}
              </span>
            ))}
          </div>
        </div>

        {/* The same numbers for screen readers. The wrapper does the hiding: a table ignores the
            1px size that sr-only relies on, so on the table itself it kept its full height and
            pushed blank space onto the bottom of the page. */}
        <div className="sr-only">
          <table>
            <caption>Sales by {range}, {RANGES[range].span}, in pesos</caption>
            <thead>
              <tr>
                <th scope="col">Period</th>
                <th scope="col">Sales</th>
              </tr>
            </thead>
            <tbody>
              {series.map((period, i) => (
                <tr key={period.start}>
                  <th scope="row">
                    {periodName(range, period)}
                    {i === series.length - 1 && ' (so far)'}
                  </th>
                  <td>{peso(period.total)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  )
}
