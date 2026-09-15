rootProject.name = "atomic-ledger"

include(":ledger-core")
include(":isolation-lab")
include(":settlement-worker")
include(":balance-read-model")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
