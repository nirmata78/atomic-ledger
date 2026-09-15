# 03 — Transaction isolation levels

## The SQL-standard table

| Level | Dirty read | Non-repeatable read | Phantom read |
|---|---|---|---|
| READ UNCOMMITTED | possible | possible | possible |
| READ COMMITTED | prevented | possible | possible |
| REPEATABLE READ | prevented | prevented | possible (standard) |
| SERIALIZABLE | prevented | prevented | prevented |

## The phenomena, one line each

- **Dirty read** — you read a row another transaction wrote but hasn't committed yet; if it rolls
  back, you read data that never really existed.
- **Non-repeatable read** — you read the same row twice in one transaction and get different
  values, because another transaction committed a change in between.
- **Phantom read** — you re-run the same *filtered query* twice in one transaction and get a
  different *set of rows*, because another transaction inserted/deleted matching rows in between.

## Where PostgreSQL diverges from the standard — the detail worth knowing cold

PostgreSQL doesn't implement all four levels literally:

- **`READ UNCOMMITTED` is silently treated as `READ COMMITTED`.** Postgres never does true dirty
  reads, at any level.
- **`REPEATABLE READ` is stricter than the standard requires.** Because Postgres uses MVCC
  snapshot isolation, its `REPEATABLE READ` also prevents phantom reads in practice — the standard
  doesn't mandate that, but Postgres's implementation happens to give it to you anyway.
- **`SERIALIZABLE` is implemented as SSI (Serializable Snapshot Isolation).** Rather than blocking
  to guarantee serializability, Postgres detects dangerous concurrent-transaction patterns (like
  write skew) and aborts one of the transactions with a `serialization_failure` error (SQLState
  `40001`), leaving the caller to retry. Code using `SERIALIZABLE` in Postgres has to be written to
  expect and retry that error — it is not optional error handling, it's the mechanism.
- **Default isolation level: `READ COMMITTED`.** Not `REPEATABLE READ`, not `SERIALIZABLE`.

[`isolation-lab`](../isolation-lab) exists to prove all four of the bullets above against a real
database rather than taking them on faith — see [T-005](../tasks/T-005-isolation-harness.md) and
[T-006](../tasks/T-006-serializable-ssi.md).

## `SELECT ... FOR UPDATE` — orthogonal to isolation level

Row-level pessimistic locking is usable at *any* isolation level, and is often the more direct fix
for a specific race condition than raising the isolation level globally — raising the isolation
level affects the whole transaction's read consistency, not just the one row that actually needs
protecting. The honest answer to "why not just use `SERIALIZABLE` everywhere" is throughput and
false-positive aborts: `SERIALIZABLE`'s SSI mechanism will abort transactions that didn't actually
conflict on the data, just on the pattern of reads and writes, under enough concurrent load.
