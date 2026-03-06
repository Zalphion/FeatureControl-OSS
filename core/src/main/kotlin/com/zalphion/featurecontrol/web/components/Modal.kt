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
) = modal {
    this.modalId = modalId
    this.header = header
    this.body = body
    this.footer = footer
    this.withCloseButton = withCloseButton

    context = {
        if (event != null) {
            attributes["@${event.eventId}.window"] = $$"$${event.dataKey} = $event.detail"
            attributes["x-data"] = "{ ${event.dataKey}: null }"
        }
    }

    contentWrapper = { block ->
        if (form != null) {
            form(method = FormMethod.post, action = form.second.toString()) {
                withRichMethod(form.first)
                block()
            }
        } else block()
    }
}

fun FlowContent.modalCloseButton(text: String = "Close") {
    button(type = ButtonType.button, classes = "uk-button uk-button-default uk-modal-close") {
        +text
    }
}

fun FlowContent.modal(builder: ModalBuilder.() -> Unit) = ModalBuilder()
    .apply(builder)
    .build(this)

class ModalBuilder {
    var modalId: String? = null
    var form: Pair<Method, Uri>? = null
    var header: (H2.() -> Unit)? = null
    var context: DIV.() -> Unit = {}
    var contentWrapper: DIV.(FlowContent.() -> Unit) -> Unit = { it() }
    var body: (DIV.() -> Unit)? = null
    var footer: (DIV.() -> Unit)? = null
    var withCloseButton: Boolean = true

    fun build(flow: FlowContent) = flow.div("uk-modal uk-modal-container") {
        modalId?.let { id = it }

        context()

        div("uk-modal-dialog") {
            attributes["x-cloak"] = ""

            div("uk-modal-header") {
                if (withCloseButton) {
                    button(type = ButtonType.button, classes = "uk-modal-close-default") {
                        ariaLabel = "Close"
                        attributes["uk-close"] = ""
                    }
                }

                header?.let {
                    h2("uk-modal-title", block = it)
                }
            }


            contentWrapper {
                body?.let {
                    div("uk-modal-body", block = it)
                }

                footer?.let {
                    div("uk-modal-footer", block = it)
                }
            }
        }
    }
}