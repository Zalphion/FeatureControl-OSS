package com.zalphion.featurecontrol.web

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.onClick
import org.http4k.core.Uri

fun FlowContent.confirmCancelButtons(confirmText: String) {
    button(type = ButtonType.submit, classes = "uk-button uk-button-primary") {
        +confirmText
    }
    button(type = ButtonType.button, classes = "uk-button uk-button-default uk-modal-close") {
        +"Cancel"
    }
}

fun FlowContent.updateResetButtons(confirmText: String = "Update") {
    button(type = ButtonType.submit, classes = "uk-button uk-button-primary") {
        +confirmText
    }
    button(type = ButtonType.button, classes = "uk-button uk-button-danger") {
        onClick = "window.location.reload()"
        +"Reset"
    }
}