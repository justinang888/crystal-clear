import { CalendarDays, Circle, Coins, Hourglass, Plus, RectangleVertical, Truck, Bell, BarChart3, History } from 'lucide-react'
import { Link } from 'react-router'

import { PageHeader, PageState, Stat } from '@/components/page'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { usePage } from '@/hooks/use-page'
import { displayDate } from '@/lib/format'
import type { DashboardData } from '@/lib/types'

const QUICK_LINKS = [
  { to: '/orders/list', label: "Today's orders", icon: Truck },
  { to: '/orders/pending', label: 'Orders waiting for delivery', icon: Hourglass },
  { to: '/customers/dailyduedates', label: 'Customers due today', icon: Bell },
  { to: '/debts/listAll', label: 'Who owes money (A/R)', icon: Coins },
]

const REPORTS = [
  { to: '/orders/daily?d=today', label: 'Daily sales', icon: CalendarDays },
  { to: '/orders/monthly?d=today', label: 'Monthly summary', icon: BarChart3 },
  { to: '/container/history?d=today', label: 'Container history', icon: History },
]

export function DashboardPage() {
  const { data, error } = usePage<DashboardData>('/dashboard')
  if (!data) return <PageState error={error} />

  return (
    <>
      <PageHeader
        title="Today"
        description={displayDate(new Date())}
        actions={
          <>
            <Button asChild>
              <Link to="/customers/list">
                <Plus /> New order
              </Link>
            </Button>
            <Button variant="outline" asChild>
              <Link to="/orders/daily?d=today">
                <CalendarDays /> Daily sales
              </Link>
            </Button>
          </>
        }
      />

      <div className="grid gap-4 sm:grid-cols-3">
        <Link to="/orders/pending" className="rounded-xl focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none">
          <Stat label="Pending orders" value={data.pendingCount} icon={<Hourglass />} tone="warning" className="h-full transition-colors hover:border-primary/40" />
        </Link>
        <Link to="/container/listAll" className="rounded-xl focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none">
          <Stat label="Round borrowed today" value={data.runningRound} icon={<Circle />} tone="round" className="h-full transition-colors hover:border-primary/40" />
        </Link>
        <Link to="/container/listAll" className="rounded-xl focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none">
          <Stat label="Slim borrowed today" value={data.runningSlim} icon={<RectangleVertical />} tone="slim" className="h-full transition-colors hover:border-primary/40" />
        </Link>
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-2">
        <LinkCard title="Quick links" links={QUICK_LINKS} />
        <LinkCard title="Reports" links={REPORTS} />
      </div>
    </>
  )
}

function LinkCard({ title, links }: { title: string; links: { to: string; label: string; icon: typeof Truck }[] }) {
  return (
    <Card className="gap-2 py-4">
      <CardHeader className="px-4">
        <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
      </CardHeader>
      <CardContent className="px-2">
        <ul>
          {links.map((link) => (
            <li key={link.to}>
              <Link to={link.to} className="flex items-center gap-3 rounded-md px-2 py-2 text-sm hover:bg-muted">
                <link.icon className="size-4 text-muted-foreground" />
                {link.label}
              </Link>
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  )
}
