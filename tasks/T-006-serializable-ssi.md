# T-006 — `SERIALIZABLE` and Postgres's SSI

**Module:** `isolation-lab` · **Closes:** the senior-level detail past the textbook table — how
Postgres actually implements `SERIALIZABLE`, and what a caller is supposed to do about it · **File:**
`IsolationLevelHarnessTest.serializableDetectsWriteSkewAndAbortsOneTransaction`

## Background

Most isolation-level explanations stop at "SERIALIZABLE prevents all three phenomena." The more
useful, more senior thing to know is *how* — Postgres doesn't get there by blocking more, it gets
there by detecting dangerous concurrent patterns and aborting one of the transactions involved,
with SQLState `40001` (`serialization_failure`). Reproducing that on purpose, and handling it
correctly, is what this task is for.

## A worked write-skew scenario to build

Write skew: two transactions each read the same data, then each write based on what they read, in
a way that's fine sequentially but isn't concurrently. A classic version, adapted to this table:
imagine a business rule "at most one order can be `PROCESSING` at a time" (pretend `orders` has a
second row, id=2, also seeded `PENDING`):

1. Both `connA` and `connB` at `TRANSACTION_SERIALIZABLE`, `autoCommit=false`.
2. `connA`: `SELECT count(*) FROM orders WHERE status = 'PROCESSING'` → 0. Decides it's safe to
   proceed, then `UPDATE orders SET status = 'PROCESSING' WHERE id = 1`.
3. `connB`, concurrently, before either commits: same `SELECT` → also sees 0 (its snapshot was
   taken before `connA`'s write). Also decides it's safe, `UPDATE orders SET status = 'PROCESSING'
   WHERE id = 2`.
4. `connA.commit()` → succeeds.
5. `connB.commit()` → **assert this throws a `SQLException` with `getSQLState()` equal to
   `"40001"`.** Both transactions individually look fine; together they violate the "at most one"
   rule neither of them could see. That's the pattern SSI is built to catch.

(If reproducing true write skew feels fiddly to set up in the time you have, a simpler
read-then-write-the-same-row conflict between `connA`/`connB` will also trigger a `40001` — that's
a fine substitute proof for this task; write skew is the more impressive one to have working if you
have the time.)

## What a caller does with `40001`

Write one sentence in this file once the test passes: what should application code actually do
when it catches a `serialization_failure`? (Hint: it's in
[docs/03-isolation-levels-theory.md](../docs/03-isolation-levels-theory.md) — retry the whole
transaction from the start, not just the failed statement.) Having this answer ready is what turns
"Postgres uses SSI" from a fact you read into something you can defend under a follow-up question.

## Definition of done

Test un-`@Disabled`d, green, asserting the specific SQLState — not just "an exception was thrown."
