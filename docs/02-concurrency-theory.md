# 02 — Concurrency theory

The goal here isn't depth for its own sake, it's **fluent naming under pressure** — being able to
reach for the right class in under two seconds when asked "what would you use for X," which is
where recall tends to fail even when the underlying understanding is solid.

## `java.util.concurrent`, by "what problem am I solving"

| If you need to... | Reach for | One line |
|---|---|---|
| A thread-safe map | `ConcurrentHashMap` | Bucket-level locking (Java 8+: mostly CAS, `synchronized` only on collision); no null keys/values |
| A rarely-mutated, often-iterated list/set | `CopyOnWriteArrayList` / `CopyOnWriteArraySet` | Every write copies the backing array; iterators never throw `ConcurrentModificationException`; wrong choice if writes are frequent |
| Hand off work between producer/consumer threads | `BlockingQueue` family | `ArrayBlockingQueue` (bounded, array-backed) · `LinkedBlockingQueue` (optionally bounded) · `SynchronousQueue` (zero capacity, direct handoff) · `PriorityBlockingQueue` — this is exactly the shape [`settlement-worker`](../settlement-worker) builds |
| A managed thread pool | `ExecutorService` / `ThreadPoolExecutor` | Core/max pool size, work queue, rejection policy; prefer this over raw `Thread` for anything production-shaped |
| Compose async work | `CompletableFuture` | `thenApply` / `thenCompose` / `allOf`; non-blocking chaining |
| A one-shot "wait until N things finish" gate | `CountDownLatch` | Count only goes down; not reusable once it hits zero |
| A reusable "wait for each other" rendezvous | `CyclicBarrier` | All N parties must arrive before any proceed; resets automatically |
| Cap concurrent access to N holders (not just 1) | `Semaphore` | Permit-based; `acquire()`/`release()`; the generalization of a lock to N slots |
| A lock-free counter/reference | `AtomicInteger`, `AtomicLong`, `AtomicReference` | CAS-based, no blocking; use before reaching for a lock on a simple counter |
| Explicit, more flexible locking | `ReentrantLock` | See below |
| Many readers / one writer | `ReentrantReadWriteLock` | Readers don't block each other; a writer blocks everyone |
| Optimistic read locking (Java 8+) | `StampedLock` | Adds a non-blocking optimistic-read mode; **not reentrant** — the one gotcha worth stating if asked |

## `ReentrantLock`

An explicit, reentrant mutual-exclusion lock in `java.util.concurrent.locks`, functionally similar
to `synchronized` but acquired/released via method calls instead of a language keyword.

**Why reach for it over `synchronized`:**

- **`tryLock()` / `tryLock(timeout, unit)`** — attempt acquisition without blocking forever; the
  standard building block for deadlock avoidance (back off and retry instead of waiting forever).
- **`lockInterruptibly()`** — a blocked acquisition can respond to `Thread.interrupt()`;
  `synchronized` cannot be interrupted while waiting.
- **Fairness policy** — `new ReentrantLock(true)` grants the lock in roughly FIFO order across
  waiting threads; `synchronized` gives no ordering guarantee at all.
- **Multiple `Condition` objects per lock** — where an implicit monitor gives you one
  `wait`/`notify` queue, a `ReentrantLock` can have several `Condition`s (e.g. "queue not full" and
  "queue not empty" as two separate wait sets on the same lock) — this is exactly how a hand-rolled
  bounded buffer is built without `BlockingQueue`.
- **Lock scope isn't tied to a single block** — you can acquire in one method and release in
  another (same thread, still must balance), which `synchronized` cannot do.

**The gotcha worth volunteering unprompted** (it reads as hands-on scar tissue, not
textbook-recital): `synchronized` releases automatically on exception; `ReentrantLock` does not —
always pair `lock()` with `try { ... } finally { lock.unlock(); }`. Unlocking inside the `try`
block, or forgetting the `finally`, is a real, recurring bug class.

```java
private final ReentrantLock lock = new ReentrantLock();

public void transfer(Account from, Account to, BigDecimal amount) {
    lock.lock();
    try {
        from.debit(amount);
        to.credit(amount);
    } finally {
        lock.unlock();
    }
}
```

**Deadlock-safe version for a two-account operation** — lock in a consistent global order
(e.g. by account ID) rather than in call order, so two concurrent transfers between the same pair
of accounts can never each hold one lock and wait on the other. This is exactly what
[T-004](../tasks/T-004-lock-based-fix.md) asks `LockingLedgerTransferService` to do:

```java
Account first  = a.getId() < b.getId() ? a : b;
Account second = a.getId() < b.getId() ? b : a;
first.lock().lock();
try {
    second.lock().lock();
    try {
        // do the transfer
    } finally {
        second.lock().unlock();
    }
} finally {
    first.lock().unlock();
}
```

## Deadlock and race conditions

**Deadlock** — a cycle of threads each holding a lock the next one needs. Prevention: consistent
lock ordering (always acquire in the same global order), `tryLock` with a timeout instead of
blocking `lock()`, keeping lock scope as small as possible, avoiding nested lock acquisition where
you can.

**Race condition** — unsynchronized concurrent access to shared mutable state where the outcome
depends on thread interleaving/timing. Fixes, roughly cheapest-to-most-expensive: immutability →
atomics → a lock → database-level pessimistic locking (`SELECT ... FOR UPDATE`) → optimistic
locking (a version column, retry on conflict).
