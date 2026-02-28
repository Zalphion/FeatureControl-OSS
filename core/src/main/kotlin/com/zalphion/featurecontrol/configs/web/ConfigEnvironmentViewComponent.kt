package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.configs.PropertyType
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.components.modalTextButton
import com.zalphion.featurecontrol.web.cssStyle
import com.zalphion.featurecontrol.web.tooltip
import kotlinx.html.FlowContent
import kotlinx.html.TD
import kotlinx.html.h5
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

class ConfigEnvironmentViewComponent(
    val application: Application,
    val spec: ConfigSpec,
    val environment: ConfigEnvironment
) {
    companion object {
        fun core(
            beforeContent: FlowContent.(Core, ConfigEnvironmentViewComponent) -> Unit = { _, _, _ -> },
            afterContent: FlowContent.(Core, ConfigEnvironmentViewComponent) -> Unit = { _, _, _ -> }
        ) = Component<ConfigEnvironmentViewComponent> { flow, core, data ->
            core.render(flow, ConfigNavBarComponent(data.application, data.environment))

            flow.beforeContent(core, data)

            flow.configTable(data.spec, data.environment) { _, type, value ->
                when(type) {
                    PropertyType.String -> span { +value.orEmpty() }
                    PropertyType.Number -> span { +value.orEmpty() }
                    PropertyType.Secret -> if (value.orEmpty().isNotEmpty()) {
                        span("uk-text-muted") { +"********" }
                    }
                    PropertyType.Boolean -> when(value?.toBoolean()) {
                        true -> span("uk-text-success") { +"TRUE" }
                        false -> span("uk-text-danger") { +"FALSE" }
                        null -> {}
                    }
                }
            }

            val modalId = "update-${data.environment.appId}-${data.environment.name}-modal"
            core.render(flow, ConfigEnvironmentEditModalComponent(
                application = data.application,
                environment = data.environment,
                spec = data.spec,
                modalId = modalId
            ))

            flow.modalTextButton(
                label = "Update",
                classes = "uk-button uk-button-primary",
                modalId = modalId
            )

            flow.afterContent(core, data)
        }
    }
}

internal fun FlowContent.configTable(
    spec: ConfigSpec,
    environment: ConfigEnvironment,
    renderValue: TD.(key: PropertyKey, type: PropertyType, value: String?) -> Unit
) {
    table("uk-table uk-table-middle") {
        thead {
            tr {
                th(classes = "uk-width-medium") { +"Key" }
                th(classes = "uk-table-expand") { +"Value" }
            }
        }

        tbody {
            for ((key, spec) in spec.properties.entries.sortedBy { it.key }) {
                val value = environment.values[key]

                tr {
                    td {
                        ariaLabel = "Key"
                        h5 {
                            // set inline so the tooltip icon renders inline and not below
                            style = cssStyle("display" to "inline-block")
                            +key.value
                        }
                        if (spec.description.isNotBlank()) {
                            span("uk-margin-small-left") {
                                style = "color: #03a9fc"
                                attributes["uk-icon"] = "icon: info"
                                tooltip = spec.description
                            }
                        }
                    }
                    td {
                        ariaLabel = "Value"
                        renderValue(key, spec.type, value)
                    }
                }
            }
        }
    }
}