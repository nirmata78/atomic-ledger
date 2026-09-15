package dev.atomicledger.core;

/**
 * The "obviously wrong under load" baseline: read both balances, compute, write both
 * balances back — with no synchronization at all. Given complete and unmodified on purpose.
 *
 * <p><b>T-003</b> is proving this is broken: write a test that fires many concurrent transfers
 * between the same two accounts and shows the sum of both balances drifts from what it
 * started as. That drift is a lost update — two threads both read the same "before" balance,
 * and the second write clobbers the first instead of building on it.
 */
public final class NaiveLedgerTransferService implements LedgerTransferService {

    @Override
    public void transfer(Account from, Account to, Money amount) {
        Money fromBalance = from.balance();
        if (fromBalance.isLessThan(amount)) {
            throw new InsufficientFundsException(from, amount);
        }
        Money toBalance = to.balance();

        // Deliberately no lock between the reads above and the writes below: this is the gap
        // that T-003's test needs to exploit.
        from.setBalance(fromBalance.minus(amount));
        to.setBalance(toBalance.plus(amount));
    }
}
