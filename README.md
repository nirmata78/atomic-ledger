# atomic-ledger

A small, honest banking-ledger service built to close three specific gaps that showed up during
a real interview loop: fluent recall of `java.util.concurrent`, `ReentrantLock` used correctly by
hand, and PostgreSQL transaction-isolation behaviour beyond the textbook table. It is not a
framework showcase — most of it is deliberately plain Java, because the point is owning the hard
part yourself rather than delegating it to a default.

**Status: scaffold.** The domain plumbing (value types, schemas, test harnesses, Testcontainers
wiring) is written and builds green. The actual exercises — the concurrency fixes, the
isolation-level proofs, the producer/consumer pipeline — are left as `UnsupportedOperationException`
stubs and `@Disabled`/`@Ignore` tests with detailed TODOs. See [`tasks/README.md`](tasks/README.md)
for the ordered list of what to build.

## Why this shape

Three earlier "demo project" ideas — a concurrency lab, an isolation-level playground, and a
producer/consumer pipeline — turned out to compose naturally into one coherent service instead of
three disconnected toys: a ledger that has to move money correctly under concurrent load
(`ledger-core`), has to reason honestly about what its database actually guarantees
(`isolation-lab`), and has to process a backlog of pending work with multiple worker threads
without double-processing anything (`settlement-worker`). A fourth module
(`balance-read-model`) adds an optional CQRS read side once the other three are solid.

## Modules

| Module | Closes | Approach |
|---|---|---|
| [`ledger-core`](ledger-core) | `ReentrantLock`, deadlock avoidance, race conditions, `SELECT ... FOR UPDATE` | Plain Java + JDBC. Three transfer implementations — naive (buggy, given), `synchronized`, `ReentrantLock` (ordered) — compared against the same concurrency test |
| [`isolation-lab`](isolation-lab) | The four transaction isolation levels, and where Postgres's real behaviour diverges from the SQL standard | A two-JDBC-connection test harness against a real Postgres (Testcontainers) — no mocks, because isolation behaviour is exactly the kind of thing only a real database tells you the truth about |
| [`settlement-worker`](settlement-worker) | `BlockingQueue`, `ConcurrentHashMap`, `ExecutorService`, graceful shutdown | A producer/consumer pipeline mirroring "consume row IDs from a collection with multiple threads." Tested in Spock — a deliberate on-ramp for a stack requirement otherwise absent from this codebase |
| [`balance-read-model`](balance-read-model) — *optional* | CQRS | Spring Boot query-side projection over the ledger's write model |

Full theory recap for the first three: [`docs/`](docs/README.md).

## Quick start

Requires JDK 25 (`ledger-core`, `isolation-lab`) — the repo also pins two modules to JDK 21 where
their tooling doesn't support JDK 25 yet, see below — and Docker running locally (Testcontainers
spins up real Postgres instances; nothing here uses mocks or an in-memory database).

```bash
./gradlew build                 # compiles and runs everything Docker-independent
./gradlew :isolation-lab:test   # needs Docker
docker compose up -d            # optional: a standalone Postgres for manual poking / balance-read-model
```

### A toolchain note worth knowing before you start

`settlement-worker` and `balance-read-model` are pinned to **Java 21**, not 25, each for the same
underlying reason: their tooling (Groovy/Spock's bundled ASM, and the Spring Boot Gradle plugin's
class scanning) can't parse Java 25 class files yet. Rather than fight it, each module pins its own
toolchain — a real, defensible pattern (per-module toolchains tracking what each module's tooling
actually supports) and worth a sentence if it comes up in an interview: "we pin the modules whose
tooling hasn't caught up yet, and track current everywhere else."

## Repository layout

```
atomic-ledger/
├── ledger-core/          concurrency-correctness module
├── isolation-lab/        transaction-isolation module
├── settlement-worker/    java.util.concurrent / producer-consumer module
├── balance-read-model/   optional CQRS read side
├── docs/                 theory recap + architecture (published via GitHub Pages)
├── tasks/                the ordered task board
└── docker-compose.yml    standalone Postgres for local use outside Testcontainers
```

## Publishing this

This repo is local-only right now. To make it public:

```bash
gh repo create atomic-ledger --public --source=. --remote=origin
git add -A && git commit -m "Initial scaffold: monorepo, task board, docs"
git push -u origin main
```

Then enable GitHub Pages (Settings → Pages → Source: GitHub Actions) — `.github/workflows/pages.yml`
publishes [`docs/`](docs/README.md) automatically on the next push to `main`.

## License

MIT — see [`LICENSE`](LICENSE).
