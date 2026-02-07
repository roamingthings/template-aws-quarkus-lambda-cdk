import org.gradle.api.tasks.testing.logging.TestExceptionFormat

group = "de.roamingthings"
version = "1.0.0-SNAPSHOT"

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        mavenLocal()
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events("skipped", "failed")
            showStandardStreams = false
            exceptionFormat = TestExceptionFormat.FULL
            showCauses = true
            showStackTraces = true
        }
    }
}
