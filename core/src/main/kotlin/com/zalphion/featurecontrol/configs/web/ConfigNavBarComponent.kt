package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.web.EnvironmentsSubNavComponent
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.configUri
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.ul

class ConfigNavBarComponent(
    val application: Application,
    val selected: ConfigEnvironment?
) {
    companion object {
        fun core(
            extraNavBarLeft: (DIV.(Core, Application, ConfigEnvironment?) -> Unit) = { _, _, _ -> },
            extraNavBarRight: (DIV.(Core, Application, ConfigEnvironment?) -> Unit) = { _, _, _ -> },
        ) = Component<ConfigNavBarComponent> { flow, core, data ->
            flow.nav {
                div("uk-navbar-container uk-navbar-transparent") {
                    attributes["uk-navbar"] = ""

                    div("uk-navbar-left") {
                        h3("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                            span {
                                attributes["uk-icon"] = "icon: file-text"
                            }
                            +"Config"
                        }

                        extraNavBarLeft(core, data.application, data.selected)
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

                        extraNavBarRight(core, data.application, data.selected)
                    }
                }

                core.render(flow, EnvironmentsSubNavComponent(
                    application = data.application,
                    getUri = { configUri(data.application.teamId, data.application.appId, it.name) },
                    selected = data.selected?.name,
                    generalLink = "Properties" to data.application.uri(),
                ))
            }
        }
    }
}