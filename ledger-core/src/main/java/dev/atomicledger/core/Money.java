package dev.atomicledger.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An immutable money amount. Given as complete plumbing (T-002) so the concurrency
 * and transaction exercises further down the task board aren't blocked on it.
 */
public final class Money {

    public static final Money ZERO = Money.of(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money of(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        return new Money(amount);
    }

    public static Money of(String amount) {
        return of(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isLessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }

    public BigDecimal asBigDecimal() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money m && amount.compareTo(m.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
