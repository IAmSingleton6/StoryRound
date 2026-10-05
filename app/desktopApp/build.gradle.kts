import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.Copy
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

apply(from = rootProject.file("gradle/server-url.gradle.kts"))

val serverUrl: String by extra
val packageVersion = providers.gradleProperty("versionName").orElse("1.0.0").get()
val generatedServerUrlDir = layout.buildDirectory.dir("generated/serverUrl/main/kotlin")
val generateServerUrlSource by tasks.registering(Copy::class) {
    from("src/main/templates") {
        include("DesktopServerUrl.kt.template")
        filter<ReplaceTokens>("tokens" to mapOf("serverUrl" to serverUrl))
        rename { "DesktopServerUrl.kt" }
    }
    into(generatedServerUrlDir)
}

kotlin.sourceSets.main {
    kotlin.srcDir(generatedServerUrlDir)
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateServerUrlSource)
}

dependencies {
    implementation(project(":app:shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}
compose.desktop {
    application {
        mainClass = "com.example.storyround.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.storyround"
            packageVersion = packageVersion
        }
    }
}
