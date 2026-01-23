package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.configUri
import com.zalphion.featurecontrol.web.cssStyle
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

class Card(
    val name: String,
    val link: Uri,
    val type: String,
    val icon: String,
    val badge: FlowContent.() -> Unit
)

fun FlowContent.card(
    card: Card,
    selected: Boolean,
    filterModel: String
) {
    a(card.link.toString()) {
        if (selected) {
            attributes["aria-current"] = "page"
        } else {
            // only filter if it's not currently selected
            attributes["x-show"] = "'${card.name}'.toLowerCase().includes($filterModel.toLowerCase())"
        }

        div("uk-card uk-card-hover uk-card-small uk-margin") {
            classes += if (selected) "uk-card-primary" else "uk-card-default"


            div("uk-card-body") {
                h3("uk-card-title") {
                    +card.name
                }

                div {
                    style = cssStyle(
                        "position" to "absolute",
                        "top" to "8px",
                        "right" to "8px"
                    )
                    card.badge(this)
                }

                p {
                    span("uk-margin-small-right") {
                        attributes["uk-icon"] = card.icon
                    }
                    +card.type
                }
            }
        }
    }
}

fun coreFeatureCard(
    feature: Feature,
    badge: FlowContent.() -> Unit = {}
) = Card(
    name = feature.key.value,
    link = feature.uri(),
    type = "Feature",
    icon = PageSpec.features.icon,
    badge = badge
)

fun coreConfigCard(
    application: Application,
    badge: FlowContent.() -> Unit = {}
) = Card(
    name = "Config",
    link = configUri(application.teamId, application.appId),
    type = "Config",
    icon = PageSpec.config.icon,
    badge = badge
)

fun applicationCard(
    application: Application,
    badge: FlowContent.() -> Unit = {}
) = Card(
    name = application.appName.value,
    link = application.uri(),
    type = "Application",
    icon = "icon: album",
    badge = badge
)