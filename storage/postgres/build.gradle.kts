dependencies {
    api(project(":core"))

    implementation(libs.hikari.cp)
    implementation(libs.flyway.core)

    runtimeOnly(libs.flyway.postgresql)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(testFixtures(project(":core")))
}