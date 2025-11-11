@file:Suppress("VulnerableLibrariesLocal")

import org.jreleaser.model.Active
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.9.21"
    id("maven-publish")
    id("java-library")
    id("org.jetbrains.dokka") version "1.9.10"
    kotlin("plugin.jpa") version "1.9.21"
    antlr
    id("org.jreleaser") version "1.20.0"
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

val ee = project.findProperty("ee") ?: "java" // jakarta
val jdkVersion = if (ee == "java") 8 else 17
val servletApiVersion = if (ee == "java") "4.0.4" else "6.0.0"
// https://stackoverflow.com/questions/77539033/how-to-avoid-compatibility-issues-between-java-ee-and-jakarta-ee
val junitVersion = if (ee == "java") "5.6.3" else "5.10.1"
val springIntegrationVersion = if (ee == "java") "5.5.20" else "6.2.1"
val springDataVersion = if (ee == "java") "2.7.18" else "3.2.1"
val springDataRestVersion = if (ee == "java") "3.7.18" else "4.2.1"
val springFrameworkVersion = if (ee == "java") "5.3.31" else "6.1.2"
//val springFrameworkVersion = if (ee == "java") "5.1.8.RELEASE" else "6.1.2"
val springBootVersion = if (ee == "java") "2.7.18" else "3.2.1"
val persistenceVersion = if (ee == "java") "2.2.3" else "3.1.0"
val hibernateVersion = if (ee == "java") "5.6.15.Final" else "6.4.1.Final"
val redissonHibernateArtifactId = if (ee == "java") "redisson-hibernate-53" else "redisson-hibernate-6"
val eclipseLinkVersion = if (ee == "java") "2.7.13" else "3.0.4"
//测试使用的 jpa 引擎，默认 hibernate
val jpa = project.findProperty("jpaImpl") ?: "hibernate" // eclipselink

dependencies {
    fun compileAndTest(dependencyNotation: Any) {
        compileOnly(dependencyNotation)
        testImplementation(dependencyNotation)
    }

    // all basic
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.springframework.boot:spring-boot:$springBootVersion")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    compileAndTest("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    compileAndTest("com.fasterxml.jackson.module:jackson-module-kotlin")

    // test required
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    compileAndTest("org.springframework.boot:spring-boot-test:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    testImplementation("org.apache.commons:commons-lang3:3.8.1")
    compileAndTest("org.assertj:assertj-core:3.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("com.h2database:h2:2.2.224")
    testRuntimeOnly("com.mysql:mysql-connector-j:8.1.0")
    compileAndTest("org.springframework:spring-test:$springFrameworkVersion")
    testImplementation("org.springframework:spring-context:$springFrameworkVersion")

    // spring boot
    compileAndTest("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")

    // execl
    compileAndTest("com.alibaba:easyexcel:3.3.4")

    // http
    compileAndTest("org.apache.httpcomponents.client5:httpclient5:5.3")
    compileAndTest("org.apache.httpcomponents:httpclient:4.5.14")

    // jpa
    compileOnly("jakarta.persistence:jakarta.persistence-api:$persistenceVersion")
    compileAndTest("org.springframework.data:spring-data-jpa:$springDataVersion")
    compileAndTest("org.springframework.data:spring-data-rest-webmvc:$springDataRestVersion")
    compileOnly("org.hibernate:hibernate-core:$hibernateVersion")
    testCompileOnly("org.hibernate:hibernate-core:$hibernateVersion")
    testCompileOnly("org.eclipse.persistence:eclipselink:$eclipseLinkVersion")
    if (jpa == "hibernate") {
        testImplementation("org.hibernate:hibernate-core:$hibernateVersion")
    } else {
        testImplementation("org.eclipse.persistence:eclipselink:$eclipseLinkVersion")
        testImplementation("jakarta.persistence:jakarta.persistence-api:$persistenceVersion")
    }

    // redisson
    compileOnly("org.redisson:$redissonHibernateArtifactId:3.26.0")
    compileAndTest("org.springframework.integration:spring-integration-core:$springIntegrationVersion")

    // aop
    compileAndTest("org.springframework:spring-aspects:$springFrameworkVersion")

    // mvc
    compileAndTest("org.springframework:spring-webmvc:$springFrameworkVersion")

    // test support
    compileAndTest("com.wix:wix-embedded-mysql:4.6.2")
    val inLinux = "Linux".equals(System.getProperty("os.name"), true)
    if (inLinux) {
        compileAndTest("com.github.kstyrc:embedded-redis:0.6")
    } else {
        compileAndTest("com.github.codemonstur:embedded-redis:1.0.0")
    }

    antlr("org.antlr:antlr4:4.13.0")
    compileAndTest("org.springframework.data:spring-data-jpa:$springDataVersion")

    // aliyun
    compileAndTest("org.apache.sshd:sshd-core:2.14.0")
    compileAndTest("com.aliyun:alibabacloud-alb20200616:1.0.13")
    compileAndTest("com.aliyun:aliyun-java-sdk-core:4.6.0")
    compileAndTest("com.aliyun:aliyun-java-sdk-eci:1.5.6")
    compileAndTest("com.aliyun:aliyun-java-sdk-ecs:4.6.0")
    compileAndTest("com.aliyun.oss:aliyun-sdk-oss:3.18.0")

    // nacos
    compileAndTest("com.alibaba.nacos:nacos-client:2.5.0")

    // servlet-api
    compileAndTest("jakarta.servlet:jakarta.servlet-api:$servletApiVersion")

    // rocket
    compileAndTest("org.apache.rocketmq:rocketmq-spring-boot:2.3.0")

    // redis
    compileAndTest("org.springframework.data:spring-data-redis:$springDataVersion")
    // 6.3.0.RELEASE  6.1.8.RELEASE
    testImplementation("io.lettuce:lettuce-core:6.3.0.RELEASE")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

}

tasks.test {
    useJUnitPlatform()
    ignoreFailures = true
}
kotlin {
    jvmToolchain(jdkVersion)
}

sourceSets.main {
    kotlin.srcDirs("src/main/kotlin", "build/main/generatedKotlin")
}

val copyEE = tasks.create("copyEE") {
    doLast {
        val codeBase = "$rootDir/src/main/kotlin"
        val targetBase = "$rootDir/build/main/generatedKotlin"
        val fileStore =
            arrayOf(
                "io/github/caijiang/common/EEType.kt",
                "io/github/caijiang/common/hibernate/SpringRedissonRegionFactory.kt"
            )

        fileStore.forEach {
            val targetFile = Paths.get("$targetBase/$it")
            Files.deleteIfExists(targetFile)
            targetFile.toFile().parentFile.mkdirs()
            Files.copy(Paths.get("$codeBase/$it-$ee"), targetFile)
        }
    }

}

tasks.compileKotlin {
    compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
    dependsOn(copyEE, tasks.generateGrammarSource)
}

/**
 * 这是一个构件目录，作为 maven的 发布重点
 */
val stagingDir = layout.buildDirectory.dir("staging").get().asFile

tasks.jreleaserDeploy {
    dependsOn("publishMavenPublicationToStagingRepository")
}

// publish
jreleaser {
    project {

    }
    packagers {

    }
    release {
        github {
            repoOwner = "caijiang"
            name = "common-ext"
            overwrite = true
        }
    }
    signing {
        active = Active.ALWAYS
        verify = true
        armored = true
    }

    deploy {
        maven {
            mavenCentral {
                create("central") {
                    active.set(Active.RELEASE)
                    url = "https://central.sonatype.com/api/v1/publisher"
                    sign = true
                    checksums = true
                    sourceJar = true
                    javadocJar = true
                    verifyPom = true
                    stagingRepository(stagingDir.absolutePath)
                }
            }
            nexus2 {
                create("central") {
                    active.set(Active.SNAPSHOT)
                    snapshotUrl = "https://central.sonatype.com/repository/maven-snapshots/"
                    snapshotSupported = true
                    sign = true
                    checksums = true
                    sourceJar = true
                    javadocJar = true
                    verifyPom = true
                    stagingRepository(stagingDir.absolutePath)
                }
            }
            github {
                create("common-ext") {
                    active.set(Active.NEVER)
                    url.set("https://maven.pkg.github.com/caijiang/common-ext")
                    snapshotSupported = true
                    sign = true
                    checksums = true
                    sourceJar = true
                    javadocJar = true
                    verifyPom = true
                    stagingRepository(stagingDir.absolutePath)
                }

            }
        }
    }
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
    archiveBaseName = "${project.name}-$ee"
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
    archiveBaseName = "${project.name}-$ee"
}

java {
    withSourcesJar()
}

tasks.jar {
    archiveBaseName = "${project.name}-$ee"
}

tasks.named<AbstractArchiveTask>("sourcesJar") {
    archiveBaseName = "${project.name}-$ee"
    dependsOn(tasks.generateGrammarSource)
}

extensions.configure<PublishingExtension> {
    publishing {
        repositories {
            maven {
                name = "staging"
                url = uri(stagingDir)
            }
            maven {
                name = "PH"
                credentials(PasswordCredentials::class)
                val releasesRepoUrl =
                    "https://packages.aliyun.com/658024d8b488fff322e7d41e/maven/2442991-release-yx2ypj"
                val snapshotsRepoUrl =
                    "https://packages.aliyun.com/658024d8b488fff322e7d41e/maven/2442991-snapshot-zrzds5"
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

//创建一个下载任务
fun downloadFile(url: URL, target: File) {
    if (!target.exists()) {
        logger.info("downloading {} to {}", url, target)
        target.parentFile.mkdirs()
        url.openStream().use { input ->
            Files.copy(input, target.toPath())
        }
    }
}

val downloadMysqlGrammar by tasks.register("downloadMysqlGrammar") {
    doFirst {
        val home = File(rootDir, "src/main/antlr")
        downloadFile(
            URL("https://github.com/antlr/grammars-v4/raw/dependabot/maven/antlr.version-4.13.0/sql/mysql/Positive-Technologies/MySqlParser.g4"),
            File(home, "MySqlParser.g4")
        )
        downloadFile(
            URL("https://github.com/antlr/grammars-v4/raw/dependabot/maven/antlr.version-4.13.0/sql/mysql/Positive-Technologies/MySqlLexer.g4"),
            File(home, "MySqlLexer.g4")
        )
    }
}

tasks.withType<AntlrTask> {
    //source("src/main/antlr")
    //source("build/generated-src/antlr4")
    dependsOn(downloadMysqlGrammar)
    arguments = arguments + listOf("-package", "io.github.caijiang.common.mysql")
    outputDirectory = File(outputDirectory, "io/github/caijiang/common/mysql")
}

tasks.named("dokkaJavadoc") {
    dependsOn("generateGrammarSource")
}
tasks.named("dokkaHtml") {
    dependsOn("generateGrammarSource")
}