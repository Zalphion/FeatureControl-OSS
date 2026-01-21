plugins {
    id("com.gradleup.shadow")
    id("com.google.devtools.ksp")
    application
}

dependencies {
    api(project(":emails"))
    implementation(project(":storage:postgres"))
    api("org.http4k:http4k-config")
    api("org.http4k:http4k-server-undertow")

    implementation("com.sun.mail:jakarta.mail:_")

    runtimeOnly("org.webjars.npm:alpinejs:_")
    runtimeOnly("org.webjars.npm:dayjs:_")
    runtimeOnly("org.tinylog:slf4j-tinylog:_")
    runtimeOnly("org.tinylog:tinylog-impl:_")

    ksp("se.ansman.kotshi:compiler:_")
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