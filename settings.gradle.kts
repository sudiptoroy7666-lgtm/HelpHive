pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        // Android Gradle Plugin
        id("com.android.application") version "8.2.2"
        id("com.android.library") version "8.2.2"

        // Kotlin
        id("org.jetbrains.kotlin.android") version "1.9.22"

        // Hilt
        id("com.google.dagger.hilt.android") version "2.57.2"

        // KSP
        id("com.google.devtools.ksp") version "1.9.22-1.0.16"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        // Needed for some libraries like MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "HelpHive"
include(":app")
