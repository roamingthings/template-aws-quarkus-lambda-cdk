plugins {
    java
}

repositories {
    mavenCentral()
    mavenLocal()
}

group = "de.roamingthings"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

dependencies {
    implementation(platform(libs.aws.sdk.bom))
    implementation(libs.aws.sdk.lambda)
    implementation(platform(libs.slf4j.bom))
    implementation(libs.slf4j.simple)
    testImplementation(platform(libs.junit.jupiter.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.assertj.core)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
