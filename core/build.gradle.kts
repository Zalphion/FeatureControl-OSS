plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api("org.http4k:http4k-core")
    api("org.http4k:http4k-format-core")
    implementation("org.http4k:http4k-format-moshi") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
    api("dev.andrewohara:service-utils:_")
    api("io.github.oshai:kotlin-logging-jvm:_")
    api("dev.forkhandles:values4k")
    api("dev.forkhandles:result4k")
    api("se.ansman.kotshi:api:_")
    api("org.jetbrains.kotlinx:kotlinx-html-jvm:_")
    api("com.nimbusds:nimbus-jose-jwt:_")

    runtimeOnly("org.tinylog:slf4j-tinylog:_")
    runtimeOnly("org.tinylog:tinylog-impl:_")

    ksp("se.ansman.kotshi:compiler:_")

    testFixturesApi("dev.forkhandles:result4k-kotest")
    testFixturesApi("org.http4k:http4k-testing-playwright")
}