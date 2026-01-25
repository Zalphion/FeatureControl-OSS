package com.zalphion.featurecontrol.web

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.ul

fun FlowContent.tagBuilder(
    key: String,
    prompt: String? = null
) {
    val submitElement = """
        if (temp.trim() !== '' && !$key.includes(temp.trim())) {
            $key.push(temp.trim());
            temp = '';
        }
    """.trimIndent()
    div {
        attributes["x-data"] = "{temp: ''}"

        // tag builder
        div {
            input(InputType.text, classes = "uk-input uk-form-width-medium") {
                if (prompt != null) placeholder = prompt
                attributes["x-model"] = "temp"
                attributes[":class"] = "{'uk-form-danger': $key.includes(temp.trim())}"
                onEnter(submitElement)
                onTab(submitElement)
                onSpace(submitElement)
            }
            a("#", classes = "uk-icon-button") {
                attributes["uk-icon"] = "plus"
                onClick(submitElement)
            }
        }

        // Tag List
        ul {
            // remove ul styling while preserving the aria semantics of ul
            style = "list-style: none; padding: 0; display: flex; flex-wrap: wrap; gap: 0.5em"
            attributes["role"] = "status"
            attributes["aria-label"] = key

            template {
                attributes["x-for"] = "(tag, index) in $key"
                attributes[":key"] = "tag + index"

                li {
                    span("uk-label") {
                        style = "margin-right: 0.5em;"
                        span {
                            attributes["x-text"] = "tag"
                        }
                        button(type = ButtonType.button, classes = "uk-button-link") {
                            attributes["uk-icon"] = "close"
                            attributes["aria-label"] = "'Remove ' + tag"
                            onClick("$key.splice(index, 1)")
                        }
                    }
                }
            }
        }
    }
}