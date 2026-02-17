package com.zalphion.featurecontrol.web.components

import com.zalphion.featurecontrol.web.AriaPopup
import com.zalphion.featurecontrol.web.PageLink
import com.zalphion.featurecontrol.web.ariaControls
import com.zalphion.featurecontrol.web.ariaDisabled
import com.zalphion.featurecontrol.web.ariaHasPopup
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.cssStyle
import com.zalphion.featurecontrol.web.tooltip
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
    label: String = tooltip,
    enabled: Boolean = true,
    dropdownToCloseId: String? = null, // dropdown to close when clicked
    attrs: BUTTON.() -> Unit = {}
) {
    button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
        ariaHasPopup = AriaPopup.Dialog
        ariaControls = modalId
        disabled = !enabled
        ariaDisabled = !enabled
        attrs()

        this.tooltip = tooltip
        ariaLabel = label // icon-only means we need an assistive label
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
        ariaControls = modalId
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
    val dropdownId = "more-$resourceId"

    button(type = ButtonType.button, classes = "uk-icon-button") {
        attributes["uk-icon"] = "icon: more-vertical"
        ariaLabel = "More Options"
        ariaControls = dropdownId
        ariaHasPopup = AriaPopup.Menu
    }

    div {
        id = dropdownId
        attributes["uk-dropdown"] = "mode: click"

        ul("uk-nav uk-dropdown-nav") {
            icons(dropdownId)
        }
    }
}

fun FlowContent.navButton(page: PageLink, selected: Boolean) {
    a(
        href = if (page.enabled) page.uri.toString() else "#",
        classes = "uk-button uk-button-large uk-width-1-1"
    ) {
        tooltip = page.tooltip
        ariaDisabled = !page.enabled
        classes += if (selected) "uk-button-primary" else "uk-button-default"
        if (!page.enabled) classes += "uk-text-muted"
        style = cssStyle(
            "padding-bottom" to "10px",
            "padding-top" to "10px",
            "margin-top" to "10px",
            "margin-bottom" to "10px"
        )

        span("uk-margin-small-right") {
            attributes["uk-icon"] = page.spec.icon
        }
        +page.spec.name
    }
}