package com.zalphion.featurecontrol.web

import kotlinx.html.FlowContent
import kotlinx.html.HTMLTag
import kotlinx.html.time
import java.time.Instant

fun cssStyle(vararg styles: Pair<String, String>) =
    styles.joinToString(";") { (key, value) -> "$key: $value" }

fun FlowContent.timestamp(time: Instant) = time("timestamp") {
    attributes["datetime"] = time.toString() //
    +time.toString()
}

var HTMLTag.ariaLabel: String
    get() = attributes["aria-label"] ?: ""
    set(value) { attributes["aria-label"] = value }

var HTMLTag.ariaLabelledBy: String
    get() = attributes["aria-labelledby"] ?: ""
    set(value) { attributes["aria-labelledby"] = value }

var HTMLTag.ariaControls: String
    get() = attributes["aria-controls"] ?: ""
    set(value) { attributes["aria-controls"] = value }

var HTMLTag.ariaHasPopup: AriaHasPopup
    get() = attributes["aria-haspopup"]
        ?.let { value -> AriaHasPopup.entries.first { entry -> entry.toString().lowercase() == value } }
        ?: AriaHasPopup.False
    set(value) { attributes["aria-haspopup"] = value.toString().lowercase() }

enum class AriaHasPopup {
    False, True, Menu, ListBox, Tree, Grid, Dialog;
}

var HTMLTag.ariaHidden: Boolean
    get() = attributes["aria-hidden"]?.toBoolean() ?: false
    set(value) { attributes["aria-hidden"] = if (value) "true" else "false" }