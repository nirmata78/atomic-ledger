# T-007 — Settlement pipeline

**Module:** `settlement-worker` · **Closes:** `BlockingQueue`, `ConcurrentHashMap`,
`ExecutorService`, graceful shutdown — the exact shape of "consume row IDs from a collection with
multiple threads" · **File:** `SettlementWorker.java`

## What's already there

`PendingEntry` (a unit of work) and `SettlementProcessor` (a callback interface) — both complete.
`SettlementWorker`'s constructor and fields; `start()`/`shutdown()` are stubs.

## What to build

1. **Consumers, not raw threads.** `start()` should submit `consumerThreads` worker tasks to an
   `ExecutorService` (store it as a field so `shutdown()` can reach it). Each worker task loops:
   take an entry from `queue` (a blocking take — `BlockingQueue#take()`, not a busy-poll), decide
   whether it's already been processed, and if not, process it.
2. **Exactly-once, even with duplicate entries.** Track in-flight/processed ids in a
   `ConcurrentHashMap<Long, Boolean>` (or `Collections.newSetFromMap(new ConcurrentHashMap<>())`).
   Use the map's atomic `putIfAbsent` (or the set's `add()`, which is atomic and returns `false` if
   already present) to decide "mine to process" — **not** a separate `containsKey` check followed
   by a `put`, which would itself be a race between two consumer threads picking up the same
   duplicate entry at the same instant.
3. **Graceful shutdown.** `shutdown()` needs to: stop the pool from accepting new work, let
   currently in-flight entries finish, and only return once every consumer thread has actually
   stopped. A `CountDownLatch` initialized to `consumerThreads`, counted down once per worker task
   as it exits its loop, with `shutdown()` awaiting it, is the clean way to make that last guarantee
   concrete rather than assumed. Decide how a consumer knows to stop looping — a poison-pill entry
   pushed once per consumer, or a `volatile boolean running` flag checked each iteration with a
   short `poll(timeout, unit)` instead of an unbounded `take()`, are both legitimate; pick one and
   be ready to explain the trade-off (poison pills are simple but need exactly `consumerThreads` of
   them; a flag+poll needs a timeout choice that trades shutdown latency against busy-waiting).

## Definition of done

`SettlementWorker` fully implemented; move on to T-008 to actually prove exactly-once holds under
load — this task alone compiling and looking reasonable isn't proof of correctness.

## Theory

[docs/02-concurrency-theory.md](../docs/02-concurrency-theory.md).
