import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class InstallGitHooks
    @Inject
    constructor(
        private val execOperations: ExecOperations,
    ) : DefaultTask() {
        @get:Internal
        abstract val repositoryDirectory: DirectoryProperty

        @TaskAction
        fun install() {
            execOperations.exec {
                workingDir(repositoryDirectory.get().asFile)
                commandLine("git", "config", "core.hooksPath", ".githooks")
            }
        }
    }

val installGitHooks = tasks.register<InstallGitHooks>("installGitHooks") {
    group = "setup"
    description = "Configures this repository to use its committed Git hooks."
    inputs.dir(rootProject.layout.projectDirectory.dir(".githooks"))
    repositoryDirectory.set(rootProject.layout.projectDirectory)
}

gradle.allprojects {
    tasks.matching { it.name == "build" }.configureEach {
        dependsOn(installGitHooks)
    }
}
