@file:Suppress("VulnerableLibrariesLocal")


plugins {
    kotlin("jvm") version "1.9.21"
    id("maven-publish")
    id("java-library")
    signing
    id("org.jetbrains.dokka") version "1.9.10"
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
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
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