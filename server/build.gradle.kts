plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
}

group = "com.example.storyround"
version = "1.0.0"
application {
    mainClass = "com.example.storyround.ApplicationKt"
}

dependencies {
    api(project(":core"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.ktor)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebSockets)
    implementation(libs.ktor.serializationJson)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.clientWebSockets)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.mockk.jvm)
    testImplementation(libs.kotlinx.coroutines.test)
}
