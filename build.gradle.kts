@file:Suppress("VulnerableLibrariesLocal")

import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


plugins {
    kotlin("jvm") version "1.9.21"
    id("maven-publish")
    id("java-library")
    signing
    id("org.jetbrains.dokka") version "1.9.10"
    kotlin("plugin.jpa") version "1.9.21"
}

/**
 * @return 获取真实版本号。
 */
fun fetchRealVersion() = project.findProperty("fixedVersion")?.toString() ?: project.version.toString().let {
    if (it == Project.DEFAULT_VERSION) "1.0-SNAPSHOT"
    else it
}

group = "io.github.caijiang"
version = fetchRealVersion()

repositories {
//        在中国区域的时候，使用阿里云加速
    if (Locale.getDefault().country == "CN") {
        maven {
            url = URI("https://maven.aliyun.com/repository/public/")
        }
    }

    mavenLocal()
    mavenCentral()
}

val ee = project.findProperty("ee") ?: "java" // jakarta
val jdkVersion = if (ee == "java") 8 else 17
// https://stackoverflow.com/questions/77539033/how-to-avoid-compatibility-issues-between-java-ee-and-jakarta-ee
val junitVersion = if (ee == "java") "5.6.3" else "5.10.1"
val springDataVersion = if (ee == "java") "2.5.12" else "3.2.1"
val springFrameworkVersion = if (ee == "java") "5.3.19" else "6.1.2"
val persistenceVersion = if (ee == "java") "2.2.3" else "3.1.0"
val hibernateVersion = if (ee == "java") "5.4.33" else "6.4.1.Final"
val eclipseLinkVersion = if (ee == "java") "2.7.13" else "3.0.4"
//测试使用的 jpa 引擎，默认 hibernate
val jpa = project.findProperty("jpaImpl") ?: "hibernate"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    fun compileAndTest(dependencyNotation: Any) {
        compileOnly(dependencyNotation)
        testImplementation(dependencyNotation)
    }
    compileAndTest("org.apache.httpcomponents.client5:httpclient5:5.3")
    compileAndTest("org.apache.httpcomponents:httpclient:4.5.14")
    compileOnly("jakarta.persistence:jakarta.persistence-api:$persistenceVersion")
    compileAndTest("org.springframework.data:spring-data-jpa:$springDataVersion")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.springframework:spring-test:$springFrameworkVersion")
    testImplementation("org.springframework:spring-context:$springFrameworkVersion")
//
    compileOnly("org.hibernate:hibernate-core:$hibernateVersion")
    testCompileOnly("org.hibernate:hibernate-core:$hibernateVersion")
    testCompileOnly("org.eclipse.persistence:eclipselink:$eclipseLinkVersion")
    if (jpa == "hibernate") {
        testImplementation("org.hibernate:hibernate-core:$hibernateVersion")
    } else {
        testImplementation("org.eclipse.persistence:eclipselink:$eclipseLinkVersion")
        testImplementation("jakarta.persistence:jakarta.persistence-api:$persistenceVersion")
    }

    testImplementation("org.apache.commons:commons-lang3:3.8.1")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("com.h2database:h2:2.2.224")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(jdkVersion)
}

val copyEE = tasks.create("copyEE") {
    val codeBase = "$rootDir/src/main/kotlin"
    val targetFile = Paths.get("$codeBase/io/github/caijiang/common/EEType.kt")
    Files.deleteIfExists(targetFile)
    Files.copy(Paths.get("$codeBase/io/github/caijiang/common/EEType.kt-$ee"), targetFile)
}

tasks.compileKotlin {
    compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
    dependsOn(copyEE)
}

// publish

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}

java {
    withSourcesJar()
}

extensions.configure<PublishingExtension> {
    publishing {
        repositories {
            maven {
                name = "OSSRH"
//            https://docs.gradle.org/current/samples/sample_publishing_credentials.html
                credentials(PasswordCredentials::class)
                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                val v = fetchRealVersion()
                url = uri(if (v.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            }
        }

        publications {
            create<MavenPublication>("maven") {
                pom {
                    name = "common ext(build for $ee ee)"
                    description =
                        "this is a common extensions written in kotlin, work for jvm($jdkVersion+). it almost has nothing depends in runtime, user should declare it by themself."
                    packaging = "jar"
                    url = "https://github.com/caijiang/common-ext"
                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://www.opensource.org/licenses/mit-license.php"
                        }
                    }
//                    version = fetchRealVersion()
                    developers {
                        developer {
                            id = "caijiang"
                            name = "Cai Jiang"
                            email = "luffy.ja@gmail.com"
                        }
                    }
                    scm {
                        connection = "scm:git:git@github.com:caijiang/common-ext.git"
                        developerConnection = "scm:git:git@github.com:caijiang/common-ext.git"
                        url = "https://github.com/caijiang/common-ext"
                    }
                }
                from(components["java"])
//                version = fetchRealVersion()
                groupId = project.group.toString()
                artifactId = project.name + "-" + ee
//                    println("read information from project ${artifactId}:${version} in group: $groupId")

                if (project.findProperty("withDocument") != null) {
                    artifact(dokkaJavadocJar)
                    artifact(dokkaHtmlJar)
                }
                versionMapping {
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
//                usage("java-api") {
//                    fromResolutionOf("runtimeClasspath")
//                }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}