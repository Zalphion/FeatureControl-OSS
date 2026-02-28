package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.configs.PropertyType
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.components.modal
import com.zalphion.featurecontrol.web.confirmCancelButtons
import com.zalphion.featurecontrol.web.uri
import com.zalphion.featurecontrol.web.xData
import com.zalphion.featurecontrol.web.xModel
import kotlinx.html.FlowContent
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.TD
import kotlinx.html.input
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.span
import org.http4k.core.Method
import org.http4k.core.Uri
import kotlin.collections.component1
import kotlin.collections.component2

class ConfigEnvironmentEditModalComponent(
    val application: Application,
    val spec: ConfigSpec,
    val environment: ConfigEnvironment,
    val modalId: String
) {
    companion object {
        fun core(
            extraInputs: FlowContent.(Core, ConfigEnvironmentEditModalComponent) -> Unit = { _, _ -> },
            uriFn: (ConfigEnvironment) -> Uri = { it.uri()}
        ) = Component<ConfigEnvironmentEditModalComponent> { flow, core, data ->
            val dto = data.spec.properties.mapValues { (key, spec) ->
                val rawValue = data.environment.values[key]
                when (spec.type) {
                    PropertyType.Secret -> if (rawValue == null) null else "********"
                    else -> rawValue
                }
            }

            flow.modal(
                modalId = data.modalId,
                header = {
                    span {
                        attributes["uk-icon"] = "${PageSpec.config.icon}; ratio: 2"
                    }
                    +"Update ${data.application.appName} (${data.environment.name}) Config"
                },
                form = Method.POST to uriFn(data.environment),
                body = {
                    xData = """{
                    values: ${core.json.asFormatString(dto)}
                }""".trimIndent()

                    input(InputType.hidden, name = "values") {
                        attributes[":value"] = "JSON.stringify(values)"
                    }

                    configTable(data.spec, data.environment) { key, type, _ ->
                        valueInput(key, type)
                    }

                    extraInputs(core, data)
                },
                footer = {
                    confirmCancelButtons("Update")
                }
            )
        }
    }
}

private fun TD.valueInput(key: PropertyKey, type: PropertyType) {
    fun INPUT.configure() {
        xModel = "values['$key']"
        ariaLabel = "Value"
    }

    when(type) {
        PropertyType.Number -> input(InputType.number, classes = "uk-input") {
            configure()
        }
        PropertyType.Boolean -> select("uk-select") {
            xModel = "values['$key']"
            ariaLabel = "Value"
            option { } // need a placeholder for the default to be selected properly
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