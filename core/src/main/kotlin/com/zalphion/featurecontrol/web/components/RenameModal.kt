package com.zalphion.featurecontrol.web.components

import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.withRichMethod
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.span
import org.http4k.core.Method
import org.http4k.core.Uri
import java.util.UUID

fun FlowContent.renameModal(
    resourceName: String,
    action: Uri,
    inputName: String = "name",
    method: Method = Method.PUT
): String {
    val modalId = "rename-${UUID.randomUUID()}"

    div("uk-modal uk-modal-container") {
        id = modalId

        div("uk-modal-dialog") {

            div("uk-modal-header") {
                button(type = ButtonType.button, classes = "uk-modal-close-default") {
                    attributes["uk-close"] = ""
                }

                h2("uk-modal-title") {
                    span("uk-text-danger uk-margin-small-right") {
                        attributes["uk-icon"] = "icon: warning; ratio: 2;"
                    }
                    +"Rename $resourceName?"
                }
            }

            form(action.toString(), method = FormMethod.post) {
                withRichMethod(method)


                div("uk-modal-body") {
                    input(InputType.text, classes = "uk-input") {
                        name = inputName
                        ariaLabel = "Name"
                        placeholder = resourceName
                    }
                }

                div("uk-modal-footer") {
                    button(type = ButtonType.submit, classes = "uk-button uk-button-primary") {
                        +"Rename"
                    }
                    button(type = ButtonType.button, classes = "uk-button uk-button-default uk-modal-close") {
                        +"Cancel"
                    }
                }
            }
        }
    }

    return modalId
}