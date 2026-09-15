// balance-read-model — OPTIONAL / STRETCH module (T-009).
// The CQRS slice: a write model (ledger-core's transfers) projects into a separate,
// query-optimized read model exposed over HTTP. This is the one module where a framework
// earns its keep, since the point here is the query-side API surface, not raw JDBC practice.

plugins {
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
}

// Spring Boot 3.4.x's Gradle plugin can't parse Java 25 class files yet (ASM lags new class
// file versions by a release or two) -- pinned to 21 LTS here rather than repo-wide, since
// this is the one module that needs the framework's tooling more than it needs the newest
// language toolchain. A real, defensible reason to have a per-module toolchain, and a fair
// thing to bring up if asked about it: "we pin the Spring modules a notch behind while the
// plugin catches up, everything else tracks current."
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// No dependency on :ledger-core on purpose: that module targets Java 25 and this one is
// pinned to 21 (see above), so Gradle's variant-aware resolution correctly refuses to let a
// 21-targeted module consume a 25-targeted one. In real CQRS this is also the more honest
// shape anyway -- the read side gets its own DTOs rather than reusing the write side's
// domain types. Define your own minimal read-model types here in T-009.
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly(libs.postgresql.driver)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(libs.bundles.testcontainers.pg)
}
