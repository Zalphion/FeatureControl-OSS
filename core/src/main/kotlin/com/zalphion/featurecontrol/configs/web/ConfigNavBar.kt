package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.web.configUri
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.ul
import kotlin.collections.plus

fun FlowContent.coreConfigNavBar(
    application: Application,
    selected: EnvironmentName?,
    extraNavBarLeft: FlowContent.() -> Unit = {}
) {
    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h3("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                span {
                    attributes["uk-icon"] = "icon: file-text"
                }
                +"Config"
            }
            extraNavBarLeft()
        }

        div("uk-navbar-right") {
            ul("uk-iconnav") {
                li {
                    a("#", classes = "uk-icon-button") {
                        attributes["uk-icon"] = "icon: code"
                        attributes["uk-tooltip"] = "Use this config in your app"
                    }
                }
            }
        }
    }

    ul("uk-subnav uk-subnav-pill uk-margin-remove-top") {
        li {
            if (selected == null) classes + "uk-active"
            a(application.uri().toString()) {
                +"Properties"
            }
        }
        for (environment in application.environments) {
            li {
                if (selected == environment.name) classes + "uk-active"
                a(configUri(application.teamId, application.appId, environment.name).toString()) {
                    +environment.name.value
                }
            }
        }
    }
}