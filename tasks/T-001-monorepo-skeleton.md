# T-001 — Monorepo skeleton & toolchain

**Status: done.** This task exists mainly as a verification checklist and as a record of decisions
made while scaffolding, so you don't have to re-derive them later.

## What exists

- A 4-module Gradle Kotlin DSL monorepo (`ledger-core`, `isolation-lab`, `settlement-worker`,
  `balance-read-model`), driven by a version catalog at `gradle/libs.versions.toml`.
- Root `build.gradle.kts` sets a Java 25 toolchain by default for every subproject, wires JUnit 5,
  and adds the JUnit Platform launcher explicitly (Gradle 9 needs it declared; older Gradle
  versions pulled it in implicitly — see the comment in the file).
- `settlement-worker` and `balance-read-model` override that default down to Java 21, each for a
  distinct, real tooling-compatibility reason. Full explanation: [docs/04-architecture.md](../docs/04-architecture.md).
- `docker-compose.yml` for a standalone local Postgres; Testcontainers is used inside tests
  independently of it.

## Verify it yourself

```bash
./gradlew build -x :isolation-lab:test   # green without Docker
docker compose up -d                     # or have Docker Desktop running
./gradlew :isolation-lab:test            # needs a live Docker daemon
```

If either fails on a fresh clone, something about the local toolchain has drifted — check
`java -version` (need 25 available; `sdk list java` if using SDKMAN) and `docker info` before
assuming the code is at fault.
