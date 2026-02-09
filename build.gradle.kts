plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.license.report)
    `java-test-fixtures`
}

val moshi: MinimalExternalModuleDependency = libs.moshi.get()

allprojects {
    repositories {
        mavenCentral()
    }

    val moshiVersion = moshi.versionConstraint.let {
        it.requiredVersion ?: it.preferredVersion ?: it.strictVersion
    }

    // ksp brings in an old version of moshi which can break intelliJ sometimes
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("${moshi.module.group}:${moshi.module.name}"))
                .using(module("${moshi.module.group}:${moshi.module.name}:$moshiVersion"))
        }
    }

    apply(plugin = "kotlin")
    apply(plugin = "java-test-fixtures")

    group = "com.zalphion.featurecontrol"
    version = "latest-SNAPSHOT"

    tasks.test {
        useJUnitPlatform {
            if (System.getenv("FAST") == "true") {
                excludeTags("playwright")
            }
            systemProperties(
                "junit.jupiter.execution.parallel.enabled" to "true",
                "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent", // parallel classes
                "junit.jupiter.execution.parallel.mode.default" to "same_thread", // sequential cases
                "junit.jupiter.execution.parallel.config.dynamic.strategy" to "dynamic",
                "junit.jupiter.execution.parallel.config.dynamic.factor" to "1",
            )
        }
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