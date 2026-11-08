import type { ReactNode } from 'react'
import { AlertTriangle, Printer } from 'lucide-react'

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'

/** Title row of a screen: the title, a short line under it, and the screen's actions. */
export function PageHeader({
  title,
  description,
  actions,
}: {
  title: ReactNode
  description?: ReactNode
  actions?: ReactNode
}) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
      <div className="min-w-0">
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2 print:hidden">{actions}</div>}
    </div>
  )
}

export function PrintButton() {
  return (
    <Button variant="outline" onClick={() => window.print()}>
      <Printer /> Print
    </Button>
  )
}

type StatTone = 'round' | 'slim' | 'warning' | 'danger'

/**
 * The whole tile tinted in its tone (background, border and number), for the dashboard.
 * The solid left strip keeps the tones apart on warm or washed-out screens, where a light
 * cream or pink tint can look the same as white.
 */
const FILLED: Record<StatTone | 'none', { card: string; value: string }> = {
  none: { card: 'border-primary/30 border-l-primary bg-primary/10', value: 'text-primary' },
  round: { card: 'border-amber-500/40 border-l-amber-500 bg-amber-500/15', value: 'text-amber-700 dark:text-amber-300' },
  slim: { card: 'border-sky-500/40 border-l-sky-500 bg-sky-500/15', value: 'text-sky-700 dark:text-sky-300' },
  warning: { card: 'border-orange-500/40 border-l-orange-500 bg-orange-500/15', value: 'text-orange-700 dark:text-orange-300' },
  danger: { card: 'border-red-500/40 border-l-red-500 bg-red-500/15', value: 'text-red-700 dark:text-red-300' },
}

/** A labelled number, for report totals and the dashboard. */
export function Stat({
  label,
  value,
  icon,
  tone,
  filled = false,
  className,
}: {
  label: string
  value: ReactNode
  icon?: ReactNode
  tone?: StatTone
  /** Tint the whole tile, not just the icon. */
  filled?: boolean
  className?: string
}) {
  const fill = filled ? FILLED[tone ?? 'none'] : undefined
  return (
    <Card className={cn('gap-0 py-4', fill && 'border-l-4', fill?.card, className)}>
      <CardContent className="flex items-center gap-3 px-4">
        {icon && (
          <span
            className={cn(
              'flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary [&_svg]:size-5',
              tone === 'round' && 'bg-amber-500/15 text-amber-700 dark:text-amber-300',
              tone === 'slim' && 'bg-sky-500/15 text-sky-700 dark:text-sky-300',
              tone === 'warning' && 'bg-orange-500/15 text-orange-700 dark:text-orange-300',
              tone === 'danger' && 'bg-red-500/15 text-red-700 dark:text-red-300',
            )}
          >
            {icon}
          </span>
        )}
        <div className="min-w-0">
          <div className="text-xs font-medium tracking-wide text-muted-foreground uppercase">{label}</div>
          <div className={cn('text-xl font-semibold break-words tabular', fill?.value)}>{value}</div>
        </div>
      </CardContent>
    </Card>
  )
}

/** Placeholder while a screen's data loads, or the reason it could not. */
export function PageState({ error }: { error?: Error | null }) {
  if (error) {
    return (
      <Alert variant="destructive">
        <AlertTriangle />
        <AlertTitle>This screen could not load</AlertTitle>
        <AlertDescription>{error.message} Try again, or go back to the dashboard.</AlertDescription>
      </Alert>
    )
  }
  return (
    <div className="space-y-4" aria-busy="true" aria-label="Loading">
      <Skeleton className="h-8 w-64" />
      <div className="grid gap-4 sm:grid-cols-3">
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
      </div>
      <Skeleton className="h-72" />
    </div>
  )
}

/** A form field: label, control and the server's validation message. */
export function Field({
  label,
  htmlFor,
  error,
  hint,
  children,
  className,
}: {
  label: string
  htmlFor?: string
  error?: string
  hint?: string
  children: ReactNode
  className?: string
}) {
  return (
    <div className={cn('grid gap-1.5', className)} data-invalid={error ? true : undefined}>
      <Label htmlFor={htmlFor} className={cn(error && 'text-destructive')}>
        {label}
      </Label>
      {children}
      {error ? (
        <p className="text-sm text-destructive" role="alert">
          {error}
        </p>
      ) : (
        hint && <p className="text-sm text-muted-foreground">{hint}</p>
      )}
    </div>
  )
}
