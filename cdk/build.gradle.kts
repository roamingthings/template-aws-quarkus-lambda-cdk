plugins {
    java
    application
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
    implementation(libs.aws.cdk.lib)
    implementation(libs.aws.cdk.constructs)
    implementation(libs.jspecify)
}

application {
    mainClass.set("de.roamingthings.CdkApp")
}

tasks.clean {
    delete.add("cdk.out")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
