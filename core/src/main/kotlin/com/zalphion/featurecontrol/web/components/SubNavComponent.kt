package com.zalphion.featurecontrol.web.components

import com.zalphion.featurecontrol.web.AriaCurrent
import com.zalphion.featurecontrol.web.ariaCurrent
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.li
import kotlinx.html.ul
import org.http4k.core.Uri
import kotlin.collections.plus

fun FlowContent.subNavLinks(options: List<Pair<String, Uri>>, selected: String) {
    ul("uk-subnav uk-subnav-pill uk-margin-remove-top") {
        for ((name, url) in options) {
            li {
                if (selected == name) {
                    classes += "uk-active"
                }
                a(url.toString()) {
                    if (selected == name) {
                        ariaCurrent = AriaCurrent.Page
                    }
                    +name
                }
            }
        }
    }
}