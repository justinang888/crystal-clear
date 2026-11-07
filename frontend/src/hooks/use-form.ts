import { useEffect, useState } from 'react'

/** Form values seeded from the controller's form object once it has loaded. */
export function useFormValues<T extends Record<string, unknown>>(initial: T | undefined) {
  const [values, setValues] = useState<T | undefined>(initial)
  useEffect(() => {
    if (initial && values === undefined) setValues(initial)
  }, [initial, values])

  const set = (name: keyof T) => (e: { target: { value: string } }) =>
    setValues((v) => (v ? { ...v, [name]: e.target.value } : v))
  const setValue = (name: keyof T, value: unknown) => setValues((v) => (v ? { ...v, [name]: value } : v))
  /** Props for an <Input> bound to one field. */
  const bind = (name: keyof T) => ({
    id: String(name),
    name: String(name),
    value: values?.[name] === null || values?.[name] === undefined ? '' : String(values[name]),
    onChange: set(name),
  })
  return { values, setValue, bind }
}
