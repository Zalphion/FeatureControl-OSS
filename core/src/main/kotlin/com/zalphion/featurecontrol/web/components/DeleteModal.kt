package com.zalphion.featurecontrol.web.components

import com.zalphion.featurecontrol.web.withRichMethod
import kotlinx.html.ButtonType
import kotlinx.html.FORM
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.button
import kotlinx.html.form
import kotlinx.html.p
import kotlinx.html.span
import org.http4k.core.Method
import org.http4k.core.Uri
import java.util.UUID

fun FlowContent.deleteModal(
    resourceName: String,
    action: Uri,
    formContent: FORM.() -> Unit = {}
): String {
    val modalId = "delete-${UUID.randomUUID()}"
    modal(
        modalId = modalId,
        header = {
            span("uk-text-danger uk-margin-small-right") {
                attributes["uk-icon"] = "icon: warning; ratio: 2;"
            }
            +"Delete $resourceName?"
        },
        body = {
            p {
                +"Are you sure you want to delete $resourceName?"
            }
        },
        footer = {
            form(action.toString(), method = FormMethod.post) {
                withRichMethod(Method.DELETE)
                formContent()

                button(type = ButtonType.submit, classes = "uk-button uk-button-danger") {
                    +"Delete"
                }

                modalCloseButton("Cancel")
            }
        }
    )

    return modalId
}