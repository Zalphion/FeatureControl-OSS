rootProject.name = "oss"

fun String.includeModule(name: String) {
    val projectName = "$this-$name"
    include(":$projectName")
    project(":$projectName").projectDir = File("$this/${name.replace(':','/')}")
}

include("core", "emails", "hosted")

"storage".apply {
    includeModule("jdbc")
    includeModule("postgres")
    includeModule("embedded")
    includeModule("mariadb")
    includeModule("couchdb")
}