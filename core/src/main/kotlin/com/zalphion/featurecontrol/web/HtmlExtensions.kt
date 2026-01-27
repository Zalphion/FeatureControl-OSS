package com.zalphion.featurecontrol.web

import kotlinx.html.FlowContent
import kotlinx.html.span
import java.time.Instant

fun cssStyle(vararg styles: Pair<String, String>) =
    styles.joinToString(";") { (key, value) -> "$key: $value" }

fun FlowContent.timestamp(time: Instant) = span("timestamp") {
    +time.toString()
}