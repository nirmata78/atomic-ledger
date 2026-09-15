package dev.atomicledger.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A trivial, already-passing sanity check — proof that the toolchain (JUnit 5 + AssertJ,
 * Java 25) is wired correctly before you touch anything concurrency-related. If this test
 * doesn't pass on a fresh checkout, fix the build first; nothing past this point will make
 * sense until it does.
 */
class MoneyTest {

    @Test
    void addsAndSubtracts() {
        Money ten = Money.of("10.00");
        Money three = Money.of("3.00");

        assertThat(ten.plus(three)).isEqualTo(Money.of("13.00"));
        assertThat(ten.minus(three)).isEqualTo(Money.of("7.00"));
    }

    @Test
    void comparesByValueNotScale() {
        assertThat(Money.of(new BigDecimal("5"))).isEqualTo(Money.of(new BigDecimal("5.00")));
    }
}
