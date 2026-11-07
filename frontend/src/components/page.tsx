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

/** A labelled number, for report totals and the dashboard. */
export function Stat({
  label,
  value,
  icon,
  tone,
  className,
}: {
  label: string
  value: ReactNode
  icon?: ReactNode
  tone?: 'round' | 'slim' | 'warning' | 'danger'
  className?: string
}) {
  return (
    <Card className={cn('gap-0 py-4', className)}>
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
          <div className="text-xl font-semibold break-words tabular">{value}</div>
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
