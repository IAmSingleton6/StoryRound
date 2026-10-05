import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

apply(from = rootProject.file("gradle/server-url.gradle.kts"))

val serverUrl: String by extra
val appVersionCode =
    providers
        .gradleProperty("versionCode")
        .orElse("1")
        .map(String::toInt)
        .get()
val appVersionName =
    providers
        .gradleProperty("versionName")
        .orElse("1.0.0")
        .get()
val releaseSigningValues =
    listOf(
        providers.environmentVariable("ANDROID_KEYSTORE_FILE").orNull,
        providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull,
        providers.environmentVariable("ANDROID_KEY_ALIAS").orNull,
        providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull,
    )
val hasReleaseSigning = releaseSigningValues.all { !it.isNullOrBlank() }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":app:shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.example.storyround"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.example.storyround"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        buildConfigField("String", "STORY_ROUND_SERVER_URL", "\"$serverUrl\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig =
                    signingConfigs.maybeCreate("release").apply {
                        storeFile = file(requireNotNull(releaseSigningValues[0]))
                        storePassword = requireNotNull(releaseSigningValues[1])
                        keyAlias = requireNotNull(releaseSigningValues[2])
                        keyPassword = requireNotNull(releaseSigningValues[3])
                    }
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}
