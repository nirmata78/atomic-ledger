# 00 — Overview

## What this is

`atomic-ledger` is a small banking-ledger service, built to practice — not to demonstrate mastery
of — three things: correct concurrent access to shared mutable state, the real behaviour of
PostgreSQL's transaction isolation levels, and a hand-built producer/consumer pipeline using
`java.util.concurrent`. Everything else (Spring Boot, CQRS, HTTP) is secondary and mostly optional.

## Why a ledger

A money-transfer domain forces the concurrency questions to be real rather than contrived: two
concurrent transfers touching the same account is the textbook lost-update scenario, and getting it
wrong has an obviously bad, easy-to-assert consequence (the total balance in the system changes,
which it never should). It also happens to be the domain of the interview loop this project was
built to prepare for — closing a knowledge gap and building portfolio-relevant material turned out
to be the same exercise.

## Design principles

- **Plain Java before frameworks.** `ledger-core`, `isolation-lab` and `settlement-worker` use no
  Spring at all. The whole point of these three modules is owning locking and transaction
  semantics by hand — reaching for `@Transactional(isolation = ...)` and moving on would defeat the
  exercise.
- **Real infrastructure, not mocks.** Isolation-level behaviour and locking behaviour are exactly
  the kind of thing a mock will happily lie to you about. Every test that needs a database uses a
  real PostgreSQL instance via Testcontainers.
- **Prove the bug before fixing it.** Each concurrency fix is validated by first writing a test
  that demonstrates the *un*-fixed version is broken, then confirming the same test passes against
  the fix. A fix you can't prove was necessary isn't demonstrated, it's asserted.
- **Stubs, not solutions.** Everything in this repository that IS the learning exercise is left as
  an `UnsupportedOperationException` or a `@Disabled`/`@Ignore` test with a detailed TODO. Plumbing
  (value types, schema setup, Testcontainers wiring) is given complete, because struggling with
  that doesn't close any of the three target gaps.

## Module map

```
                    ┌─────────────────┐
                    │   ledger-core    │   accounts, transfers, three
                    │  (Java 25, JDBC) │   concurrency-correctness strategies
                    └─────────────────┘
                              ▲
                              │ (no dependency — deliberately decoupled,
                              │  see docs/04-architecture.md)
                    ┌─────────────────┐
                    │  isolation-lab   │   two-connection JDBC harness,
                    │ (Java 25, tests) │   real Postgres via Testcontainers
                    └─────────────────┘

    ┌───────────────────┐        ┌──────────────────────┐
    │ settlement-worker  │        │  balance-read-model   │
    │ (Java 21 — Spock)  │        │  (Java 21 — Spring)   │
    │ BlockingQueue       │        │  OPTIONAL: CQRS query │
    │ producer/consumer   │        │  side over the ledger │
    └───────────────────┘        └──────────────────────┘
```

See [04 Architecture](04-architecture.md) for why two modules are pinned to Java 21 and why
`ledger-core` isn't a shared dependency of the other three.
