// ledger-core — the concurrency-correctness module.
// Deliberately plain Java + JDBC, no Spring: the point of T-003/T-004 is owning
// locking and transaction semantics by hand, not delegating them to a framework default.

dependencies {
    implementation(libs.postgresql.driver)
    implementation(libs.hikaricp)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    testImplementation(libs.bundles.testing.core)
    testImplementation(libs.bundles.testcontainers.pg)
}
