# T-003 — Reproduce the bug

**Module:** `ledger-core` · **Closes:** proving a lost-update race condition exists, not just
naming one · **File:** `src/test/java/dev/atomicledger/core/LedgerTransferConcurrencyTest.java`

## The gap this closes

Being able to *describe* a race condition and being able to *reproduce one on demand* are
different skills, and only the second one is fully convincing in an interview. This task is purely
about the second.

## What's already there

`NaiveLedgerTransferService` — complete, unmodified, deliberately broken: it reads both account
balances, computes new values, then writes them back, with no synchronization anywhere between the
reads and the writes.

## What to build

In `LedgerTransferConcurrencyTest` (currently one `@Disabled` placeholder method):

1. Create two `Account`s with known starting balances (e.g. both `Money.of("1000.00")`).
2. Record the total: `fromBalance.plus(toBalance)`.
3. Using an `ExecutorService` with several threads, fire a large number (hundreds, not a handful —
   the bug needs enough concurrent pressure to show up reliably) of small transfers in random
   directions between the two accounts, via `NaiveLedgerTransferService`.
4. Use a `CountDownLatch` (one count per submitted transfer) to know when every transfer has
   actually finished before asserting anything.
5. Assert the total is unchanged.

Remove `@Disabled` and run it. **It should fail** — that's the point. A test that passes against
`NaiveLedgerTransferService` on the first try means the concurrent load wasn't heavy enough to
trigger the interleaving; increase the thread count and iteration count until it fails reliably
(not on every single run necessarily — race conditions are timing-dependent — but reliably enough
across a few runs that you trust it's real).

## Definition of done

- The test demonstrably fails against `NaiveLedgerTransferService` (keep the failing run — a
  screenshot or pasted output in your notes is good interview material).
- Leave the test itself in place and passing-when-pointed-at-the-fix — you'll re-point it in T-004.

## Theory

[docs/02-concurrency-theory.md](../docs/02-concurrency-theory.md) — "Deadlock and race conditions."
