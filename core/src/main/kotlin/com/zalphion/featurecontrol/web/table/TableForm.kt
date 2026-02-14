package com.zalphion.featurecontrol.web.table

import com.zalphion.featurecontrol.web.ariaHidden
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.cssStyle
import com.zalphion.featurecontrol.web.onClick
import com.zalphion.featurecontrol.web.template
import com.zalphion.featurecontrol.web.tooltip
import com.zalphion.featurecontrol.web.tr
import com.zalphion.featurecontrol.web.xData
import com.zalphion.featurecontrol.web.xText
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import org.http4k.lens.BiDiMapping
import kotlin.collections.map

private const val ELEMENT_KEY = "element"
private const val INDEX_KEY = "index"

fun <Element: Any> FlowContent.tableForm(
    inputName: String,
    rowAriaLabel: String?, // null to disable add/remove rows
    schema: List<TableElementSchema>,
    elements: List<Element>,
    mapper: BiDiMapping<String, List<Element>>,
    debug: Boolean = false
) = div {
    xData = listOf(
        "$inputName:${mapper(elements)}",
        *schema.flatMap { it.extraXData("this.$inputName", INDEX_KEY) }.toTypedArray()
    ).joinToString(",").let { "{$it}" }

    input(InputType.hidden, name = inputName) {
        attributes[":value"] = "JSON.stringify($inputName)"
    }

    if (debug) {
        p {
            xText = $$"JSON.stringify($data, null, 2)"
        }
    }

    table("uk-table uk-table-middle") {
        style = "width: auto;"

        thead {
            tr {
                for (schemaElement in schema) {
                    th(classes = schemaElement.headerClasses) {
                        style = cssStyle(*schemaElement.headerStyles.map { it.key to it.value }.toTypedArray())
                        +schemaElement.label
                    }
                }
            }
        }

        tbody {
            template {
                attributes["x-for"] = "($ELEMENT_KEY, $INDEX_KEY) in $inputName"
                attributes[":key"] = INDEX_KEY

                tr {
                    for (schemaElement in schema) {
                        td {
                            schemaElement.render(this, ELEMENT_KEY)
                        }
                    }

                    if (rowAriaLabel != null) {
                        td { // remove button
                            button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
                                ariaLabel = "Remove"
                                tooltip = "Remove $rowAriaLabel"
                                attributes["uk-icon"] = "trash"
                                onClick("$inputName.splice(index, 1)")
                            }
                        }
                    }
                    if (debug) {
                        td {
                            p {
                                xText = "JSON.stringify($ELEMENT_KEY, null, 2)"
                            }
                        }
                    }
                }
            }
        }

        if (rowAriaLabel != null) {
            tfoot {
                tr {
                    td { // New Row
                        button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
                            // calculate what to push to new rows, because the defaults in the inputs don't update the alpine state
                            val initialElement = schema
                                .filter { it.defaultJson != null }
                                .joinToString(", ") { "${it.key} : ${it.defaultJson}" }
                                .let { "{$it}" }

                            attributes["uk-icon"] = "plus"
                            tooltip = "Add $rowAriaLabel"
                            ariaLabel = "Add"
                            onClick("$inputName.push($initialElement)")
                        }
                    }
                }
            }
        }
    }
}