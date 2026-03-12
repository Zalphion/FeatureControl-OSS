package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.configs.PropertyType
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.uri
import com.zalphion.featurecontrol.web.xData
import com.zalphion.featurecontrol.web.xModel
import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.INPUT
import kotlinx.html.InputType
import kotlinx.html.TD
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlin.collections.component1
import kotlin.collections.component2

class ConfigEnvironmentEditComponent(
    val application: Application,
    val spec: ConfigSpec,
    val environment: ConfigEnvironment
) {
    companion object {
        fun core(
            extraInputsBefore: FlowContent.(Core, ConfigEnvironmentEditComponent) -> Unit = { _, _ -> },
            extraInputsAfter: FlowContent.(Core, ConfigEnvironmentEditComponent) -> Unit = { _, _ -> },
            updateButtonContent: BUTTON.(ConfigEnvironmentEditComponent) -> Unit = { _ -> +"Update" },
            formId: String = "config-environment-form"
        ) = Component<ConfigEnvironmentEditComponent> { flow, core, data ->
            val dto = data.spec.properties.mapValues { (key, spec) ->
                val rawValue = data.environment.values[key]
                when (spec.type) {
                    PropertyType.Secret -> if (rawValue == null) null else "********"
                    else -> rawValue
                }
            }

            flow.form(method = FormMethod.post) {
                id = formId
                xData = """{
                    values: ${core.json.asFormatString(dto)}
                }""".trimIndent()

                extraInputsBefore(core, data)

                input(InputType.hidden, name = "values") {
                    attributes[":value"] = "JSON.stringify(values)"
                }

                configTable(data.spec, data.environment) { key, type, _ ->
                    valueInput(key, type)
                }

                extraInputsAfter(core, data)

                p { // wrapped to add a margin
                    button(type = ButtonType.submit, classes = "uk-button uk-button-primary") {
                        updateButtonContent(data)
                    }
                    a(classes = "uk-button uk-button-danger", href = data.environment.uri().toString()) {
                        +"Cancel"
                    }
                }
            }
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