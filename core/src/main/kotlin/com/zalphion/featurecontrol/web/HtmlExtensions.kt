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

var HTMLTag.ariaHasPopup: AriaPopup
    get() = attributes["aria-haspopup"]
        ?.let { value -> AriaPopup.entries.first { entry -> entry.toString().lowercase() == value } }
        ?: AriaPopup.False
    set(value) { attributes["aria-haspopup"] = value.toString().lowercase() }

enum class AriaPopup {
    False, True, Menu, ListBox, Tree, Grid, Dialog;
}

var HTMLTag.ariaCurrent: AriaCurrent
    get() = attributes["aria-current"]
        ?.let { value -> AriaCurrent.entries.first { entry -> entry.toString().lowercase() == value } }
        ?: AriaCurrent.False
    set(value) { attributes["aria-current"] = value.toString().lowercase() }

enum class AriaCurrent { Page, Step, Location, Date, Time, True, False }

var HTMLTag.ariaHidden: Boolean
    get() = attributes["aria-hidden"]?.toBoolean() ?: false
    set(value) { if (value) { attributes["aria-hidden"] = "true" } else attributes.remove("aria-hidden") }

var HTMLTag.ariaDisabled: Boolean
    get() = attributes["aria-disabled"]?.toBoolean() ?: false
    set(value) { if (value) { attributes["aria-disabled"] = "true" } else attributes.remove("aria-disabled") }

var HTMLTag.tooltip: String?
    get() = attributes["uk-tooltip"]
    set(value) { if (value != null) { attributes["uk-tooltip"] = value } else attributes.remove("uk-tooltip") }

