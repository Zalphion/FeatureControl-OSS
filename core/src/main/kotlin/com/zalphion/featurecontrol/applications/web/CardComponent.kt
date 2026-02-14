package com.zalphion.featurecontrol.applications.web

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
            badge: FlowContent.(FeatureCardComponent) -> Unit = {}
        ) = Component<FeatureCardComponent> { flow, data ->
            flow.renderCard(
                name = data.feature.key.value,
                link = data.feature.uri(),
                type = "Feature",
                icon = PageSpec.features.icon,
                selected = data.selected,
                filterModel = data.filterModel,
                badge = { badge(this, data) }
            )
        }
    }
}

class ConfigCardComponent(val application: Application, val selected: Boolean, val filterModel: String) {
    companion object {
        fun core(
            badge: FlowContent.(ConfigCardComponent) -> Unit = {}
        ) = Component<ConfigCardComponent> { flow, data ->
            flow.renderCard(
                name = "Config",
                link = configUri(data.application.teamId, data.application.appId),
                type = "Config",
                icon = PageSpec.config.icon,
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
        ) = Component<ApplicationCardComponent> { flow, data ->
            flow.renderCard(
                name = data.application.appName.value,
                link = data.application.uri(),
                type = "Application",
                icon = "icon: album",
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
    icon: String,
    type: String,
    selected: Boolean,
    filterModel: String,
    badge: FlowContent.() -> Unit
) {
    flowTemplate {
        attributes["x-if"] = "'$name'.toLowerCase().includes($filterModel.toLowerCase())"

        a(link.toString()) {
            if (selected) {
                ariaCurrent = AriaCurrent.Page
            } else {
                // only filter if it's not currently selected
                attributes["x-show"] = "'$name'.toLowerCase().includes($filterModel.toLowerCase())"
            }

            div("uk-card uk-card-hover uk-card-small uk-margin") {
                classes += if (selected) "uk-card-primary" else "uk-card-default"


                div("uk-card-body") {
                    h3("uk-card-title") {
                        +name
                    }

                    div {
                        style = cssStyle(
                            "position" to "absolute",
                            "top" to "8px",
                            "right" to "8px"
                        )
                        badge(this)
                    }

                    p {
                        span("uk-margin-small-right") {
                            attributes["uk-icon"] = icon
                        }
                        +type
                    }
                }
            }
        }
    }
}