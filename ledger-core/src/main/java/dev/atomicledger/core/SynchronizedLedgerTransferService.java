package dev.atomicledger.core;

/**
 * T-004a. Fix {@link NaiveLedgerTransferService}'s bug using {@code synchronized} — a static
 * lock object shared by every transfer, since {@code Account} doesn't expose a monitor of its
 * own here. Then re-run T-003's concurrency test against this class instead and confirm the
 * balance drift disappears. Once it's green, note in tasks/T-004.md what you gave up to get
 * there (hint: every transfer now serializes against every other transfer, even ones touching
 * completely unrelated accounts).
 */
public final class SynchronizedLedgerTransferService implements LedgerTransferService {

    @Override
    public void transfer(Account from, Account to, Money amount) {
        throw new UnsupportedOperationException("T-004a: implement using a shared synchronized block");
    }
}
