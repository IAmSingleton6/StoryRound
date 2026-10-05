import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

apply(from = rootProject.file("gradle/server-url.gradle.kts"))

val serverUrl: String by extra
val generatedServerUrlDir = layout.buildDirectory.dir("generated/serverUrl/commonMain/kotlin")
val generateServerUrlSource by tasks.registering(Copy::class) {
    from("src/commonMain/templates") {
        include("WebServerUrl.kt.template")
        filter<ReplaceTokens>("tokens" to mapOf("serverUrl" to serverUrl))
        rename { "WebServerUrl.kt" }
    }
    into(generatedServerUrlDir)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedServerUrlDir)
        }
        commonMain.dependencies {
            implementation(project(":app:shared"))

            implementation(libs.compose.ui)
        }
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && name.contains("Kotlin")) {
        dependsOn(generateServerUrlSource)
    }
}
