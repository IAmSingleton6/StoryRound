import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    android {
        namespace = "com.example.storyround.app.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.clientOkHttp)
        }
        commonMain.dependencies {
            api(project(":core"))
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.multiplatform.settings.noArg)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientWebSockets)
        }
        commonTest.dependencies {
            implementation(project(":testing"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.ktor.clientJs)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientCio)
        }
        jvmTest {
            languageSettings.optIn("androidx.compose.ui.test.ExperimentalTestApi")
        }
        jvmTest.dependencies {
            implementation(project(":server"))
            implementation(project.dependencies.testFixtures(project(":server")))
            implementation(libs.mockk.jvm)
            implementation(libs.kotlinx.coroutines.test)
            implementation("org.jetbrains.compose.ui:ui-test-junit4:${libs.versions.composeMultiplatform.get()}")
            implementation(compose.desktop.currentOs)
        }
        iosMain.dependencies {
            implementation(libs.ktor.clientDarwin)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
