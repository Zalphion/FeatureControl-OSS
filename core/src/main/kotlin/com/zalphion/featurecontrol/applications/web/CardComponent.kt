package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.AriaCurrent
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.ariaCurrent
import com.zalphion.featurecontrol.web.configUri
import com.zalphion.featurecontrol.web.cssStyle
import com.zalphion.featurecontrol.web.flowTemplate
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.H3
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import org.http4k.core.Uri
import kotlin.collections.plus

class FeatureCardComponent(
    val application: Application,
    val feature: Feature,
    val selected: Boolean,
    val filterModel: String
) {
    companion object {
        fun core(
            badge: FlowContent.(FeatureCardComponent, Core) -> Unit = { _, _ -> }
        ) = Component<FeatureCardComponent> { flow, core, data ->
            flow.renderCard(
                name = data.feature.key.value,
                link = data.feature.uri(),
                type = PageSpec.features,
                selected = data.selected,
                filterModel = data.filterModel,
                badge = { badge(this, data, core) }
            )
        }
    }
}

class ConfigCardComponent(val application: Application, val selected: Boolean, val filterModel: String) {
    companion object {
        fun core(
            badge: FlowContent.(ConfigCardComponent) -> Unit = {}
        ) = Component<ConfigCardComponent> { flow, _, data ->
            flow.renderCard(
                name = "Config",
                link = configUri(data.application.teamId, data.application.appId),
                type = PageSpec.config,
                selected = data.selected,
                filterModel = data.filterModel,
                badge = { badge(this, data) }
            )
        }
    }
}

class ApplicationCardComponent(val application: Application, val selected: Boolean, val filterModel: String) {
    companion object {
        fun core(
            badge: FlowContent.(ApplicationCardComponent) -> Unit = {}
        ) = Component<ApplicationCardComponent> { flow, _, data ->
            flow.renderCard(
                name = data.application.appName.value,
                link = data.application.uri(),
                type = PageSpec.applications,
                selected = data.selected,
                filterModel = data.filterModel,
                badge = { badge(this, data) }
            )
        }
    }
}

fun FlowContent.renderCard(
    name: String,
    link: Uri,
    selected: Boolean,
    type: PageSpec? = null,
    filterModel: String? = null,
    badge: FlowContent.() -> Unit = {},
    content: FlowContent.() -> Unit = {}
) = renderCard(
    name = { +name },
    searchTerm = name,
    link = link,
    type = type,
    selected = selected,
    filterModel = filterModel,
    badge = badge,
    content = content,
)

fun FlowContent.renderCard(
    name: H3.() -> Unit,
    link: Uri,
    selected: Boolean,
    type: PageSpec? = null,
    searchTerm: String? = null,
    filterModel: String? = null,
    badge: FlowContent.() -> Unit = {},
    content: FlowContent.() -> Unit = {}
) {
    fun FlowContent.render() = a(link.toString()) {
        if (selected) {
            ariaCurrent = AriaCurrent.Page
        }

        div("uk-card uk-card-hover uk-card-small uk-margin") {
            classes += if (selected) "uk-card-primary" else "uk-card-default"


            div("uk-card-body") {
                h3("uk-card-title") {
                    name()
                }

                div {
                    style = cssStyle(
                        "position" to "absolute",
                        "top" to "8px",
                        "right" to "8px"
                    )
                    badge(this)
                }

                content()

                if (type != null) {
                    p {
                        span("uk-margin-small-right") {
                            attributes["uk-icon"] = type.icon
                        }
                        +type.name
                    }
                }
            }
        }
    }

    if (filterModel != null && searchTerm != null) {
        flowTemplate {
            attributes["x-if"] = "'$searchTerm'.toLowerCase().includes($filterModel.toLowerCase())"
            render()
        }
    } else {
        render()
    }
}