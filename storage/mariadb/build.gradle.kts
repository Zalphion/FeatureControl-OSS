dependencies {
    api(project(":storage-jdbc"))
    runtimeOnly(libs.mariadb)
    runtimeOnly(libs.flyway.mysql)
    implementation(libs.hikari.cp)

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.testcontainers.mariadb)
    testImplementation(libs.testcontainers.junit.jupiter)
}