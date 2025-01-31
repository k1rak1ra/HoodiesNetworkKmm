import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
}

group = "net.k1ra.hoodies_network_kmm"
version = System.getenv("releaseName") ?: "999999.999999.999999"

sqldelight {
    databases {
        create("HoodiesNetworkDatabase") {
            packageName.set("net.k1ra.hoodies_network_kmm.database")
            generateAsync.set(true)
        }
    }
    linkSqlite = true
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
        publishLibraryVariants("release", "debug")
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val iosFrameworkName = "HoodiesNetworkKmm"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework(iosFrameworkName)
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }

    jvm()

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

        jvmMain.dependencies {
            implementation(libs.sqldelight.driver.jdbc)
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(libs.sqldelight.driver.web)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", libs.versions.sqlJsWorker.get()))
            implementation(npm("sql.js", libs.versions.sqlJs.get()))
            implementation(devNpm("copy-webpack-plugin", libs.versions.webPackPlugin.get()))
        }
    }
}

android {
    namespace = "net.k1ra.hoodies_network_kmm"

    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
        maven {
            name = "k1ra-nexus"
            url = uri("https://k1ra.net/nexus/repository/public/")

            credentials(PasswordCredentials::class) {
                username = System.getenv("NEXUS_USERNAME") ?: "anonymous"
                password = System.getenv("NEXUS_PASSWORD") ?: ""
            }
        }
    }
}

tasks{
    register<Jar>("dokkaJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }
}