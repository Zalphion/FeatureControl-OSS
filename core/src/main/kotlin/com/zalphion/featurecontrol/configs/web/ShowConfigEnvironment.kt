package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.configs.PropertyType
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.TD
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h5
import kotlinx.html.input
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlin.collections.component1
import kotlin.collections.component2

class ConfigEnvironmentComponent(
    val application: Application,
    val spec: ConfigSpec,
    val environment: ConfigEnvironment
) {
    companion object {
        fun core(
            extraContent: FlowContent.(ConfigEnvironmentComponent) -> Unit = {}
        ) = Component<ConfigEnvironmentComponent> { flow, core, data ->
            val environment = data.environment
            val spec = data.spec

            core.render(flow, ConfigNavBarComponent(data.application, data.environment))

            val dto = spec.properties.mapValues { (key, spec) ->
                val rawValue = environment.values[key]
                when(spec.type) {
                    PropertyType.Secret -> if (rawValue == null) null else "********"
                    else -> rawValue
                }
            }

            flow.form(environment.uri().toString(), method = FormMethod.post) {
                // need to use the keys from the spec, because the environment may not have all the keys filled
                attributes["x-data"] = """{
                    values: ${core.json.asFormatString(dto)}
                }""".trimIndent()

                input(InputType.hidden, name = "values") {
                    attributes[":value"] = "JSON.stringify(values)"
                }

                table("uk-table uk-table-middle") {
                    thead {
                        tr {
                            th(classes = "uk-width-medium") { +"Key" }
                            th(classes = "uk-table-expand") { +"Value" }
                        }
                    }

                    tbody {
                        for ((key, spec) in spec.properties.entries.sortedBy { it.key }) {
                            tr {
                                td {
                                    h5 { +key.value }
                                    if (spec.description.isNotBlank()) {
                                        span("uk-margin-small-left") {
                                            style = "color: #03a9fc"
                                            attributes["uk-icon"] = "icon: info"
                                            attributes["uk-tooltip"] = spec.description
                                        }
                                    }
                                }
                                td { valueInput(key, spec.type) }
                            }
                        }
                    }
                }

                extraContent(data)

                div("uk-padding-small") {
                    updateResetButtons("Update", environment.uri())
                }
            }
        }
    }
}

private fun TD.valueInput(key: PropertyKey, type: PropertyType) {
    fun INPUT.configure() {
        attributes["x-model"] = "values['$key']"
        ariaLabel = "Value"
        placeholder = "Value"
    }

    when(type) {
        PropertyType.Number -> input(InputType.number, classes = "uk-input") {
            configure()
        }
        PropertyType.Boolean -> select("uk-select") {
            attributes["x-model"] = "values['$key']"
            ariaLabel = "Value"
            option { }
            option {
                value = "true"
                +"True"
            }
            option {
                value = "false"
                +"False"
            }
        }
        PropertyType.String -> input(InputType.text, classes = "uk-input") {
            configure()
        }
        PropertyType.Secret -> input(InputType.password, classes = "uk-input") {
            configure()
        }
    }
}