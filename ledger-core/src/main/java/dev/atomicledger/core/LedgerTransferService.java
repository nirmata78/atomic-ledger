package dev.atomicledger.core;

/**
 * Moves {@code amount} from {@code from} to {@code to}. Three implementations of this
 * interface exist on purpose — see the task board (tasks/T-003, T-004) for what each one is
 * supposed to prove:
 *
 * <ul>
 *   <li>{@link NaiveLedgerTransferService} — intentionally broken under concurrency. Given
 *       complete; T-003 is writing the test that proves it.</li>
 *   <li>{@link SynchronizedLedgerTransferService} — {@code synchronized}-based fix. T-004a.</li>
 *   <li>{@link LockingLedgerTransferService} — {@link java.util.concurrent.locks.ReentrantLock}
 *       based fix with ordered lock acquisition. T-004b.</li>
 * </ul>
 *
 * <p>A fourth variant — database-level, using {@code SELECT ... FOR UPDATE} — is deliberately
 * <b>not</b> stubbed here, because it needs its own schema and repository rather than an
 * in-memory {@link Account}. It's T-004c; see that task file for the shape it should take.
 */
public interface LedgerTransferService {

    void transfer(Account from, Account to, Money amount);

    /** Thrown when a transfer would take an account balance negative. */
    final class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(Account from, Money amount) {
            super("Account %d has insufficient funds for a transfer of %s".formatted(from.id(), amount));
        }
    }
}
