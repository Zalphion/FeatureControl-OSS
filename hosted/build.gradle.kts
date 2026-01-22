plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.ksp)
    application
}

dependencies {
    api(project(":emails"))
    api(libs.http4k.core)
    api(libs.http4k.config)
    api(libs.http4k.server.undertow)

    implementation(project(":storage:postgres"))
    implementation(libs.jakarta.mail)

    runtimeOnly(libs.webjars.alpinejs)
    runtimeOnly(libs.webjars.dayjs)
    runtimeOnly(libs.tinylog.impl)
    runtimeOnly(libs.tinylog.slf4j)

    ksp(libs.kotshi.compiler)
}

application {
    mainClass.set("com.zalphion.featurecontrol.CoreMainKt")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.shadowJar {
    minimize {
        exclude(dependency("org.postgresql:postgresql:.*"))
        exclude(dependency("org.flywaydb:flyway-database-postgresql:.*"))
        exclude(dependency("org.tinylog:tinylog-impl:.*"))
        exclude(dependency("org.tinylog:slf4j-tinylog:.*"))
        exclude(dependency("com.sun.mail:jakarta.mail:.*"))
        exclude(dependency("io.undertow:undertow-core:.*"))
    }
}