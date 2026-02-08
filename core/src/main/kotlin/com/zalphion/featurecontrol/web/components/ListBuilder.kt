package com.zalphion.featurecontrol.web.components

import com.zalphion.featurecontrol.web.AlpineEvent
import com.zalphion.featurecontrol.web.ariaHidden
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.li
import com.zalphion.featurecontrol.web.onClick
import com.zalphion.featurecontrol.web.onEnter
import com.zalphion.featurecontrol.web.onSpace
import com.zalphion.featurecontrol.web.onTab
import com.zalphion.featurecontrol.web.template
import com.zalphion.featurecontrol.web.xData
import com.zalphion.featurecontrol.web.xModel
import com.zalphion.featurecontrol.web.xText
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
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
    body = {
        // only render the control if the dispatch has completed
        template {
            attributes["x-if"] = "modalData"

            div { // the template needs a single root element
                listBuilder(label, "modalData", placeholderText)
            }
        }
    },
    footer = null
)

fun FlowContent.listBuilder(
    label: String,
    alpineKey: String,
    placeholderText: String?
) {
    ul("uk-list overflow-y-auto") {
        ariaLabel = "$label List"

        template {
            attributes["x-for"] = "(tag, index) in $alpineKey"
            attributes[":key"] = "tag + index"

            li("flex items-center gap-2") {
                span {
                    xText = "tag"
                }
                button(type = ButtonType.button) {
                    onClick("$alpineKey.splice(index, 1)")
                    ariaLabel = "Remove $label"
                    attributes["uk-icon"] = "icon: close"
                }
            }
        }

        // fallback for no elements
        template {
            attributes["x-if"] = "$alpineKey.length === 0"

            li("uk-text-muted") {
                ariaHidden = true // don't appear as a valid aria LIST_ITEM in tests
                +"No $label items..."
            }
        }
    }

    input(type = InputType.text, classes = "uk-input") {
        xData = "{ temp: '' }"
        xModel = "temp"
        ariaLabel = "Add $label"
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