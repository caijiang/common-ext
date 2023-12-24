@file:Suppress("VulnerableLibrariesLocal")

import java.net.URI
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

val springDataVersion = "2.5.12"
val springFrameworkVersion = "5.3.27"
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
    compileOnly("javax.persistence:javax.persistence-api:2.2")
    compileAndTest("org.springframework.data:spring-data-jpa:$springDataVersion")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.springframework:spring-test:$springFrameworkVersion")
    testImplementation("org.springframework:spring-context:$springFrameworkVersion")
//
    testCompileOnly("org.hibernate:hibernate-core:5.4.33")
    testCompileOnly("org.eclipse.persistence:eclipselink:2.7.13")
    if (jpa == "hibernate") {
        testImplementation("org.hibernate:hibernate-core:5.4.33")
    } else {
        testImplementation("org.eclipse.persistence:eclipselink:2.7.13")
    }

    testImplementation("org.apache.commons:commons-lang3:3.8.1")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testRuntimeOnly("com.h2database:h2:2.2.224")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}
tasks.compileKotlin {
    compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
//    dependsOn(processResources)
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
                    name = "common ext"
                    description =
                        "this is a common extensions written in kotlin, work for jvm(8+). it almost has nothing depends in runtime, user should declare it by themself."
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
                artifactId = project.name
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