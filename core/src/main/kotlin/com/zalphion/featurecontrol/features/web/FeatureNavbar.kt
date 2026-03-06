package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.web.EnvironmentsSubNavComponent
import com.zalphion.featurecontrol.web.components.deleteModal
import com.zalphion.featurecontrol.web.components.modalTextButton
import com.zalphion.featurecontrol.web.components.moreMenu
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.ul
import kotlin.collections.plus

fun FlowContent.featureNavbar(
    core: Core,
    application: Application,
    feature: Feature,
    selected: EnvironmentName?,
    leftNavbarItems: Collection<(FlowContent.() -> Unit)> = emptyList()
) = nav {
    div( "uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h2("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                span {
                    attributes["uk-icon"] = "icon: cog"
                }
                +feature.key.value
            }
            for (item in leftNavbarItems) {
                div("uk-navbar-item") { item(this) }
            }
        }

        div("uk-navbar-right") {
            ul("uk-iconnav") {
                li {
                    a("#", classes = "uk-icon-button") {
                        attributes["uk-icon"] = "icon: code"
                        attributes["uk-tooltip"] = "Use this feature in your app"
                    }
                }
                li {
                    moreMenu(feature.key) { dropdownId ->
                        li {
                            val deleteModalId = deleteModal(feature.key.value, feature.uri())
                            modalTextButton(
                                label = "Delete Feature",
                                icon = "icon: trash",
                                modalId = deleteModalId,
                                dropdownToCloseId = dropdownId
                            ) {
                                classes += "uk-text-danger"
                            }
                        }
                    }
                }
            }
        }
    }

    core.render(this, EnvironmentsSubNavComponent(
        application = application,
        getUri = { feature.uri(it.name) },
        selected = selected,
        generalLink = "General" to feature.uri()
    ))
}