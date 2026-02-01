package com.zalphion.featurecontrol.web

import kotlinx.html.FlowContent
import kotlinx.html.time
import java.time.Instant

fun cssStyle(vararg styles: Pair<String, String>) =
    styles.joinToString(";") { (key, value) -> "$key: $value" }

fun FlowContent.timestamp(time: Instant) = time("timestamp") {
    attributes["datetime"] = time.toString() //
    +time.toString()
}

var FlowContent.ariaLabel: String
    get() = attributes["aria-label"] ?: ""
    set(value) { attributes["aria-label"] = value }

var FlowContent.ariaControls: String
    get() = attributes["aria-controls"] ?: ""
    set(value) { attributes["aria-controls"] = value }
