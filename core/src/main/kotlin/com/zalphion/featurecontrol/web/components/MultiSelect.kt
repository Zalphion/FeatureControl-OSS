package com.zalphion.featurecontrol.web.components

import com.zalphion.featurecontrol.web.AlpineEvent
import com.zalphion.featurecontrol.web.template
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.ul

fun FlowContent.multiSelectModal(
    label: String,
    options: Map<String, String>, // value to label
    modalId: String,
    eventId: String?,
    optionsKey: String,
    availableKey: String
) = modal(
    modalId = modalId,
    header = { +label },
    event = if (eventId == null) null else AlpineEvent(eventId, "modalData"),
    body = {
        // only render the multi-select if the dispatch has completed
        template {
            attributes["x-if"] = "modalData"

            div { // the template needs a single root element
                multiSelect(options, "modalData.$optionsKey", "modalData.$availableKey")
            }
        }
    },
    footer = null
)

fun FlowContent.multiSelect(
    options: Map<String, String>, // value to label
    targetArrayKey: String,
    availableValuesKey: String,
) = ul("uk-list") {
    for ((value, label) in options) {
        li {
            label {
                input(InputType.checkBox, classes = "uk-checkbox") {
                    attributes[":disabled"] = "!$availableValuesKey.includes('$value')"
                    attributes[":checked"] = "$targetArrayKey.includes('$value')"
                    attributes["@change"] = $$"""
                        if ($event.target.checked) {
                            if (!$$targetArrayKey.includes('$$value')) { $$targetArrayKey.push('$$value') }
                        } else {
                            const idx = $$targetArrayKey.indexOf('$$value')
                            if (idx > -1) $$targetArrayKey.splice(idx, 1)
                        }
                    """.trimIndent()
                }
                +label
            }
        }
    }
}