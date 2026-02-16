package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.web.confirmCancelButtons
import com.zalphion.featurecontrol.web.featuresUri
import com.zalphion.featurecontrol.web.onClick
import com.zalphion.featurecontrol.web.template
import com.zalphion.featurecontrol.web.tr
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.web.withRichMethod
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import org.http4k.core.Method
import se.ansman.kotshi.JsonSerializable
import java.util.UUID

class NewFeatureModalComponent(
    val application: Application,
    val modalId: String,
) {
    companion object {
        fun core(
            extraControls: FlowContent.() -> Unit = {}
        ) = Component<NewFeatureModalComponent> { flow, core, data ->
            flow.div("uk-modal uk-modal-container") {
                id = data.modalId

                div("uk-modal-dialog") {
                    form(
                        method = FormMethod.post,
                        action = data.application.featuresUri().toString(),
                        classes = "uk-form-stacked"
                    ) {
                        div("uk-modal-header") {
                            h2("uk-modal-title") { +"New Feature" }
                        }

                        div("uk-modal-body") {

                            button(type = ButtonType.button, classes = "uk-modal-close-default") {
                                attributes["uk-close"] = ""
                            }

                            // key
                            div("uk-margin") {
                                label("uk-form-label") {
                                    htmlFor = "featureKey"
                                    +"Key"
                                }
                                div("uk-form-controls") {
                                    input(InputType.text, name="featureKey", classes = "uk-input") {
                                        id = "featureKey"
                                        placeholder = "newUi, my-new-feature, etc."
                                        required = true
                                    }
                                }
                            }

                            editControls(
                                core = core,
                                idPrefix = "new-feature",
                                description = "",
                                variants = emptyMap(),
                                defaultVariant = null,
                                extraControls = extraControls
                            )
                        }

                        div("uk-modal-footer") {
                            confirmCancelButtons("Create")
                        }
                    }
                }
            }
        }
    }
}

class FeatureComponent(val application: Application, val feature: Feature) {
    companion object {
        fun core(
            extraControls: FlowContent.(FeatureComponent, Core) -> Unit = { _, _ -> }
        ) = Component<FeatureComponent> { flow, core, data ->
            val feature = data.feature
            flow.div {
                attributes["aria-label"] = "Edit Feature"
                form(method = FormMethod.post) {
                    withRichMethod(Method.PUT)
                    editControls(
                        idPrefix = "feature-${feature.key}",
                        core = core,
                        description = feature.description,
                        variants = feature.variants,
                        defaultVariant = feature.defaultVariant,
                        extraControls = { extraControls(this, data, core) }
                    )

                    div("uk-margin-top") {
                        updateResetButtons("Update", feature.uri())
                    }
                }
            }
        }
    }
}

@JsonSerializable
data class VariantViewModel(
    val name: String,
    val description: String,
    val id: UUID = UUID.randomUUID()
)

private fun Map<Variant, String>.toViewModel() = map {
    VariantViewModel(it.key.value, it.value)
}

private fun FlowContent.editControls(
    core: Core,
    description: String,
    variants: Map<Variant, String>,
    defaultVariant: Variant?,
    idPrefix: String,
    extraControls: FlowContent.() -> Unit
) = div {
    val variantsViewModel = variants.toViewModel()
        .takeIf { it.isNotEmpty() }
        ?: listOf(VariantViewModel("", ""))

    attributes["x-data"] = $$"""{
        variants: $${core.json.asFormatString(variantsViewModel)},
        defaultIndex: $${variantsViewModel.indexOfFirst { it.name == defaultVariant?.value }.coerceAtLeast(0)},
        init() {
            this.$watch('variants', (list) => {
                if (this.defaultIndex >= list.length) this.defaultIndex = 0;
            })
        }
    }"""

    div("uk-margin") {
        label("uk-form-label") {
            htmlFor = "$idPrefix-description"
            +"Description"
        }
        div("uk-form-controls") {
            input(InputType.text, name="description", classes = "uk-input") {
                id = "$idPrefix-description"
                placeholder = "Explain the feature"
                value = description
            }
        }
    }

    extraControls()

    input(InputType.hidden, name = "variants") {
        // convert from key-value pairs to Map<Variant, String>
        attributes[":value"] = "JSON.stringify(variants)"
    }
    input(InputType.hidden, name = "defaultVariant") {
        id = "$idPrefix-defaultVariant"
        attributes[":value"] = "variants[defaultIndex]?.name"
    }

    // TODO would be nice to use the tableForm builder, but would need a solution for the defaultVariant
    table("uk-table uk-table-middle") {
        thead {
            tr {
                th(classes = "uk-width-large") {
                    style = "min-width: 250px;"
                    +"Variant"
                }
                th(classes = "uk-width-auto") { +"Default"}
                th(classes = "uk-width-expand") { +"Description" }
                th(classes = "uk-width-small")
            }
        }

        tbody {
            template {
                attributes["x-for"] = "(entry, index) in variants"
                attributes[":key"] = "index"

                tr {
                    td { // variant
                        input(InputType.text, classes = "uk-input") {
                            attributes["x-model"] = "entry.name"
                            attributes["aria-label"] = "Name"
                            placeholder = "Variant (e.g. off, on)"
                            required = true
                        }
                    }

                    td { // default
                        input(InputType.radio, classes = "uk-radio") {
                            required = true
                            attributes[":value"] = "index"
                            attributes["aria-label"] = "Default"
                            attributes["x-model"] = "defaultIndex"
                        }
                    }

                    td { // description
                        input(InputType.text, classes = "uk-input") {
                            attributes["aria-label"] = "Description"
                            attributes["x-model"] = "entry.description"
                            placeholder = "Description"
                        }
                    }

                    td { // remove button
                        button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
                            attributes["aria-label"] = "Remove Variant"
                            attributes["uk-icon"] = "trash"
                            onClick("variants.splice(index, 1)")
                        }
                    }
                }
            }
        }

        tfoot {
            tr {
                td { // New Row
                    button(type = ButtonType.button, classes = "uk-icon-button uk-button-default") {
                        attributes["aria-label"] = "New Variant"
                        attributes["uk-icon"] = "plus"
                        onClick("variants.push({})")
                    }
                }
            }
        }
    }
}