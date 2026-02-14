tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        if (System.getenv("FAST") == "true") {
            excludeTags("playwright")
        }
        systemProperties(
            "junit.jupiter.execution.parallel.enabled" to "true",
            "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent",
            "junit.jupiter.execution.parallel.mode.default" to "concurrent",
            "junit.jupiter.execution.parallel.config.dynamic.strategy" to "dynamic",
            "junit.jupiter.execution.parallel.config.dynamic.factor" to "1",
        )
    }
}