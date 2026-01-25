package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.web.TableElementSchema
import com.zalphion.featurecontrol.web.tableForm
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.form

fun FlowContent.renderConfigSpec(core: Core, config: ConfigSpec) = form(
    action = config.uri().toString(),
    method = FormMethod.post,
) {
    tableForm(
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
        elements = config.properties.map { it.value.toDto(it.key) },
        mapper = core.json.asBiDiMapping()
    )

    updateResetButtons("Update", config.uri())
}