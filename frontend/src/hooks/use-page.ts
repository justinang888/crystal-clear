import { useEffect } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useLocation, useNavigate } from 'react-router'
import { toast } from 'sonner'

import { getPage, postForm, type FormValues, type PageData } from '@/lib/api'

/** Data for the screen at `path` (default: the current address). */
export function usePage<T extends PageData>(path?: string) {
  const location = useLocation()
  const key = path ?? location.pathname + location.search
  const query = useQuery({ queryKey: ['page', key], queryFn: () => getPage<T>(key) })

  // Messages a controller put straight into the model (not through a redirect).
  const msg = query.data?.msg
  const failmsg = query.data?.failmsg
  useEffect(() => {
    if (msg) toast.success(msg)
  }, [msg])
  useEffect(() => {
    if (failmsg) toast.error(failmsg)
  }, [failmsg])

  return query
}

/**
 * Posts a form. When the controller redirects, shows its message and opens that screen with the
 * data that came back; when it answers with the same form (validation failed), returns the errors.
 */
export function useSave<T extends PageData = PageData>(path: string, options: { formView?: string } = {}) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (values: FormValues) => postForm<T>(path, values),
    onSuccess: ({ path: landed, data }) => {
      // A save that worked redirects; one that didn't answers from the save address itself,
      // with the form again (validation errors) or the error page.
      // (A redirect can land back on the form's own screen, as setting a customer's price does.)
      const redirected = landed.split('?')[0] !== path
      const messages = Object.values(data.errors ?? {})
      if (!redirected || (data.view === options.formView && messages.length > 0)) {
        toast.error(data.view === options.formView ? 'Please check the highlighted fields.' : messages[0] ?? 'The save did not go through.')
        return
      }
      // Every screen's data may have changed; refetch what is shown next.
      queryClient.invalidateQueries({ queryKey: ['page'] })
      queryClient.setQueryData(['page', landed], { ...data, msg: undefined, failmsg: undefined })
      if (data.msg) toast.success(data.msg)
      if (data.failmsg) toast.error(data.failmsg)
      navigate(landed)
    },
    onError: (error) => toast.error(error.message),
  })
}

/** Errors from the last failed save, by field name. */
export function fieldErrors(result: { data?: { data: PageData } } | undefined): Record<string, string> {
  return result?.data?.data.errors ?? {}
}
