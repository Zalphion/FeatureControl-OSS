dependencies {
    api(project(":storage-jdbc"))

    implementation(libs.hikari.cp)

    runtimeOnly(libs.flyway.postgresql)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(testFixtures(project(":core")))
}