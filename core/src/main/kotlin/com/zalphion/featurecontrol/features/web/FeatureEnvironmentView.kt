package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.FeatureEnvironment
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

fun FlowContent.coreFeatureEnvironment(
    core: Core,
    feature: Feature,
    environment: FeatureEnvironment,
    environmentName: EnvironmentName,
    extraInputs: FlowContent.() -> Unit = {},
    extraTableSchema: List<TableElementSchema> = emptyList()
) {
    form(method = FormMethod.post) {
        extraInputs()

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
            elements = feature.variants.keys.map { variant -> environment.toDto(variant) },
            mapper = core.json.asBiDiMapping(),
        )

        updateResetButtons("Update", feature.uri(environmentName))
    }
}