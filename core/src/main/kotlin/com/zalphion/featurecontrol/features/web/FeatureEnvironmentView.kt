package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.web.tagBuilder
import com.zalphion.featurecontrol.web.updateResetButtons
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.input
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr

fun FlowContent.coreFeatureEnvironment(
    core: Core,
    feature: Feature,
    environment: FeatureEnvironment,
    environmentName: EnvironmentName,
    extraData: String = "",
    extraInputs: FlowContent.() -> Unit = {},
    extraColumns: List<Pair<String, FlowContent.(Variant) -> Unit>> = emptyList()
) {
    val variantOverrides = environment.overrides.entries
        .groupBy { it.value }
        .mapValues { (_, value) -> value.map { (subjectId, _) -> subjectId } }
        // ensure every variant is present
        .let { overrides -> feature.variants.keys.associateWith { overrides[it] ?: emptyList() } }

    form(method = FormMethod.post) {
        attributes["x-data"] = """{
            "weights": ${core.json.asFormatString(environment.weights)}, // Map<Variant, Weight>
            "overrides": ${core.json.asFormatString(variantOverrides)}, // Map<Variant, List<SubjectId>>
            "temp": ${core.json.asFormatString(feature.variants.mapValues { "" })}, // List<Variant>
            $extraData
        }"""

        input(InputType.hidden, name = "weights") {
            attributes["x-model"] = "JSON.stringify(weights)"
        }

        input(InputType.hidden, name = "overrides") {
            attributes["x-model"] = "JSON.stringify(overrides)"
        }

        input(InputType.hidden, name = "metadata") {
            value = "{}"
        }

        extraInputs()

        table("uk-table") {
            style = "width: auto; table-layout: auto;" // shrink to fit content
            thead {
                tr {
                    th(classes = "uk-width-small") { +"Variant" }
                    th(classes = "uk-width-auto") {
                        style = "min-width: 200px;"
                        +"Weight"
                    }
                    th(classes = "uk-width-large") { +"Overrides" }
                    for ((label, _) in extraColumns) {
                        th { +label }
                    }
                }
            }
            tbody {
                for (variant in feature.variants.keys) {
                    tr {
                        td { +variant.value }
                        td {
                            input(InputType.number, classes = "uk-input") {
                                placeholder = "Weight (e.g. 85)"
                                attributes["x-model.number"] = "weights['$variant']"
                                attributes["min"] = "0"
                            }
                        }
                        td {
                            tagBuilder(
                                result = "overrides['$variant']",
                                temp = "temp['$variant']",
                                prompt = "Subject ID"
                            )
                        }
                        for ((_, extraColumn) in extraColumns) {
                            td { extraColumn(variant) }
                        }
                    }
                }
            }
        }

        div("uk-margin-top") {
            updateResetButtons("Update", feature.uri(environmentName))
        }
    }
}