package com.zalphion.featurecontrol.web

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.input
import kotlinx.html.span
import kotlinx.html.ul

fun FlowContent.listBuilderModal(
    label: String,
    modalId: String,
    eventId: String?,
    placeholderText: String?
) = modal(
    modalId = modalId,
    header = { +label },
    event = if (eventId == null) null else AlpineEvent(eventId, "modalData"),
    body = { listBuilder(label, "modalData", placeholderText) },
    footer = null
)

fun FlowContent.listBuilder(
    label: String,
    alpineKey: String,
    placeholderText: String?
) {
    ul("uk-list overflow-y-auto") {
        attributes["aria-label"] = "$label List"

        template {
            attributes["x-for"] = "(tag, index) in $alpineKey"
            attributes[":key"] = "tag + index"

            li("flex items-center gap-2") {
                span {
                    attributes["x-text"] = "tag"
                }
                button(type = ButtonType.button) {
                    onClick("$alpineKey.splice(index, 1)")
                    attributes["aria-label"] = "Remove $label"
                    attributes["uk-icon"] = "icon: close"
                }
            }
        }

        // fallback for no elements
        template {
            attributes["x-if"] = "$alpineKey.length === 0"

            li("uk-text-muted") {
                attributes["aria-hidden"] = "true" // don't appear as a valid aria LIST_ITEM in tests
                +"No $label items..."
            }
        }
    }

    input(type = InputType.text, classes = "uk-input") {
        attributes["x-data"] = "{ temp: '' }"
        attributes["x-model"] = "temp"
        attributes["aria-label"] = "Add $label"
        if (placeholderText != null) {
            placeholder = placeholderText
        }

        val submitElement = """
                if (temp.trim()) {
                    $alpineKey.push(temp.trim())
                    temp = ''
                }
            """.trimIndent()

        onEnter(submitElement)
        onTab(submitElement)
        onSpace(submitElement)
    }
}