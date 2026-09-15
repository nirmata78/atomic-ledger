# T-004 — Fix it: `synchronized` vs. `ReentrantLock`

**Module:** `ledger-core` · **Closes:** `ReentrantLock` used correctly by hand, deadlock avoidance
via lock ordering, and the trade-off between a coarse shared lock and a fine-grained one · **Files:**
`SynchronizedLedgerTransferService.java`, `LockingLedgerTransferService.java`

This is the flagship exercise — it's the one that touches the most ground from the original gap
list in one place.

## T-004a — `SynchronizedLedgerTransferService`

Implement `transfer` using a single shared lock object (a `private static final Object LOCK = new
Object();` or similar) around the read-compute-write sequence — every transfer, regardless of which
accounts it touches, serializes against every other transfer.

Re-point T-003's concurrency test at this class. It should now **pass**.

## T-004b — `LockingLedgerTransferService`

Implement `transfer` using each `Account`'s own `ReentrantLock` (`Account#lock()`) instead of one
shared monitor. The part that actually matters:

- Acquire the two accounts' locks in a **consistent global order** — by `Account#id()`, always the
  lower id first — never in call order. Two concurrent transfers moving money in opposite
  directions between the same pair of accounts will deadlock otherwise: each thread holds one lock
  and blocks forever waiting for the other.
- Release both locks in a `finally`, innermost-first.
- A worked version of exactly this pattern is in
  [docs/02-concurrency-theory.md](../docs/02-concurrency-theory.md#reentrantlock) if you get stuck —
  use it to check your answer, not as a starting point to copy before attempting it yourself.

Re-point T-003's concurrency test at this class too. It should also **pass** — and, if you want to
prove the fine-grained locking actually bought something, write a quick throughput comparison (see
below).

## The comparison that makes this worth doing

Write a short section in this file (or a new `NOTES.md` in `ledger-core/`) comparing the three
implementations:

| Implementation | Correct under load? | What it serializes |
|---|---|---|
| `NaiveLedgerTransferService` | No (T-003 proves it) | Nothing — that's the bug |
| `SynchronizedLedgerTransferService` | Yes | **Every** transfer against every other transfer, even ones touching completely disjoint accounts |
| `LockingLedgerTransferService` | Yes | Only transfers that actually share an account |

If you have time, actually measure it: run T-003's test scenario against both fixed
implementations with a much larger thread count and account pool, and time them. The
`synchronized` version's throughput should degrade as thread count grows past a few; the
`ReentrantLock` version, with enough distinct accounts in the pool, shouldn't degrade nearly as
much. This is the sentence you want ready: *"synchronized was correct but serialized unrelated
work; per-account locks fixed the same bug without the same throughput cost."*

## Definition of done

Both classes implemented, T-003's test passes against both, and you can explain the deadlock risk
in T-004b and why the fix (ordered acquisition) works.
