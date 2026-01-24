package com.zalphion.featurecontrol.web

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.HTMLTag
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import org.http4k.lens.BiDiMapping

interface TableElementSchema {
    val label: String
    val key: String?
    val required: Boolean
    val default: String?
    val headerClasses: String?

    fun render(flow: FlowContent) {
        renderInternal(flow) { tag ->
            tag.attributes["x-model"] = if (key == null) "element" else "element.${key}"
            tag.attributes["aria-label"] = label

            if (required) {
                tag.attributes["required"] = ""
            }
        }
    }

    fun renderInternal(flow: FlowContent, block: (HTMLTag) -> Unit)

    data class DynamicInput(
        override val label: String,
        override val key: String?,
        val typeExpression: String, // dynamic type for alpine.js (operating on an `element` arg)
        override val default: String? = null,
        override val required: Boolean = true,
        val placeholder: String? = null,
        override val headerClasses: String? = null,
    ): TableElementSchema {
        override fun renderInternal(flow: FlowContent, block: (HTMLTag) -> Unit) = flow.input {
            attributes[":type"] = typeExpression
            attributes[":class"] =  $$"['checkbox', 'radio'].includes($el.type) ? 'uk-checkbox' : 'uk-input'"
            this@DynamicInput.placeholder?.let {
                attributes["placeholder"] = it
            }
            block(this)
        }
    }

    data class Input(
        override val label: String,
        override val key: String?,
        val type: InputType,
        override val default: String? = null,
        override val required: Boolean = true,
        val placeholder: String? = null,
        override val headerClasses: String? = null,
    ): TableElementSchema {
        override fun renderInternal(flow: FlowContent, block: (HTMLTag) -> Unit) = flow.input(type) {
            classes += when (type) {
                InputType.radio, InputType.checkBox -> "uk-checkbox"
                else -> "uk-input"
            }
            this@Input.placeholder?.let {
                attributes["placeholder"] = it
            }
            block(this)
        }
    }

    data class Select(
        override val label: String,
        override val key: String?,
        val options: List<String>,
        override val default: String? = null,
        override val required: Boolean = true,
        override val headerClasses: String? = null
    ): TableElementSchema {
        override fun renderInternal(flow: FlowContent, block: (HTMLTag) -> Unit) = flow.select("uk-select") {
            block(this)

            if (default != null) {
                option("uk-text-muted") {
                    disabled = true
                    selected = true
                    +"-$default-"
                }
            }

            for (option in options) {
                option {
                    selected = option == default
                    +option
                }
            }
        }
    }
}

fun <Element: Any> FlowContent.tableForm(
    inputName: String,
    rowAriaLabel: String,
    schema: List<TableElementSchema>,
    elements: List<Element>,
    mapper: BiDiMapping<String, List<Element>>,
    debug: Boolean = false
) = div {
    attributes["x-data"] = """{
        $inputName: ${mapper(elements)}
    }"""

    input(InputType.hidden, name = inputName) {
        attributes[":value"] = "JSON.stringify($inputName)"
    }

    if (debug) {
        p {
            attributes["x-text"] = $$"JSON.stringify($data, null, 2)"
        }
    }

    table("uk-table uk-table-middle") {
        thead {
            tr {
                for (schemaElement in schema) {
                    th(classes = schemaElement.headerClasses) {
                        +schemaElement.label
                    }
                }
            }
        }

        tbody {
            template {
                attributes["x-for"] = "(element, index) in $inputName"
                attributes[":key"] = "index"

                tr {
                    for (schemaElement in schema) {
                        td {
                            schemaElement.render(this)
                        }
                    }

                    td { // remove button
                        button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
                            attributes["aria-label"] = "Remove $rowAriaLabel"
                            attributes["uk-icon"] = "trash"
                            onClick("$inputName.splice(index, 1)")
                        }
                    }
                }
            }
        }

        tfoot {
            tr {
                td { // New Row
                    button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
                        // calculate what to push to new rows, because the defaults in the inputs don't update the alpine state
                        val initialElement = schema
                            .filter { it.key != null && it.default != null }
                            .joinToString(", ") { "${it.key} : '${it.default}'" }
                            .let { "{$it}" }

                        attributes["uk-icon"] = "plus"
                        attributes["uk-tooltip"] = "Add $rowAriaLabel"
                        attributes["aria-label"] = "Add $rowAriaLabel"
                        onClick("$inputName.push($initialElement)")
                    }
                }
            }
        }
    }
}