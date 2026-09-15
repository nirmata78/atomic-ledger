# Task board

Ten tasks, ordered. Each closes a specific, named gap — this isn't a generic "build a CRUD app"
list, it's sized around three things that came up short in a real interview and one bonus (CQRS).

## How to use this board

1. Work top to bottom — later tasks assume earlier ones are done (T-004 needs T-003's test; T-008
   needs T-007's implementation).
2. Read the task file fully before starting — each one names the exact class/test to edit, what
   "done" looks like, and which doc page has the theory behind it.
3. Remove the `@Disabled`/`@Ignore` annotation (or the `UnsupportedOperationException`) as your
   last step, once the real implementation is in and the test is green.
4. Tick the box below in the same commit that finishes the task.

## Status

| Task | Closes | Module | Status |
|---|---|---|---|
| [T-001](T-001-monorepo-skeleton.md) | — (setup/verification) | root | ✅ Done |
| [T-002](T-002-domain-warmup.md) | domain-model fluency | `ledger-core` | ⬜ Not started |
| [T-003](T-003-reproduce-the-bug.md) | proving a concurrency bug exists | `ledger-core` | ⬜ Not started |
| [T-004](T-004-lock-based-fix.md) | `ReentrantLock`, deadlock avoidance, `synchronized` | `ledger-core` | ⬜ Not started |
| [T-005](T-005-isolation-harness.md) | the four isolation levels + Postgres's real behaviour | `isolation-lab` | ⬜ Not started |
| [T-006](T-006-serializable-ssi.md) | Postgres `SERIALIZABLE` / SSI retry handling | `isolation-lab` | ⬜ Not started |
| [T-007](T-007-settlement-pipeline.md) | `BlockingQueue`, `ConcurrentHashMap`, `ExecutorService` | `settlement-worker` | ⬜ Not started |
| [T-008](T-008-settlement-spock-spec.md) | proving exactly-once processing; Spock | `settlement-worker` | ⬜ Not started |
| [T-009](T-009-cqrs-read-model.md) *(stretch)* | CQRS | `balance-read-model` | ⬜ Not started |
| [T-010](T-010-polish-and-publish.md) | — (readiness for interview / public repo) | root | ⬜ Not started |

## What each task is really testing

| If asked about... | Point to |
|---|---|
| "Which concurrency features are in core Java?" | T-002 (naming), T-004 (using), [docs/02](../docs/02-concurrency-theory.md) |
| "Which isolation level do you use, and why?" | T-005, T-006, [docs/03](../docs/03-isolation-levels-theory.md) |
| "How do you solve a race condition at the database level?" | T-004 (README comparison), [docs/03](../docs/03-isolation-levels-theory.md) |
| "Tell me about a producer/consumer you've built" | T-007, T-008 |
| "What's CQRS?" | T-009 |
