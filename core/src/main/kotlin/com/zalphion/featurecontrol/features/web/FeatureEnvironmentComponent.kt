package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.mapToList
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.table.TableElementSchema
import com.zalphion.featurecontrol.web.components.listBuilderModal
import com.zalphion.featurecontrol.web.table.InputTableElementSchema
import com.zalphion.featurecontrol.web.table.ModalTableElementSchema
import com.zalphion.featurecontrol.web.table.StaticTableElementSchema
import com.zalphion.featurecontrol.web.table.tableForm
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.form
import org.http4k.lens.BiDiMapping

class FeatureEnvironmentComponent(
    val application: Application,
    val feature: Feature,
    val environmentName: EnvironmentName,
    val environment: FeatureEnvironment
) {
    companion object {
        internal fun core(core: Core) = create(
            jsonMapper = core.json.asBiDiMapping<Array<CoreVariantEnvironmentDto>>().mapToList(),
            dtoMapper = { env, variant ->  env.toCoreDto(variant) }
        )

        fun <DTO: VariantEnvironmentDto> create(
            jsonMapper: BiDiMapping<String, List<DTO>>,
            dtoMapper: (FeatureEnvironment, Variant) -> DTO,
            extraInputs: FlowContent.(FeatureEnvironmentComponent) -> Unit = {},
            extraTableSchema: (FeatureEnvironmentComponent) -> List<TableElementSchema> = { emptyList() },
        ) = Component<FeatureEnvironmentComponent> { flow, data ->
            flow.form(method = FormMethod.post) {
                extraInputs(data)

                val subjectIdsModalId = "subject-ids-modal"
                val subjectIdsEventId = "subject-ids-event"
                listBuilderModal(
                    label = "Subject ID",
                    modalId = subjectIdsModalId,
                    eventId = subjectIdsEventId,
                    placeholderText = "Add Subject ID",
                )

                tableForm(
                    inputName = "variants",
                    rowAriaLabel = null,
                    schema = listOf(
                        StaticTableElementSchema(
                            label = "Variant",
                            key = "name",
                            headerClasses = "uk-width-small"
                        ),
                        InputTableElementSchema(
                            label = "Weight",
                            key = "weight",
                            type = InputType.number,
                            required = false,
                            placeholder = "e.g. 50",
                            headerClasses = "uk-table-shrink",
                            headerStyles = mapOf("min-width" to "100px")
                        ),
                        ModalTableElementSchema(
                            label = "Subjects",
                            labelExpression = { currentRef -> $$"`Subjects (${$$currentRef.subjectIds.length})`" },
                            key = "subjectIds",
                            headerClasses = "uk-table-shrink",
                            modalId = subjectIdsModalId,
                            dispatchEventId = subjectIdsEventId
                        )
                    ) + extraTableSchema(data),
                    elements = data.feature.variants.keys.map { variant -> dtoMapper(data.environment, variant) },
                    mapper = jsonMapper
                )

                updateResetButtons("Update", data.feature.uri(data.environmentName))
            }
        }
    }
}