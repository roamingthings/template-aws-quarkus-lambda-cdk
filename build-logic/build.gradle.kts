plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.errorprone.gradle.plugin)
    implementation(libs.nullaway.gradle.plugin)
}
