// isolation-lab — the transaction-isolation module (T-005/T-006).
// A two-connection JDBC harness against a real Postgres (via Testcontainers), not mocks:
// isolation-level behaviour is exactly the kind of thing that only a real database
// will tell you the truth about.

// Everything lives in the test source set: PostgresTestSupport is test infrastructure
// (Testcontainers), not production code, and this module has no production code of its own.

dependencies {
    testImplementation(libs.postgresql.driver)
    testImplementation(libs.hikaricp)
    testImplementation(libs.slf4j.api)
    testRuntimeOnly(libs.logback.classic)

    testImplementation(libs.bundles.testing.core)
    testImplementation(libs.bundles.testcontainers.pg)
}
