plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    api(project(":core"))
    implementation(libs.http4k.format.moshi) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }

    ksp(libs.kotshi.compiler)

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.testcontainers.junit.jupiter)
}