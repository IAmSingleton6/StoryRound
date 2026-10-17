plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    `java-test-fixtures`
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
    testFixturesApi(project(":testing"))
    testFixturesApi(platform(libs.koin.bom))
    testFixturesApi(libs.koin.core)
    testFixturesApi(libs.ktor.serverTestHost)
    testFixturesApi(libs.ktor.clientWebSockets)
    testFixturesApi(libs.kotlinx.coroutines.test)
    testFixturesApi(libs.kotlin.test)
    testFixturesImplementation(libs.ktor.serverWebSockets)
    testFixturesImplementation(libs.mockk.jvm)
    testFixturesImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.clientWebSockets)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.mockk.jvm)
    testImplementation(libs.kotlinx.coroutines.test)
}
