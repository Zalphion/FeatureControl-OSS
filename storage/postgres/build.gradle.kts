dependencies {
    api(project(":core"))

    implementation("com.zaxxer:HikariCP:_")
    implementation("org.flywaydb:flyway-core:_")

    runtimeOnly("org.flywaydb:flyway-database-postgresql:_")
    runtimeOnly("org.postgresql:postgresql:_")

    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:junit-jupiter:_")
    testImplementation(testFixtures(project(":core")))
}