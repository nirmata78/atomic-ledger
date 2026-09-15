package dev.atomicledger.settlement;

import java.util.concurrent.BlockingQueue;

/**
 * T-007 / T-008 live here. Full instructions: tasks/T-007.md and tasks/T-008.md.
 *
 * <p>Rebuilds, cleanly and by hand, the pipeline described on the Revolut screen: several
 * consumer threads pull {@link PendingEntry} items off a shared {@link BlockingQueue} and hand
 * each one to a {@link SettlementProcessor}, with a guarantee that no entry is ever processed
 * twice even if it somehow ends up on the queue more than once.
 *
 * <p>What "done" needs, concretely (see tasks/T-007.md for the full spec):
 * <ul>
 *   <li>A configurable number of consumer threads, started via {@link java.util.concurrent.ExecutorService}
 *       — not raw {@code new Thread()}.</li>
 *   <li>A {@code java.util.concurrent.ConcurrentHashMap<Long, Boolean>} (or
 *       {@code Collections.newSetFromMap(new ConcurrentHashMap<>())}) tracking in-flight/processed
 *       ids, so a duplicate entry is skipped rather than processed twice. Use
 *       {@code putIfAbsent}/{@code Set#add}'s atomic return value to decide "mine to process" —
 *       not a separate {@code containsKey} check, which would itself be a race.</li>
 *   <li>Graceful shutdown: a {@code shutdown()} method that stops accepting new work, lets
 *       in-flight entries finish, and returns only once every consumer thread has actually
 *       stopped — {@link java.util.concurrent.CountDownLatch} (one count per consumer thread) is
 *       the clean way to signal that back to the caller.</li>
 * </ul>
 */
public final class SettlementWorker {

    private final BlockingQueue<PendingEntry> queue;
    private final int consumerThreads;
    private final SettlementProcessor processor;

    public SettlementWorker(BlockingQueue<PendingEntry> queue, int consumerThreads, SettlementProcessor processor) {
        this.queue = queue;
        this.consumerThreads = consumerThreads;
        this.processor = processor;
    }

    /** Starts {@code consumerThreads} consumers pulling from the queue. TODO T-007. */
    public void start() {
        throw new UnsupportedOperationException("T-007: start consumerThreads via an ExecutorService");
    }

    /** Stops accepting work and blocks until every in-flight entry has finished. TODO T-007. */
    public void shutdown() {
        throw new UnsupportedOperationException("T-007: signal shutdown and await every consumer thread");
    }
}
