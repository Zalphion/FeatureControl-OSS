package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.web.deleteModal
import com.zalphion.featurecontrol.web.modalButton
import com.zalphion.featurecontrol.web.moreMenu
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
                            modalButton(
                                label = "Delete Feature",
                                icon = "icon: trash",
                                modalId = deleteModalId,
                                dropdownToCloseId = dropdownId
                            ) {
                                classes + "uk-text-danger"
                            }
                        }
                    }
                }
            }
        }
    }

    ul("uk-subnav uk-subnav-pill uk-margin-remove-top") {
        li {
            if (selected == null) classes + "uk-active"
            a(feature.uri().toString()) {
                +"General"
            }
        }
        for (environment in application.environments) {
            li {
                if (selected == environment) classes + "uk-active"
                a(feature.uri(environment.name).toString()) {
                    +environment.name.value
                }
            }
        }
    }
}