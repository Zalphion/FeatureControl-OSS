package com.zalphion.featurecontrol.web.components

import com.zalphion.featurecontrol.web.AlpineEvent
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.withRichMethod
import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.H2
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.id
import org.http4k.core.Method
import org.http4k.core.Uri

fun FlowContent.modal(
    modalId: String,
    event: AlpineEvent? = null,
    withCloseButton: Boolean = true,
    form: Pair<Method, Uri>? = null,
    header: H2.() -> Unit,
    body: DIV.() -> Unit,
    footer: (DIV.() -> Unit)?
) = div("uk-modal uk-modal-container") {
    id = modalId

    if (event != null) {
        attributes["@${event.eventId}.window"] = $$"$${event.dataKey} = $event.detail"
        attributes["x-data"] = "{ ${event.dataKey}: null }"
    }

    div("uk-modal-dialog") {
        attributes["x-cloak"] = ""

        if (form == null) {
            modalContent(withCloseButton, header, body, footer)
        } else {
            form(method = FormMethod.post, action = form.second.toString()) {
                withRichMethod(form.first)
                modalContent(withCloseButton, header, body, footer)
            }
        }
    }
}

private fun FlowContent.modalContent(
    withCloseButton: Boolean = true,
    header: H2.() -> Unit,
    body: DIV.() -> Unit,
    footer: (DIV.() -> Unit)?
) {
    div("uk-modal-header") {
        if (withCloseButton) {
            button(type = ButtonType.button, classes = "uk-modal-close-default") {
                ariaLabel = "Close"
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

fun FlowContent.modalCloseButton(text: String = "Close") {
    button(type = ButtonType.button, classes = "uk-button uk-button-default uk-modal-close") {
        +text
    }
}