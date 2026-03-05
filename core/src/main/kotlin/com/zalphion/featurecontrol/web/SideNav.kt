package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.web.components.navButton
import kotlinx.html.ASIDE
import kotlinx.html.FlowContent
import kotlinx.html.aside
import kotlinx.html.div
import kotlinx.html.style

class SideNav(
    val pages: List<PageLink>,
    val selected: PageSpec?,
    val topBar: ASIDE.(Core) -> Unit = {}
)

fun SideNav.render(flow: FlowContent, core: Core, rightSide: Boolean = false) {
    flow.aside("uk-width-large uk-background-muted uk-padding-small uk-overflow-auto") {
        val blurValue = if (rightSide) "-2px" else "2px"
        style = "box-shadow: $blurValue 0 5px rgba(0, 0, 0, 0.05);"

        topBar(this, core)

        if (pages.isNotEmpty()) {
            div {
                for (page in pages) {
                    navButton(page, selected = page.spec == selected)
                }
            }
        }
    }
}