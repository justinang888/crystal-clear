import { useMemo, useState } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { History, MessageSquareText, Send, TriangleAlert } from 'lucide-react'
import { Link } from 'react-router'

import { DataTable } from '@/components/data-table'
import { PageHeader, PageState } from '@/components/page'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { usePage, useSave } from '@/hooks/use-page'
import type { PageData } from '@/lib/api'
import { displayDate, displayDateTime } from '@/lib/format'
import type { SmsCandidate, SmsLogRow } from '@/lib/types'
import { cn } from '@/lib/utils'

// ---------- How many texts a message takes ----------

// The GSM 03.38 alphabet every phone handles at 160 characters a text. Anything outside it
// (₱, curly quotes, emoji) switches the whole message to 70 characters a text.
const GSM_BASIC = new Set(
  '@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !"#¤%&\'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà',
)
const GSM_EXTENDED = new Set('^{}\\[~]|€') // two characters each

function textCount(message: string) {
  let gsmLength = 0
  const outside = new Set<string>()
  for (const ch of message) {
    if (GSM_BASIC.has(ch)) gsmLength += 1
    else if (GSM_EXTENDED.has(ch)) gsmLength += 2
    else outside.add(ch)
  }
  if (outside.size === 0) {
    return { length: gsmLength, texts: gsmLength <= 160 ? 1 : Math.ceil(gsmLength / 153), perText: 160, outside: [] as string[] }
  }
  const length = [...message].length
  return { length, texts: length <= 70 ? 1 : Math.ceil(length / 67), perText: 70, outside: [...outside] }
}

function fill(template: string, c: SmsCandidate) {
  return template.replaceAll('{name}', c.greetingName).replaceAll('{days}', String(c.daysSince))
}

// ---------- Compose ----------

interface ComposeData extends PageData {
  candidates: SmsCandidate[]
  template: string
  maxLength: number
  configured: boolean
  cooldownDays: number
  graceDays: number
  defaultIntervalDays: number
}

export function SmsComposePage() {
  const { data, error } = usePage<ComposeData>()
  if (!data) return <PageState error={error} />
  return <Compose key={data.candidates.map((c) => c.customerId).join()} data={data} />
}

function Compose({ data }: { data: ComposeData }) {
  const send = useSave('/sms/send')
  const sendable = data.candidates.filter((c) => !c.skipReason)
  const skipped = data.candidates.filter((c) => c.skipReason)
  const [template, setTemplate] = useState(data.template)
  const [chosen, setChosen] = useState<Set<number>>(() => new Set(sendable.map((c) => c.customerId)))

  const picked = sendable.filter((c) => chosen.has(c.customerId))
  // The longest filled-in message decides the warning: names differ in length.
  const longest = picked.map((c) => textCount(fill(template, c))).sort((a, b) => b.length - a.length)[0] ?? textCount(template)
  const tooLong = template.length > data.maxLength
  const canSend = data.configured && picked.length > 0 && template.trim() !== '' && !tooLong && !send.isPending

  function toggle(id: number, on: boolean) {
    setChosen((current) => {
      const next = new Set(current)
      if (on) next.add(id)
      else next.delete(id)
      return next
    })
  }

  return (
    <>
      <PageHeader
        title="Text reminders"
        description={`Customers whose last delivery was more than their usual interval plus ${data.graceDays} days ago.`}
        actions={
          <Button variant="outline" asChild>
            <Link to="/sms/list"><History /> Sent texts</Link>
          </Button>
        }
      />

      {!data.configured && (
        <Alert className="mb-4">
          <TriangleAlert />
          <AlertTitle>Texting isn't set up on this PC</AlertTitle>
          <AlertDescription>
            You can write and preview the messages, but Send stays off until the shop phone is connected (see the README,
            "Text reminders").
          </AlertDescription>
        </Alert>
      )}

      <div className="grid gap-4 lg:grid-cols-[minmax(0,2fr)_minmax(0,3fr)] lg:items-start">
        <Card className="gap-4 py-5 lg:sticky lg:top-16">
          <CardHeader className="px-5">
            <CardTitle>Message</CardTitle>
            <CardDescription>
              <code className="rounded bg-muted px-1">{'{name}'}</code> becomes the customer's name (or contact person),{' '}
              <code className="rounded bg-muted px-1">{'{days}'}</code> the days since their last delivery.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 px-5">
            <Label htmlFor="sms-template" className="sr-only">Message</Label>
            <Textarea
              id="sms-template"
              value={template}
              onChange={(e) => setTemplate(e.target.value)}
              rows={6}
              aria-invalid={tooLong || template.trim() === '' || undefined}
            />
            <p className={cn('text-sm', longest.texts > 1 || tooLong ? 'text-destructive' : 'text-muted-foreground')}>
              {tooLong
                ? `Too long: ${template.length} of ${data.maxLength} characters.`
                : `Longest message ${longest.length} characters: ${longest.texts === 1 ? '1 text' : `${longest.texts} texts`} each (${longest.perText} characters per text).`}
            </p>
            {longest.outside.length > 0 && (
              <p className="text-sm text-destructive">
                {longest.outside.map((ch) => `“${ch}”`).join(' ')} {longest.outside.length === 1 ? "isn't" : "aren't"} in the
                basic text alphabet, so each text holds only 70 characters. Use plain letters (for example P instead of ₱,
                ' instead of ’).
              </p>
            )}
            <AlertDialog>
              <AlertDialogTrigger asChild>
                <Button className="w-full" disabled={!canSend}>
                  <Send /> {picked.length === 1 ? 'Send 1 text' : `Send ${picked.length} texts`}
                </Button>
              </AlertDialogTrigger>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Send {picked.length === 1 ? '1 text' : `${picked.length} texts`} from the shop phone?</AlertDialogTitle>
                  <AlertDialogDescription>
                    Each customer gets the message shown under their name. Texts can't be taken back once the phone sends them.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>Not yet</AlertDialogCancel>
                  <AlertDialogAction
                    onClick={() => send.mutate({ customerIds: picked.map((c) => c.customerId).join(','), template })}
                  >
                    Send
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </CardContent>
        </Card>

        <div className="space-y-4">
          <Card className="gap-3 py-5">
            <CardHeader className="px-5">
              <CardTitle>To text ({picked.length} of {sendable.length})</CardTitle>
              <CardDescription>Untick anyone you'd rather not text this time.</CardDescription>
            </CardHeader>
            <CardContent className="px-5">
              {sendable.length === 0 ? (
                <p className="py-6 text-center text-sm text-muted-foreground">
                  No overdue customers can be texted right now.
                </p>
              ) : (
                <ul className="divide-y">
                  {sendable.map((c) => (
                    <li key={c.customerId} className="flex gap-3 py-3">
                      <input
                        type="checkbox"
                        id={`sms-${c.customerId}`}
                        className="mt-1 size-4 shrink-0"
                        checked={chosen.has(c.customerId)}
                        onChange={(e) => toggle(c.customerId, e.target.checked)}
                      />
                      <div className="min-w-0 flex-1 space-y-1.5">
                        <div className="flex flex-wrap items-baseline gap-x-3 gap-y-0.5">
                          <label htmlFor={`sms-${c.customerId}`} className="font-medium">{c.name}</label>
                          <span className="text-sm text-muted-foreground tabular">{c.mobile}</span>
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {c.daysSince} days since the last delivery ({displayDate(c.lastDelivered)}), usually every{' '}
                          {c.orderInterval > 0 ? `${c.orderInterval} days` : `${data.defaultIntervalDays} days (not set)`}
                        </p>
                        <p
                          className={cn(
                            'rounded-md border bg-muted/50 px-3 py-2 text-sm whitespace-pre-wrap',
                            !chosen.has(c.customerId) && 'opacity-50',
                          )}
                        >
                          {fill(template, c)}
                        </p>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>

          {skipped.length > 0 && (
            <Card className="gap-3 py-5">
              <CardHeader className="px-5">
                <CardTitle>Won't be texted ({skipped.length})</CardTitle>
                <CardDescription>Overdue, but not texted for the reason shown.</CardDescription>
              </CardHeader>
              <CardContent className="px-5">
                <ul className="divide-y">
                  {skipped.map((c) => (
                    <li key={c.customerId} className="flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1 py-2.5 text-sm">
                      <Link to={`/customers/view?customerId=${c.customerId}`} className="font-medium text-primary hover:underline">
                        {c.name}
                      </Link>
                      <span className="text-muted-foreground">
                        {c.skipReason}
                        {c.lastTexted && ` (${displayDate(c.lastTexted)})`} · {c.daysSince} days since delivery
                      </span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </>
  )
}

// ---------- Sent texts ----------

function StateBadge({ state }: { state: string }) {
  const failed = state === 'Error' || state === 'Failed'
  return (
    <Badge
      variant="outline"
      className={cn(
        'rounded-full',
        failed && 'border-destructive/40 bg-destructive/10 text-destructive',
        state === 'Skipped' && 'text-muted-foreground',
      )}
    >
      {state}
    </Badge>
  )
}

export function SmsLogPage() {
  // The send's own summary arrives as a toast; every text it handed over is a row here.
  const { data, error } = usePage<PageData & { messages: SmsLogRow[]; configured: boolean }>()
  const columns = useMemo<ColumnDef<SmsLogRow>[]>(
    () => [
      { accessorKey: 'createdAt', header: 'Sent', cell: ({ getValue }) => displayDateTime(getValue<string>()) },
      {
        accessorKey: 'customerName', header: 'Customer',
        cell: ({ row }) => (
          <Link to={`/customers/view?customerId=${row.original.customerId}`} className="font-medium text-primary hover:underline">
            {row.original.customerName ?? `Customer ${row.original.customerId}`}
          </Link>
        ),
      },
      { accessorKey: 'phoneNumber', header: 'Number' },
      {
        accessorKey: 'body', header: 'Message',
        cell: ({ getValue }) => <span className="block max-w-md truncate" title={getValue<string>()}>{getValue<string>()}</span>,
      },
      {
        accessorKey: 'state', header: 'State',
        cell: ({ row }) => (
          <div className="space-y-1">
            <StateBadge state={row.original.state} />
            {row.original.error && <p className="max-w-xs text-xs whitespace-normal text-destructive">{row.original.error}</p>}
          </div>
        ),
      },
      { accessorKey: 'createdBy', header: 'By' },
    ],
    [],
  )
  if (!data) return <PageState error={error} />
  return (
    <>
      <PageHeader
        title="Sent texts"
        description="Every text Aurora asked the shop phone to send, newest first."
        actions={
          <Button asChild>
            <Link to="/sms/compose"><MessageSquareText /> Text reminders</Link>
          </Button>
        }
      />
      <DataTable columns={columns} data={data.messages} emptyText="No texts have been sent yet." />
    </>
  )
}
