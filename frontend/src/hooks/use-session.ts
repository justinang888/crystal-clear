import { useQuery } from '@tanstack/react-query'

import { currentSession } from '@/lib/api'

/** The logged-in user and what the menus should offer them. One request, shared by every caller. */
export function useSession() {
  return useQuery({ queryKey: ['session'], queryFn: currentSession, retry: false, staleTime: 5 * 60_000 })
}
