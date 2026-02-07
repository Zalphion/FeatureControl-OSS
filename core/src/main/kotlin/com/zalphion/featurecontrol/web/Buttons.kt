package com.zalphion.featurecontrol.web

import dev.forkhandles.values.StringValue
import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.onClick
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.ul
import kotlin.collections.plus

fun FlowContent.modalIconButton(
    tooltip: String,
    icon: String,
    modalId: String,
    dropdownToCloseId: String? = null, // dropdown to close when clicked
    attrs: BUTTON.() -> Unit = {}
) {
    button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
        ariaHasPopup = AriaPopup.Dialog
        ariaControls = modalId
        attrs()

        attributes["uk-tooltip"] = tooltip
        attributes["aria-label"] = tooltip // icon-only means we need an assistive label
        onClick = "${if (dropdownToCloseId == null) "" else "UIkit.dropdown('#$dropdownToCloseId').hide(true); "}UIkit.modal('#$modalId').show()"
        span {
            attributes["uk-icon"] = icon
        }
    }
}

fun FlowContent.modalTextButton(
    label: String,
    classes: String = "uk-button uk-button-text uk-text-muted uk-margin-xsmall",
    modalId: String,
    icon: String? = null,
    dropdownToCloseId: String? = null,  // dropdown to close when clicked
    attrs: BUTTON.() -> Unit = {}
) {
    button(type = ButtonType.button, classes = classes) {
        ariaHasPopup = AriaPopup.Dialog
        attrs()

        onClick = "${if (dropdownToCloseId == null) "" else "UIkit.dropdown('#$dropdownToCloseId').hide(true); "}UIkit.modal('#$modalId').show()"
        if (icon != null) {
            span("uk-margin-small-right") {
                attributes["uk-icon"] = icon
            }
        }
        +label
    }
}

fun FlowContent.moreMenu(resourceId: StringValue, icons: UL.(String) -> Unit) {
    button(type = ButtonType.button, classes = "uk-icon-button") {
        attributes["uk-icon"] = "icon: more-vertical"
        attributes["aria-label"] = "More Options"
    }

    val dropdownId = "more-$resourceId"
    div {
        id = dropdownId
        attributes["uk-dropdown"] = "mode: click"

        ul("uk-nav uk-dropdown-nav") {
            icons(dropdownId)
        }
    }
}

fun FlowContent.navButton(page: PageLink, selected: Boolean) {
    a(page.uri.toString(), classes = "uk-button uk-button-large uk-width-1-1") {
        if (page.tooltip != null) attributes["uk-tooltip"] = page.tooltip
        if (!page.enabled) attributes["disabled"] = ""
        classes += if (selected) "uk-button-primary" else "uk-button-default"
        style = "padding-bottom: 10px; padding-top: 10px; margin-top: 10px; margin-bottom: 10px;"

        span("uk-margin-small-right") {
            attributes["uk-icon"] = page.spec.icon
        }
        +page.spec.name
    }
}