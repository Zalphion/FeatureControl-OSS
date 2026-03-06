package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.AriaCurrent
import com.zalphion.featurecontrol.web.ariaCurrent
import kotlinx.html.A
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.li
import kotlinx.html.ul
import org.http4k.core.Uri
import kotlin.collections.plus

class EnvironmentsSubNavComponent(
    val application: Application,
    val getUri: (Environment) -> Uri,
    val selected: EnvironmentName?,
    val generalLink: Pair<String, Uri>?
) {
    companion object {
        fun core(
            environmentLabel: A.(Environment) -> Unit = { + it.name.toString() }
        ) = Component<EnvironmentsSubNavComponent> { flow, _, data ->
            flow.ul("uk-subnav uk-subnav-pill uk-margin-remove-top") {
                data.generalLink?.let { (label, uri) ->
                    li {
                        if (data.selected == null) {
                            classes += "uk-active"
                        }
                        a(uri.toString()) {
                            if (data.selected == null) {
                                ariaCurrent = AriaCurrent.Page
                            }
                            +label
                        }
                    }
                }

                for (environment in data.application.environments) {
                    li {
                        if (data.selected == environment.name) {
                            classes += "uk-active"
                        }
                        a(data.getUri(environment).toString()) {
                            if (data.selected == environment.name) {
                                ariaCurrent = AriaCurrent.Page
                            }
                            environmentLabel(environment)
                        }
                    }
                }
            }
        }
    }
}