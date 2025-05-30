pluginManagement {
    repositories {
        //        在中国区域的时候，使用阿里云加速
        if (java.util.Locale.getDefault().country == "CN") {
            maven {
                url = java.net.URI("https://maven.aliyun.com/repository/public/")
            }
        }

        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    @Suppress("UnstableApiUsage")
    repositories {
        //        在中国区域的时候，使用阿里云加速
        if (java.util.Locale.getDefault().country == "CN") {
            maven {
                url = java.net.URI("https://maven.aliyun.com/repository/public/")
            }
        }
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "common-ext"

