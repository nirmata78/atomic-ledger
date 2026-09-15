package dev.atomicledger.core;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ledger account. Given as complete plumbing (T-002), with ONE deliberate gap: it exposes
 * a private {@link ReentrantLock} via {@link #lock()} but nothing in this class uses it yet.
 * Wiring it up correctly — in the right order, released in a {@code finally} — is T-004.
 *
 * <p>The {@code id} ordering matters: {@link LedgerTransferService} must always acquire the
 * lower-id account's lock first to avoid the classic two-account deadlock. See T-004.
 */
public final class Account {

    private final long id;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile Money balance;

    public Account(long id, Money openingBalance) {
        this.id = id;
        this.balance = Objects.requireNonNull(openingBalance, "openingBalance");
    }

    public long id() {
        return id;
    }

    /** Not thread-safe on its own — see T-003 for why, and T-004 for the fix. */
    public Money balance() {
        return balance;
    }

    void setBalance(Money balance) {
        this.balance = balance;
    }

    public ReentrantLock lock() {
        return lock;
    }

    @Override
    public String toString() {
        return "Account{id=%d, balance=%s}".formatted(id, balance);
    }
}
