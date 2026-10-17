import dev.detekt.gradle.Detekt
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

apply(from = "scripts/build/git-hooks.gradle.kts")

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    extensions.configure<KtlintExtension> {
        filter {
            exclude("**/build/**")
        }
    }

    tasks.withType<BaseKtLintCheckTask>().configureEach {
        exclude("**/build/**")
    }

    tasks.withType<Detekt>().configureEach {
        val buildDirectory = layout.buildDirectory.get().asFile
        exclude { it.file.startsWith(buildDirectory) }
    }

    if (project.path == ":app:shared") {
        extensions.configure<KtlintExtension> {
            additionalEditorconfig.put("ktlint_standard_function-naming", "disabled")
        }

        afterEvaluate {
            tasks.withType<BaseKtLintCheckTask>().configureEach {
                setSource(
                    fileTree("src") {
                        include("**/*.kt")
                    },
                )
            }
        }
    }
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs formatting and static-analysis checks for every project."
    dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
    dependsOn(subprojects.map { "${it.path}:detekt" })
    dependsOn(
        ":core:detektCommonMainSourceSet",
        ":core:detektCommonTestSourceSet",
        ":app:shared:detektCommonMainSourceSet",
        ":app:shared:detektCommonTestSourceSet",
        ":app:shared:detektJvmTestSourceSet",
        ":testing:detektCommonMainSourceSet",
        ":testing:detektCommonTestSourceSet",
    )
}
