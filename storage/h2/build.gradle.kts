dependencies {
    api(project(":storage-jdbc"))
    implementation(libs.h2)

    testImplementation(testFixtures(project(":core")))
}