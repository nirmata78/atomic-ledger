package dev.atomicledger.readmodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OPTIONAL / STRETCH (T-009). The query side of a small CQRS slice: {@code ledger-core}'s
 * transfers are the write model; this module projects them into a read-optimized view and
 * serves it over HTTP. Given complete as an entry point — T-009 is the projection and the
 * controller, not this class.
 */
@SpringBootApplication
public class BalanceReadModelApplication {
    public static void main(String[] args) {
        SpringApplication.run(BalanceReadModelApplication.class, args);
    }
}
