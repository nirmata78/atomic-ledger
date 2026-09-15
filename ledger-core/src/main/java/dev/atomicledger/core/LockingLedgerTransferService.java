package dev.atomicledger.core;

/**
 * T-004b. Fix {@link NaiveLedgerTransferService}'s bug using each {@link Account}'s own
 * {@link java.util.concurrent.locks.ReentrantLock} (see {@link Account#lock()}) instead of one
 * shared monitor — this is the version that should let two transfers on disjoint account pairs
 * proceed concurrently, unlike T-004a.
 *
 * <p>The part that actually matters: lock the two accounts in a <b>consistent global order</b>
 * (by {@link Account#id()}), not in call order. Two concurrent transfers moving money in
 * opposite directions between the same pair of accounts will deadlock otherwise — each thread
 * holds one lock and blocks forever waiting for the other. Both locks must be released in a
 * {@code finally}, innermost-first.
 */
public final class LockingLedgerTransferService implements LedgerTransferService {

    @Override
    public void transfer(Account from, Account to, Money amount) {
        throw new UnsupportedOperationException(
                "T-004b: implement using Account#lock(), acquired in a consistent order by id()");
    }
}
