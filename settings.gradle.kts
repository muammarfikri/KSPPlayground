pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    plugins {
        id("com.google.devtools.ksp") version "1.7.0-1.0.6"
        kotlin("jvm") version "1.7.0"
        id("org.jetbrains.kotlin.android") version "1.7.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "KSPPlayground"
include(":app")
include(":ksp_annotation")
include(":ksp_processor")
