package com.zalphion.featurecontrol.web

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.id

fun FlowContent.modal(
    modalId: String,
    event: AlpineEvent? = null,
    withCloseButton: Boolean = true,
    header: FlowContent.() -> Unit,
    body: FlowContent.() -> Unit,
    footer: (FlowContent.() -> Unit)?
) = div("uk-modal uk-modal-container") {
    id = modalId


    if (event != null) {
        attributes["@${event.eventId}.window"] = $$"$${event.dataKey} = $event.detail"
        attributes["x-data"] = "{ ${event.dataKey}: null }"
    }

    div("uk-modal-dialog") {
        attributes["x-cloak"] = ""

        div("uk-modal-header") {
            if (withCloseButton) {
                button(type = ButtonType.button, classes = "uk-modal-close-default") {
                    attributes["aria-label"] = "Close"
                    attributes["uk-close"] = ""
                }
            }

            h2("uk-modal-title", block = header)
        }


        div("uk-modal-body", block = body)

        if (footer != null) {
            div("uk-modal-footer", block = footer)
        }
    }
}

fun FlowContent.modalCloseButton(text: String = "Close") {
    button(type = ButtonType.button, classes = "uk-button uk-button-default uk-modal-close") {
        +text
    }
}