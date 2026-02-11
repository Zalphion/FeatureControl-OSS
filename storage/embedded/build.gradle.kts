dependencies {
    api(project(":core"))
    implementation(libs.flyway.core)
    implementation(libs.h2)

    testImplementation(testFixtures(project(":core")))
}