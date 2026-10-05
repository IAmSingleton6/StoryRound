import java.util.Properties

val endpointPlatform =
    when (project.name) {
        "androidApp" -> "android"
        "desktopApp" -> "desktop"
        "webApp" -> "web"
        else -> error("No server URL platform is configured for ${project.path}.")
    }

val endpointProperties =
    Properties().apply {
        rootProject.file("config/development.properties").inputStream().use(::load)
        rootProject
            .file("config/local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use(::load)
    }

extra["serverUrl"] =
    providers.environmentVariable("STORY_ROUND_SERVER_URL").orNull
        ?: requireNotNull(endpointProperties.getProperty("$endpointPlatform.serverUrl")) {
            "Missing $endpointPlatform.serverUrl in config/development.properties."
        }
