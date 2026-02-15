package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.FeatureControl
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.table.InputTableElementSchema
import com.zalphion.featurecontrol.web.table.SelectTableElementSchema
import com.zalphion.featurecontrol.web.table.tableForm
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.form

class ConfigSpecComponent(val application: Application, val spec: ConfigSpec) {
    companion object {
        fun core(app: FeatureControl) = Component<ConfigSpecComponent> { flow, data ->
            app.render(flow, ConfigNavBarComponent(data.application, null))

            flow.form(method = FormMethod.post, action = data.spec.uri().toString()) {
                tableForm(
                    inputName = "properties",
                    rowAriaLabel = "Property",
                    schema = listOf(
                        InputTableElementSchema(
                            label = "Key",
                            type = InputType.text,
                            key = "key",
                            placeholder = "Key",
                            headerClasses = "uk-width-medium",
                            required = true
                        ),
                        SelectTableElementSchema(
                            label = "Type",
                            key = "type",
                            isRequired = true,
                            headerClasses = "uk-width-small",
                            options = PropertyTypeDto.entries.map { it.toString() },
                            default = PropertyTypeDto.String.toString()
                        ),
                        InputTableElementSchema(
                            label = "Description",
                            type = InputType.text,
                            key = "description",
                            placeholder = "Description",
                            required = false,
                            headerClasses = "uk-width-large"
                        ),
                    ),
                    elements = data.spec.properties.map { it.value.toDto(it.key) },
                    mapper = app.core.json.asBiDiMapping()
                )

                updateResetButtons("Update", data.spec.uri())
            }
        }
    }
}