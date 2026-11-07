import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  BarChart3,
  Bell,
  Boxes,
  CalendarCheck,
  CalendarDays,
  ChevronsUpDown,
  CircleHelp,
  Coins,
  Droplets,
  History,
  LayoutDashboard,
  LogOut,
  Moon,
  Package,
  Receipt,
  Sun,
  Truck,
  UserCheck,
  UserMinus,
  Users,
} from 'lucide-react'
import { useTheme } from 'next-themes'
import { Link, Navigate, Outlet, useLocation, useNavigate } from 'react-router'

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
} from '@/components/ui/sidebar'
import { AuthError, currentUser, logout } from '@/lib/api'

interface NavItem {
  label: string
  to: string
  icon: typeof Users
  /** Other screens that belong to this entry, so it stays highlighted on them. */
  match?: string[]
}

const NAV: { label?: string; items: NavItem[] }[] = [
  {
    items: [
      { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
      { label: 'Customers', to: '/customers/list', icon: Users, match: ['/customers/view', '/customers/new', '/customers/edit', '/customers/containerBalances', '/prices', '/payments', '/debts/new', '/debts/list', '/container/return'] },
      { label: 'Orders', to: '/orders/list', icon: Truck, match: ['/orders/pending', '/orders/neworder', '/orders/edit'] },
      { label: 'Products', to: '/products/list', icon: Package, match: ['/products/new', '/products/edit'] },
      { label: 'Expenses', to: '/expenses/list?d=today', icon: Receipt, match: ['/expenses/new'] },
      { label: 'A/R Summary', to: '/debts/listAll', icon: Coins },
    ],
  },
  {
    label: 'Reports',
    items: [
      { label: 'Daily Sales', to: '/orders/daily?d=today', icon: CalendarDays },
      { label: 'Monthly Summary', to: '/orders/monthly?d=today', icon: BarChart3 },
      { label: 'Due Dates', to: '/customers/duedates', icon: CalendarCheck },
      { label: 'Due Today', to: '/customers/dailyduedates', icon: Bell },
      { label: 'Inactive Customers', to: '/customers/listinactive', icon: UserMinus },
      { label: 'Monthly Active', to: '/customers/listactive?d=today', icon: UserCheck },
    ],
  },
  {
    label: 'Containers',
    items: [
      { label: 'Balances', to: '/container/listAll', icon: Boxes },
      { label: 'History', to: '/container/history?d=today', icon: History },
    ],
  },
]

function isActive(item: NavItem, path: string) {
  return [item.to.split('?')[0], ...(item.match ?? [])].some((p) => path === p || path.startsWith(p + '/'))
}

export function AppLayout() {
  const location = useLocation()
  const user = useQuery({ queryKey: ['session'], queryFn: currentUser, retry: false, staleTime: 5 * 60_000 })

  if (user.error instanceof AuthError) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }

  return (
    <SidebarProvider>
      <Sidebar collapsible="icon" className="print:hidden">
        <SidebarHeader>
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton size="lg" asChild>
                <Link to="/dashboard">
                  <span className="flex size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                    <Droplets className="size-4" />
                  </span>
                  <span className="grid leading-tight">
                    <span className="font-semibold">Crystal Clear</span>
                    <span className="text-xs text-sidebar-foreground/70">Aurora back office</span>
                  </span>
                </Link>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarHeader>
        <SidebarContent>
          {NAV.map((group, i) => (
            <SidebarGroup key={group.label ?? i}>
              {group.label && <SidebarGroupLabel>{group.label}</SidebarGroupLabel>}
              <SidebarGroupContent>
                <SidebarMenu>
                  {group.items.map((item) => (
                    <SidebarMenuItem key={item.to}>
                      <SidebarMenuButton
                        asChild
                        isActive={isActive(item, location.pathname)}
                        tooltip={item.label}
                        // The screen you're on is a light pill on the blue, clearly apart from hover.
                        className="data-active:bg-sidebar-primary data-active:text-sidebar-primary-foreground data-active:hover:bg-sidebar-primary data-active:hover:text-sidebar-primary-foreground"
                      >
                        <Link to={item.to}>
                          <item.icon />
                          <span>{item.label}</span>
                        </Link>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
          ))}
        </SidebarContent>
        <SidebarFooter>
          <UserMenu username={user.data} />
        </SidebarFooter>
      </Sidebar>
      <SidebarInset className="min-w-0">
        <header className="sticky top-0 z-20 flex h-12 items-center gap-2 border-b bg-background/90 px-4 backdrop-blur print:hidden">
          <SidebarTrigger className="-ml-1" />
        </header>
        <main className="mx-auto w-full max-w-[1400px] flex-1 p-4 md:p-6">
          <Outlet />
        </main>
      </SidebarInset>
    </SidebarProvider>
  )
}

function UserMenu({ username }: { username?: string }) {
  const { resolvedTheme, setTheme } = useTheme()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const dark = resolvedTheme === 'dark'

  async function signOut() {
    await logout()
    queryClient.clear()
    navigate('/login', { replace: true, state: { loggedOut: true } })
  }

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <SidebarMenuButton size="lg">
              <span className="flex size-8 items-center justify-center rounded-full bg-sidebar-accent text-sm font-semibold text-sidebar-accent-foreground uppercase">
                {username?.slice(0, 1) ?? '·'}
              </span>
              <span className="truncate font-medium">{username ?? '…'}</span>
              <ChevronsUpDown className="ml-auto size-4" />
            </SidebarMenuButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent side="top" align="start" className="w-56">
            <DropdownMenuLabel>Signed in as {username}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem asChild>
              <Link to="/help">
                <CircleHelp /> Help
              </Link>
            </DropdownMenuItem>
            <DropdownMenuItem onSelect={() => setTheme(dark ? 'light' : 'dark')}>
              {dark ? <Sun /> : <Moon />} {dark ? 'Light mode' : 'Dark mode'}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem variant="destructive" onSelect={signOut}>
              <LogOut /> Log out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}
