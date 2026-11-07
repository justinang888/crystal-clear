/**
 * Talks to the Spring controllers. Each screen's data lives at the same address as the screen:
 * /aurora/orders/list?d=2026-11-07 shows the page in the browser and, asked for JSON, returns the
 * controller's model. Saves are ordinary form posts; the controller's redirect is followed and
 * answers with the next screen's data (including its flash message, such as `msg`).
 */

export const BASE = '/aurora'

export class AuthError extends Error {
  constructor() {
    super('Not logged in')
  }
}

export class ServerError extends Error {
  status: number
  constructor(status: number) {
    super(status >= 500 ? 'The server ran into an error.' : `The request failed (${status}).`)
    this.status = status
  }
}

/** Every model comes back with the view the controller chose and any form errors. */
export interface PageData {
  view: string
  errors: Record<string, string>
  msg?: string
  failmsg?: string
  [key: string]: unknown
}

function csrfToken(): string {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/)
  return match ? decodeURIComponent(match[1]) : ''
}

async function check(response: Response): Promise<Response> {
  if (response.status === 401) throw new AuthError()
  if (!response.ok) throw new ServerError(response.status)
  return response
}

/** The JSON for an app path such as "/orders/list?d=today". */
export async function getPage<T = PageData>(path: string): Promise<T> {
  const response = await fetch(BASE + path, {
    headers: { Accept: 'application/json' },
    credentials: 'same-origin',
  })
  await check(response)
  return response.json() as Promise<T>
}

export type FormValues = Record<string, string | number | null | undefined>

export interface PostResult<T = PageData> {
  /** The app path the controller sent us to, e.g. "/orders/list". */
  path: string
  data: T
}

/** Posts a form the way the old JSP forms did and returns where the controller sent us. */
export async function postForm<T = PageData>(path: string, values: FormValues): Promise<PostResult<T>> {
  const body = new URLSearchParams()
  for (const [key, value] of Object.entries(values)) {
    if (value !== undefined && value !== null) body.append(key, String(value))
  }
  const response = await fetch(BASE + path, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': csrfToken(),
    },
    credentials: 'same-origin',
    body,
  })
  await check(response)
  const url = new URL(response.url)
  const landed = url.pathname.startsWith(BASE) ? url.pathname.slice(BASE.length) || '/' : url.pathname
  return { path: landed + url.search, data: (await response.json()) as T }
}

/** Makes sure the XSRF-TOKEN cookie exists before the first post (login). */
async function ensureCsrfCookie() {
  if (!csrfToken()) {
    await fetch(BASE + '/session', { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
  }
}

export async function login(username: string, password: string): Promise<boolean> {
  await ensureCsrfCookie()
  const response = await fetch(BASE + '/login', {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': csrfToken(),
    },
    credentials: 'same-origin',
    body: new URLSearchParams({ username, password }),
  })
  if (response.status === 401) return false
  await check(response)
  return true
}

export async function logout(): Promise<void> {
  await fetch(BASE + '/logout', {
    method: 'POST',
    headers: { 'X-XSRF-TOKEN': csrfToken() },
    credentials: 'same-origin',
  })
}

export async function currentUser(): Promise<string> {
  const data = await getPage<{ username: string }>('/session')
  return data.username
}
