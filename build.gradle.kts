buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.1.1")
        classpath("androidx.room:room-gradle-plugin:2.8.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.60.1")
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.4.0")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.4.0")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.10")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.google.services) apply false
}
