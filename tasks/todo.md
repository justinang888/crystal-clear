# To do: text reminders to inactive customers

Details, acceptance and checks for each task: [plan.md](plan.md).

- [x] 1. Schema: `customer.sms_opt_out` and `sms_message` (V2 migration)
- [x] 2. Phone numbers to +639...
- [x] 3. Who is overdue (interval + 2 days, never-delivered left out)
- [x] Checkpoint A: tests pass, build clean
- [x] 4. Gateway: interface, fake phone, Android phone client
- [x] 5. Service and controller: compose, send, log, opt-out
- [x] Checkpoint B: all Java tests pass
- [x] 6. React screens and demo data
- [ ] Checkpoint C: demo walk-through, review with the owner
- [ ] 7. Check against the real phone, README and start script
- [ ] Checkpoint D: CI green, ready to merge
