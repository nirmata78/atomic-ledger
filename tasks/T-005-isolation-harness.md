# T-005 — Isolation-level harness

**Module:** `isolation-lab` · **Closes:** naming and demonstrating the four transaction isolation
levels, and the two ways Postgres's real behaviour is stricter than the SQL standard · **File:**
`src/test/java/dev/atomicledger/isolation/IsolationLevelHarnessTest.java`

## What's already there

`PostgresTestSupport` — a real Postgres via Testcontainers, with a fresh `orders` table
(`id`, `status`, `amount`) reset before every test. Two `@Disabled` test methods with detailed
TODOs.

## T-005a — dirty reads are prevented at `READ_COMMITTED`

1. Open two JDBC connections from `PostgresTestSupport.dataSource()`. Call them `connA` (stands in
   for "transaction 1") and `connB` ("transaction 2").
2. `setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED)` and `setAutoCommit(false)` on
   both.
3. `connA`: `UPDATE orders SET amount = 999.00 WHERE id = 1` — do **not** commit yet.
4. `connB`: `SELECT amount FROM orders WHERE id = 1` — assert it's still `100.00`. This is the
   proof: Postgres never lets you see another transaction's uncommitted write, even at its lowest
   isolation level (see the `READ UNCOMMITTED` note in
   [docs/03-isolation-levels-theory.md](../docs/03-isolation-levels-theory.md)).
5. `connA.rollback()`.

## T-005b — non-repeatable reads: possible at `READ_COMMITTED`, prevented at `REPEATABLE_READ`

1. `connB` at `READ_COMMITTED`, `autoCommit=false`: `SELECT amount ... WHERE id = 1` → `100.00`.
2. `connA`: `UPDATE orders SET amount = 150.00 WHERE id = 1`; commit.
3. `connB`: re-run the **same** select, inside the **same still-open transaction** → now `150.00`.
   That's the non-repeatable read — same query, same transaction, different answer.
4. Repeat steps 1–3 with `connB` at `TRANSACTION_REPEATABLE_READ` instead, and show the second read
   stays `100.00` — the snapshot taken at the start of the transaction holds for its duration.

## Definition of done

Both tests un-`@Disabled`d and green, and you've written (in a comment or in `NOTES.md`) one
sentence per phenomenon in your own words — the writing is what makes the recall stick, not just
the passing test.

## Theory

[docs/03-isolation-levels-theory.md](../docs/03-isolation-levels-theory.md).
