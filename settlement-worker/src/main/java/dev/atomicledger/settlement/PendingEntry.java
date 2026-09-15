package dev.atomicledger.settlement;

/**
 * A unit of work pulled off the queue in {@link SettlementWorker} — stands in for the "row ID
 * pulled from a collection" shape described on the Revolut screen. Given complete.
 */
public record PendingEntry(long id, String payload) {
}
