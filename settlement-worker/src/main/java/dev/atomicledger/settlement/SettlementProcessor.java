package dev.atomicledger.settlement;

/** What a consumer thread does with one {@link PendingEntry}. Given complete — a plain callback. */
@FunctionalInterface
public interface SettlementProcessor {
    void process(PendingEntry entry);
}
