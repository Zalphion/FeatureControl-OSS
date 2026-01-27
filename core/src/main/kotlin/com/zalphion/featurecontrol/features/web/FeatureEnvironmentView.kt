package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.TableElementSchema
import com.zalphion.featurecontrol.web.tableForm
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.form

class FeatureEnvironmentComponent(
    val application: Application,
    val feature: Feature,
    val environmentName: EnvironmentName,
    val environment: FeatureEnvironment
) {
    companion object {
        fun core(
            core: Core,
            extraInputs: FlowContent.(FeatureEnvironmentComponent) -> Unit = {},
            extraTableSchema: List<TableElementSchema> = emptyList()
        ) = Component<FeatureEnvironmentComponent> { flow, data ->
            flow.form(method = FormMethod.post) {
                extraInputs(data)

                tableForm(
                    inputName = "variants",
                    rowAriaLabel = null,
                    schema = listOf(
                        TableElementSchema.Static(
                            label = "Variant",
                            key = "name",
                            headerClasses = "uk-width-small"
                        ),
                        TableElementSchema.Input(
                            label = "Weight",
                            key = "weight",
                            type = InputType.number,
                            required = false,
                            placeholder = "e.g. 50",
                            headerClasses = "uk-width-small",
                        ),
                        TableElementSchema.Tags(
                            label = "Subject IDs",
                            key = "subjectIds",
                            headerClasses = "uk-width-medium"
                        )
                    ) + extraTableSchema,
                    elements = data.feature.variants.keys.map { variant -> data.environment.toDto(variant) },
                    mapper = core.json.asBiDiMapping(),
                )

                updateResetButtons("Update", data.feature.uri(data.environmentName))
            }
        }
    }
}