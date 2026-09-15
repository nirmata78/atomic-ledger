package dev.atomicledger.isolation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

/**
 * T-005 / T-006 live here. Full instructions: tasks/T-005.md and tasks/T-006.md.
 *
 * <p>Every test method below opens TWO {@link Connection}s from the same
 * {@link PostgresTestSupport#dataSource()} — that's the whole trick to observing isolation-level
 * behaviour: one connection stands in for "transaction 1", the other for "transaction 2", and you
 * interleave their statements by hand to reproduce a specific phenomenon.
 * {@code connection.setTransactionIsolation(...)} sets the level per connection before you call
 * {@code setAutoCommit(false)} to open a transaction.
 */
class IsolationLevelHarnessTest {

    private static PostgresTestSupport db;

    @BeforeAll
    static void startDatabase() {
        db = new PostgresTestSupport();
        db.start();
    }

    @AfterAll
    static void stopDatabase() {
        db.close();
    }

    @BeforeEach
    void resetTable() {
        db.resetOrdersTable();
    }

    @Test
    @Disabled("T-005a: at READ_COMMITTED, T2 should never see T1's uncommitted UPDATE")
    void readCommittedPreventsDirtyReads() {
        // TODO T-005a:
        //  1. Open connA, connB from db.dataSource(); setTransactionIsolation(TRANSACTION_READ_COMMITTED)
        //     on both; setAutoCommit(false) on both.
        //  2. connA: UPDATE orders SET amount = 999.00 WHERE id = 1   (do NOT commit yet)
        //  3. connB: SELECT amount FROM orders WHERE id = 1           -> assert it is still 100.00
        //  4. connA: rollback().
    }

    @Test
    @Disabled("T-005b: at READ_COMMITTED, the SAME query re-run in T2 can return a different value")
    void readCommittedAllowsNonRepeatableReads() {
        // TODO T-005b:
        //  1. connB (READ_COMMITTED, autoCommit=false): SELECT amount FROM orders WHERE id = 1 -> 100.00
        //  2. connA: UPDATE orders SET amount = 150.00 WHERE id = 1; commit().
        //  3. connB: re-run the SAME select -> now 150.00, inside the SAME still-open transaction.
        //  4. That's the non-repeatable read. Then repeat 1-3 with connB at REPEATABLE_READ and
        //     show the second read stays 100.00 instead.
    }

    @Test
    @Disabled("T-006: Postgres SERIALIZABLE aborts one of two conflicting transactions instead of blocking")
    void serializableDetectsWriteSkewAndAbortsOneTransaction() {
        // TODO T-006:
        //  1. Both connections at TRANSACTION_SERIALIZABLE, autoCommit=false.
        //  2. Construct a classic write-skew pattern: both read the same row, both then write
        //     based on what they read, in a way that would be fine sequentially but isn't
        //     concurrently. (See tasks/T-006.md for a worked scenario.)
        //  3. Commit connA first -> succeeds. Commit connB -> assert it throws a SQLException
        //     with SQLState 40001 (serialization_failure). That's Postgres's SSI at work: it
        //     detects the dangerous pattern and aborts rather than silently corrupting data.
        //  4. Write one sentence in tasks/T-006.md on what the caller is expected to do when it
        //     catches that exception (hint: it's in this repo's own theory recap, §6).
    }
}
