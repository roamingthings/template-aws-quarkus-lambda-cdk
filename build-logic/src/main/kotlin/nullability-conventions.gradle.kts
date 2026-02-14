import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

plugins {
    id("net.ltgt.errorprone")
    id("net.ltgt.nullaway")
}

val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    "errorprone"(libs.findLibrary("errorprone-core").orElseThrow())
    "errorprone"(libs.findLibrary("nullaway").orElseThrow())
}

nullaway {
    onlyNullMarked = true
    jspecifyMode = true
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableAllChecks.set(true)
    }
    options.errorprone.nullaway {
        error()
    }
}
