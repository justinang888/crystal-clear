import { useState, type FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { isoDate, isoMonth } from '@/lib/format'
import { cn } from '@/lib/utils'

export function StatusBadge({ status }: { status: string }) {
  return (
    <Badge
      variant="outline"
      className={cn(
        'rounded-full',
        status === 'Pending' && 'border-orange-300 bg-orange-50 text-orange-800 dark:border-orange-800 dark:bg-orange-950 dark:text-orange-200',
        status === 'Delivered' && 'border-emerald-300 bg-emerald-50 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-200',
        status === 'Cancelled' && 'text-muted-foreground',
      )}
    >
      {status}
    </Badge>
  )
}

/**
 * Picks the day a report shows. The reports read ?d= as "yyyy-MM-dd" (or "today");
 * month reports read "MM-yyyy".
 */
export function DateFilter({ month = false, label = 'Show' }: { month?: boolean; label?: string }) {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const current = params.get('d')
  const initial = month
    ? current && /^\d{2}-\d{4}$/.test(current)
      ? `${current.slice(3)}-${current.slice(0, 2)}`
      : isoMonth()
    : current && /^\d{4}-\d{2}-\d{2}$/.test(current)
      ? current
      : isoDate()
  const [value, setValue] = useState(initial)

  function submit(e: FormEvent) {
    e.preventDefault()
    if (!value) return
    const d = month ? `${value.slice(5, 7)}-${value.slice(0, 4)}` : value
    const next = new URLSearchParams(params)
    next.set('d', d)
    navigate({ search: next.toString() })
  }

  return (
    <form onSubmit={submit} className="flex items-center gap-2">
      <Input
        type={month ? 'month' : 'date'}
        value={value}
        onChange={(e) => setValue(e.target.value)}
        className="w-auto"
        aria-label={month ? 'Month' : 'Date'}
        required
      />
      <Button type="submit" variant="secondary">
        {label}
      </Button>
    </form>
  )
}

/**
 * Picks a range of days for screens that read ?from=yyyy-MM-dd&to=yyyy-MM-dd (both included).
 * {@code from} and {@code to} are what the server shows now, so the fields start there.
 */
export function DateRangeFilter({ from, to }: { from: string; to: string }) {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [start, setStart] = useState(from)
  const [end, setEnd] = useState(to)

  function submit(e: FormEvent) {
    e.preventDefault()
    if (!start || !end) return
    const next = new URLSearchParams(params)
    next.delete('d')
    next.set('from', start)
    next.set('to', end)
    navigate({ search: next.toString() })
  }

  return (
    <form onSubmit={submit} className="flex flex-wrap items-center gap-2">
      <Input type="date" value={start} onChange={(e) => setStart(e.target.value)} className="w-auto" aria-label="From" required />
      <span className="text-sm text-muted-foreground">to</span>
      <Input type="date" value={end} min={start || undefined} onChange={(e) => setEnd(e.target.value)} className="w-auto" aria-label="To" required />
      <Button type="submit" variant="secondary">
        Show
      </Button>
    </form>
  )
}

/** A count cell that dims zeros, so the non-zero counts in busy tables stand out. */
export function Count({ value }: { value: number | string | null | undefined }) {
  const n = Number(value ?? 0)
  return n ? <>{n}</> : <span className="text-muted-foreground/50">0</span>
}
