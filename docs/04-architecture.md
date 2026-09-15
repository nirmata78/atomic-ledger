# 04 — Architecture

## Why `ledger-core` isn't a shared dependency

It might look like an oversight that `settlement-worker` and `balance-read-model` don't depend on
`ledger-core` for its `Account`/`Money` types. It's deliberate, and it's the direct result of
something that broke during scaffolding — worth knowing about because it's a real toolchain
constraint, not a style preference.

## The Java-25-vs-tooling problem

`ledger-core` and `isolation-lab` target **Java 25**. Two other things in the wider Java ecosystem
don't parse Java 25 class files yet:

1. **The Spring Boot Gradle plugin** (`org.springframework.boot`) scans compiled classes for
   `@SpringBootApplication` using ASM, and the version bundled with the plugin release used here
   throws `Unsupported class file major version 69` (69 = Java 25) on anything compiled at that
   language level.
2. **Groovy's bundled ASM** (used by the Spock compiler to resolve types while transforming a
   spec) hits the exact same error — and it decompiles *every* class on the compiler's classpath,
   including a module's own main output, not just external dependencies. So even a Spock module
   with zero external project dependencies breaks if its own main sources compile to Java 25.

Both are the same underlying cause: tooling that reads compiled bytecode directly, built before
Java 25 existed, lagging the newest class file version by a release or two. It's a normal, recurring
kind of friction — not a bug in this project — and the fix in both cases is the same pattern:

**Pin the affected module's own toolchain down to a supported LTS (21), rather than fighting the
compiler.** `settlement-worker` and `balance-read-model` both do this explicitly in their
`build.gradle.kts`, with a comment explaining why.

## Why that forces `ledger-core` to be decoupled

Gradle's dependency resolution is JVM-target-aware: a module targeting Java 21 is not allowed to
depend on a module targeting Java 25, because 25-targeted bytecode isn't guaranteed runnable on a
21 JVM. Once `settlement-worker` and `balance-read-model` were pinned to 21, a
`project(":ledger-core")` dependency from either of them became a hard resolution failure, not a
warning.

Rather than dragging `ledger-core` down to 21 as well — which would have meant the "Java 25
monorepo" this project is meant to be practice for isn't actually testing anything on Java 25 —
the fix was to check whether the dependency was load-bearing. It wasn't: `settlement-worker`'s
`PendingEntry`/`SettlementProcessor` never needed `Account`/`Money`, and a read-model naturally
wants its own DTOs independent of the write side's domain types anyway (a legitimately more honest
CQRS shape, not just a workaround). Dropping the dependency fixed the build and arguably improved
the design.

## The resulting shape

```
ledger-core        Java 25    no project dependencies
isolation-lab       Java 25    no project dependencies (test-only module; no main sources)
settlement-worker   Java 21    no project dependencies (pinned: Groovy/Spock)
balance-read-model  Java 21    no project dependencies (pinned: Spring Boot plugin ASM)
```

Four independent modules sharing a build convention (the root `build.gradle.kts`) rather than a
domain-model dependency graph. For a project this size that's a fine trade — if it grew large
enough that duplicating small value types across modules actually hurt, the next step would be a
`shared-kernel` module built to the lowest common toolchain (21) that all four could depend on.
That's future scope, not something T-001 through T-010 need.
