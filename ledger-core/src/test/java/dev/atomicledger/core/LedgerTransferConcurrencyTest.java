package dev.atomicledger.core;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * T-003 / T-004 live here. Full instructions: tasks/T-003.md and tasks/T-004.md.
 *
 * <p>The shape of the test you're building, once {@code @Disabled} comes off:
 * <ol>
 *   <li>Create two accounts, each starting at a known balance (e.g. 1_000.00).</li>
 *   <li>Record the total: {@code fromBalance + toBalance}.</li>
 *   <li>Using an {@link java.util.concurrent.ExecutorService} with N threads, fire a large
 *       number of small, random-direction transfers between the two accounts concurrently.
 *       {@link java.util.concurrent.CountDownLatch} is the clean way to wait for every submitted
 *       transfer to finish before asserting anything.</li>
 *   <li>Assert the total is unchanged. Against {@link NaiveLedgerTransferService} this should
 *       FAIL (that's the point — you're proving the bug, not hiding it). Re-point the same test
 *       at {@link SynchronizedLedgerTransferService} and then {@link LockingLedgerTransferService}
 *       once each is implemented; both should pass.</li>
 * </ol>
 *
 * <p>A parameterized test (one {@code @ParameterizedTest} over the three implementations) is a
 * clean way to avoid triplicating this test once all three exist — worth doing, not required to
 * finish T-003 itself.
 */
class LedgerTransferConcurrencyTest {

    @Test
    @Disabled("T-003: remove this annotation once the test body below is implemented")
    void concurrentTransfersPreserveTotalBalance() {
        // TODO T-003: build the scenario described above against NaiveLedgerTransferService
        // and assert it FAILS today. Then implement T-004a/T-004b and confirm it passes there.
    }
}
