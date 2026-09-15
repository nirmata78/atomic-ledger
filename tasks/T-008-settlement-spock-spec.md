# T-008 — Settlement worker Spock spec

**Module:** `settlement-worker` · **Closes:** proving T-007 under concurrent load, and first-hand
Spock exposure (a named JD requirement this codebase otherwise has zero history with) · **File:**
`SettlementWorkerSpec.groovy`

## Why Spock, specifically

Spock's `given:`/`when:`/`then:` blocks map directly onto the STARR/AAA structure a JUnit test
already follows — so if you've written a clear JUnit test before, this should read as familiar
material in new syntax rather than a new way of thinking about testing. That reframing is worth
having ready if asked "how much Spock experience do you have."

## What to build

In the one `@Ignore`d feature method:

1. **`given:`** Build a `BlockingQueue<PendingEntry>` containing some ids more than once — e.g.
   id=1 three times, id=2 once — to force the duplicate-handling path in T-007's implementation to
   actually matter. Use a thread-safe counter as the `SettlementProcessor` passed to
   `SettlementWorker` — a `ConcurrentHashMap<Long, AtomicInteger>`, incrementing per id each time
   `process` is called, is a clean choice.
2. **`when:`** `worker.start()`, enqueue the entries, then `worker.shutdown()` (or however T-007
   signals "drain and stop").
3. **`then:`** Assert the counter for id=1 is exactly `1` (not 3), and for id=2 is exactly `1`.

Run it with several consumer threads (3–5) to actually exercise concurrent access to the
in-flight-tracking map — a single-consumer worker would pass this test even with a broken,
non-atomic duplicate check, because there'd be no real race to expose the bug.

## Definition of done

`@Ignore` removed, the spec green, and it's genuinely testing concurrent duplicate-handling (not
just "the happy path with one entry"). If you want to go one step further: add a second feature
method that fires the queue from a producer thread concurrently with the consumers starting, to
validate `BlockingQueue#take()`'s blocking behavior (consumers waiting on an empty queue, then
picking up work as it arrives) rather than only testing a pre-filled queue.
