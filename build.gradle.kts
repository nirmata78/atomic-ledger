plugins {
    java
}

allprojects {
    group = "dev.atomicledger"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        // Gradle 9's test executor needs this on the classpath explicitly; older Gradle
        // versions pulled it in implicitly, so this is a real, current-day gotcha to know
        // about rather than boilerplate for its own sake. Kept as a direct coordinate (not
        // routed through the version catalog) because VersionCatalogsExtension isn't
        // reliably visible from inside a root-level `subprojects {}` block.
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher:1.11.4")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        maxParallelForks = 1
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:all,-processing")
    }
}
