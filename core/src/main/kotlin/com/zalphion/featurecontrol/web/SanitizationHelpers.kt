package com.zalphion.featurecontrol.web

fun String.sanitizeSearchTerm() = lowercase()
    .replace(" ", "")
    .replace("'", "")