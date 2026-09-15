// settlement-worker — the java.util.concurrent module (T-007/T-008).
// A BlockingQueue producer/consumer pipeline, mirroring the "consume row IDs from a
// collection with multiple threads" shape from the Revolut screen. Tests are written in
// Spock deliberately: it is a named JD requirement (Spock/jOOQ) and otherwise absent from
// this repo's own experience, so this module is also the Spock on-ramp.

plugins {
    groovy
}

// Pinned to 21 LTS for the whole module, not just tests: Groovy/Spock's bundled ASM can't
// parse Java 25 class files (major version 69) yet, and it decompiles every class on the
// compiler's classpath -- including this module's OWN main output -- while wiring up a Spock
// spec, not just external dependencies. Same root cause as balance-read-model's pin, just
// forced one layer earlier because the groovy plugin touches main output directly.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// No dependency on :ledger-core: this module is self-contained (PendingEntry /
// SettlementProcessor don't need Account/Money), and keeping it that way sidesteps a real
// toolchain gotcha -- Groovy/Spock's bundled ASM can't yet decompile Java 25 class files, so
// depending on a Java-25-targeted project jar breaks Spock spec compilation here. If a later
// task genuinely needs ledger-core's types, pin this module to Java 21 (like
// balance-read-model does, for the same underlying reason) rather than fighting the compiler.
dependencies {
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    testImplementation(libs.groovy)
    testImplementation(libs.spock.core)
    testImplementation(libs.bundles.testing.core)
}

tasks.test {
    // Spock specs live in src/test/groovy; the groovy plugin wires that source set
    // automatically, this just makes the intent explicit for anyone reading the build file.
    testLogging.showStandardStreams = true
}
