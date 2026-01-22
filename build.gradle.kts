plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.license.report)
    `java-test-fixtures`
}

allprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "kotlin")
    apply(plugin = "java-test-fixtures")

    group = "com.zalphion.featurecontrol"
    version = "latest-SNAPSHOT"

    tasks.test {
        useJUnitPlatform()
    }

    kotlin {
        jvmToolchain(25)
    }

    tasks.compileKotlin {
        compilerOptions {
            allWarningsAsErrors = true
        }
    }
}

licenseReport {
    allowedLicensesFile = File("$rootDir/allowed-licenses.json")
}

tasks.check {
    dependsOn(tasks.checkLicense)
}