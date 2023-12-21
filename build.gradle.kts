@file:Suppress("VulnerableLibrariesLocal")

plugins {
    kotlin("jvm") version "1.9.21"
}

group = "io.github.caijiang"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    fun compileAndTest(dependencyNotation: Any) {
        compileOnly(dependencyNotation)
        testImplementation(dependencyNotation)
    }
    compileAndTest("org.apache.httpcomponents.client5:httpclient5:5.3")
    compileAndTest("org.apache.httpcomponents:httpclient:4.5.14")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.slf4j:slf4j-simplªe:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}