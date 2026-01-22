plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    api(libs.http4k.core)
    api(libs.http4k.format.core)
    implementation(libs.http4k.format.moshi) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    api(libs.service.utils)
    api(libs.kotlin.logging.jvm)
    api(libs.forkhandles.values4k)
    api(libs.forkhandles.result4k)
    api(libs.kotshi.api)
    api(libs.kotlinx.html)
    api(libs.nimbus.jose.jwt)

    ksp(libs.kotshi.compiler)


    testFixturesApi(libs.junit.jupiter.api)
    testFixturesApi(libs.forkhandles.result4k.kotest)
    testFixturesApi(libs.http4k.testing.playwright)
    testFixturesApi(libs.kotest.assertions.core.jvm)

    testFixturesImplementation(libs.junit.jupiter)

    testFixturesRuntimeOnly(libs.tinylog.slf4j)
    testFixturesRuntimeOnly(libs.tinylog.impl)
    testFixturesRuntimeOnly(libs.junit.platform.launcher)
}