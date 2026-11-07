import { useState, type FormEvent } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Droplets, Loader2 } from 'lucide-react'
import { useLocation, useNavigate } from 'react-router'

import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { login } from '@/lib/api'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()
  const state = location.state as { from?: string; loggedOut?: boolean } | null
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      if (await login(username, password)) {
        queryClient.clear()
        navigate(state?.from && state.from !== '/login' ? state.from : '/dashboard', { replace: true })
      } else {
        setError('Wrong username or password.')
      }
    } catch {
      setError('Could not reach the server. Check that Aurora is running.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex min-h-svh items-center justify-center bg-gradient-to-br from-sky-50 via-background to-blue-100 p-4 dark:from-slate-950 dark:via-background dark:to-blue-950">
      <Card className="w-full max-w-sm shadow-xl">
        <CardContent className="px-8 py-6">
          <div className="mb-6 flex flex-col items-center text-center">
            <span className="mb-3 flex size-12 items-center justify-center rounded-xl bg-primary text-primary-foreground">
              <Droplets className="size-6" />
            </span>
            <h1 className="text-xl font-semibold">Crystal Clear</h1>
            <p className="text-sm text-muted-foreground">Log in to the back office</p>
          </div>
          {state?.loggedOut && !error && (
            <Alert className="mb-4">
              <AlertDescription>You've been logged out.</AlertDescription>
            </Alert>
          )}
          {error && (
            <Alert variant="destructive" className="mb-4">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}
          <form onSubmit={submit} className="grid gap-4">
            <div className="grid gap-1.5">
              <Label htmlFor="username">Username</Label>
              <Input id="username" autoComplete="username" autoFocus required value={username} onChange={(e) => setUsername(e.target.value)} />
            </div>
            <div className="grid gap-1.5">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" autoComplete="current-password" required value={password} onChange={(e) => setPassword(e.target.value)} />
            </div>
            <Button type="submit" size="lg" disabled={busy} className="mt-2">
              {busy && <Loader2 className="animate-spin" />} Log in
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
