import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kover)
}

group = "net.k1ra.hoodies_network_kmm"
version = "1.0.0"

sqldelight {
    databases {
        create("HoodiesNetworkDatabase") {
            packageName.set("net.k1ra.hoodies_network_kmm.database")
        }
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release", "debug")
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            linkerOpts.add("-lsqlite3")
        }
    }

    val iosFrameworkName = "HoodiesNetworkKmm"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework(iosFrameworkName)
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.coroutines)
            api(libs.kotlin.serialization)
            api(libs.kotlin.reflect)
            api(libs.kotlin.datetime)
            api(libs.korlibs.crypto)
            implementation(libs.bundles.sqldelight.common)
            implementation(compose.foundation)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.uuid)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
            implementation(libs.sqldelight.driver.jdbc)
            implementation(libs.androidx.crypto)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.ios)
        }
    }
}

android {
    namespace = "net.k1ra.hoodies_network_kmm"

    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        debug {
            testCoverage {
                enableAndroidTestCoverage = true
            }
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}