# T-009 — CQRS read model (stretch, optional)

**Module:** `balance-read-model` · **Closes:** reinforcement, not a gap — the CQRS answer from the
original interview was already strong. Do this after T-001–T-008, and only if you have time left.

## What's already there

A bare, complete Spring Boot entry point (`BalanceReadModelApplication`) and an `application.yml`
pointed at `localhost:5432` (works against `docker compose up -d`'s Postgres). No domain types, no
controller — this task is the whole module.

## Why this module is Java 21, not 25

See [docs/04-architecture.md](../docs/04-architecture.md) — the Spring Boot Gradle plugin's ASM
can't parse Java 25 class files yet. Not a design choice specific to CQRS, just where the pin
happened to land.

## What to build

1. **A write-side event, minimally.** Doesn't need to be real event sourcing — a simple
   `TransferRecorded(fromId, toId, amount, timestamp)` published somewhere `ledger-core`'s transfer
   services could call is enough to make the CQRS shape real rather than hypothetical. (Given the
   Java-25/21 split, the cleanest option is: define this event type *inside*
   `balance-read-model` itself, and have a small standalone "producer" in this module publish
   synthetic transfer events directly into the projection for now, rather than wiring a live
   dependency from `ledger-core` — note in your own README why, referencing the toolchain split.)
2. **A projection.** A listener/handler that takes a `TransferRecorded` event and updates a
   `balances` read table (`account_id`, `balance`) — debit `fromId`, credit `toId`. This is the
   write side of CQRS's read side: eventually consistent with the ledger, optimized for query, not
   for transactional correctness (that guarantee already lives in `ledger-core`).
3. **A query endpoint.** `GET /accounts/{id}/balance` returning the current projected balance from
   the read table — plain Spring MVC + JDBC, no need for anything fancier.
4. *(Optional, matches the original JD's Redis mention)* Cache the projection in Redis instead of
   (or in addition to) Postgres, with a short TTL, to make the "why would you actually do this"
   answer concrete: the write side stays the source of truth, the read side can be denormalized,
   cached, and scaled independently.

## Definition of done

A running `GET /accounts/{id}/balance` returning a value that changes after a synthetic
`TransferRecorded` event is published, plus one integration test (Testcontainers Postgres, matching
the pattern in `isolation-lab`) proving the projection updates correctly.
