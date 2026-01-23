package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.web.onClick
import com.zalphion.featurecontrol.web.template
import com.zalphion.featurecontrol.web.tr
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.TD
import kotlinx.html.a
import kotlinx.html.form
import kotlinx.html.input
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr

fun FlowContent.renderConfigSpec(core: Core, config: ConfigSpec) = form(
    action = config.uri().toString(),
    method = FormMethod.post,
) {
    attributes["x-data"] = """{
        properties: ${config.properties.map { it.value.toDto(it.key) }.let(core.json::asFormatString)} // Array<ConfigPropertyDto>
    }"""

    input(InputType.hidden, name = "properties") {
        attributes[":value"] = "JSON.stringify(properties)"
    }

    table("uk-table uk-table-middle") {
        thead {
            tr {
                th(classes = "uk-width-medium") { +"Key" }
                th(classes = "uk-width-small") { +"Type" }
                th(classes = "uk-table-shrink") { +"Nullable" }
                th(classes = "uk-width-medium") { +"Default Value"}
                th(classes = "uk-width-large") { +"Description" }
                th(classes = "uk-width-small")
            }
        }

        tbody {
            template {
                attributes["x-for"] = "(property, index) in properties"
                attributes[":key"] = "index"

                tr {
                    td { // property name
                        input(InputType.text, classes = "uk-input") {
                            attributes["x-model"] = "property.key"
                            placeholder = "Key"
                            required = true
                        }
                    }

                    td {
                        // type
                        select("uk-select") {
                            attributes["x-model"] = "property.type"
                            for (type in PropertyTypeDto.entries) {
                                option {
                                    value = type.toString()
                                    +type.toString()
                                }
                            }
                        }
                    }

                    td { // nullable
                        input(InputType.checkBox, classes = "uk-checkbox") {
                            attributes["x-model"] = "property.nullable"
                        }
                    }

                    td { // default value
                        valueInput()
                    }

                    td { // description
                        input(InputType.text, classes = "uk-input") {
                            attributes["x-model"] = "property.description"
                            placeholder = "Description"
                        }
                    }

                    td { // remove button
                        a("#", classes = "uk-icon-button") {
                            attributes["uk-icon"] = "trash"
                            onClick("properties.splice(index, 1)")
                        }
                    }
                }
            }
        }

        tfoot {
            tr {
                td { // New Row
                    a("#", classes = "uk-icon-button") {
                        attributes["uk-icon"] = "plus"
                        onClick("properties.push({'type':'${PropertyTypeDto.String}'})")
                    }
                }
            }
        }
    }

    updateResetButtons("Update", config.uri())
}

private fun TD.valueInput() {
    template {
        attributes["x-if"] = "property.type == '${PropertyTypeDto.String}'"
        input(InputType.text, classes = "uk-input") {
            attributes["x-model"] = "property.default"
            placeholder = "Default"
        }
    }

    template {
        attributes["x-if"] = "property.type == '${PropertyTypeDto.Number}'"
        input(InputType.number, classes = "uk-input") {
            attributes["x-model"] = "property.default"
            placeholder = "Default"
        }
    }

    template {
        attributes["x-if"] = "property.type == '${PropertyTypeDto.Boolean}'"
        input(InputType.checkBox, classes = "uk-checkbox") {
            attributes["x-model"] = "property.default"
            placeholder = "Default"
        }
    }

    template {
        attributes["x-if"] = "property.type == '${PropertyTypeDto.Secret}'"
        input(InputType.password, classes = "uk-input") {
            attributes["x-model"] = "property.default"
            placeholder = "Default"
        }
    }
}