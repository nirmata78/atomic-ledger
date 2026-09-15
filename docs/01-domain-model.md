# 01 — Domain model

Each module has its own small, self-contained model. They are deliberately not unified into a
single shared domain module — see [04 Architecture](04-architecture.md) for why.

## `ledger-core`

- **`Money`** — an immutable, `BigDecimal`-backed value type, always scaled to 2 decimal places.
  Complete, given as plumbing.
- **`Account`** — has an `id`, a `balance`, and its own `ReentrantLock`. The lock exists on the
  class but nothing uses it yet — wiring it up correctly is
  [T-004](../tasks/T-004-lock-based-fix.md).
- **`LedgerTransferService`** — the interface all three transfer strategies implement:
  `NaiveLedgerTransferService` (complete, deliberately broken under concurrency),
  `SynchronizedLedgerTransferService` and `LockingLedgerTransferService` (both stubs).

## `isolation-lab`

A single table, created fresh before every test by `PostgresTestSupport`:

```sql
CREATE TABLE orders (
    id      INTEGER PRIMARY KEY,
    status  TEXT NOT NULL,
    amount  NUMERIC(10,2) NOT NULL
);
INSERT INTO orders (id, status, amount) VALUES (1, 'PENDING', 100.00);
```

There's no Java domain model here on purpose — the module's entire job is two raw JDBC
`Connection`s talking to this table with different isolation levels and interleaved statements.
Wrapping that in a domain object would hide exactly the mechanics the exercise is about.

## `settlement-worker`

- **`PendingEntry`** — `record(long id, String payload)`. A unit of work, standing in for a row ID
  pulled off a real table.
- **`SettlementProcessor`** — `@FunctionalInterface` — what a consumer thread does with one entry.
- **`SettlementWorker`** — the pipeline itself: N consumer threads pulling from a shared
  `BlockingQueue<PendingEntry>`, with exactly-once processing guaranteed even if an entry appears
  on the queue more than once. Stub — see
  [T-007](../tasks/T-007-settlement-pipeline.md).

## `balance-read-model` (optional)

No domain model yet — T-009 is defining it. By design it does **not** reuse `ledger-core`'s
`Money`/`Account` types (see [04 Architecture](04-architecture.md)); a real read-model DTO,
independent of the write side's domain types, is part of the exercise.
