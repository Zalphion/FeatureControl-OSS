package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.TableElementSchema
import com.zalphion.featurecontrol.web.tableForm
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.InputType

class ConfigSpecComponent(val application: Application, val spec: ConfigSpec) {
    companion object {
        fun core(core: Core) = Component<ConfigSpecComponent> { flow, data ->
            flow.coreConfigNavBar(data.application, null)
            flow.tableForm(
                inputName = "properties",
                rowAriaLabel = "Property",
                schema = listOf(
                    TableElementSchema.Input(
                        label = "Key",
                        type = InputType.text,
                        key = "key",
                        placeholder = "Key",
                        headerClasses = "uk-width-medium",
                        required = true
                    ),
                    TableElementSchema.Select(
                        label = "Type",
                        key = "type",
                        required = true,
                        headerClasses = "uk-width-small",
                        options = PropertyTypeDto.entries.map { it.toString() },
                        default = PropertyTypeDto.String.toString()
                    ),
//            TableElementSchema.DynamicInput(
//                label = "Default", key = "default", required = false,
//                typeExpression = """
//                    element.type === '${PropertyTypeDto.Boolean}' ? 'checkbox' :
//                    (element.type === '${PropertyTypeDto.Number}' ? 'number' :
//                    (element.type === '${PropertyTypeDto.Secret}' ? 'password' :
//                    'text'))
//                """.trimIndent(),
//                headerClasses = "uk-width-medium"
//            ),
                    TableElementSchema.Input(
                        label = "Description",
                        type = InputType.text,
                        key = "description",
                        placeholder = "Description",
                        required = false,
                        headerClasses = "uk-width-large"
                    ),
                ),
                elements = data.spec.properties.map { it.value.toDto(it.key) },
                mapper = core.json.asBiDiMapping()
            )

            flow.updateResetButtons("Update", data.spec.uri())
        }
    }
}