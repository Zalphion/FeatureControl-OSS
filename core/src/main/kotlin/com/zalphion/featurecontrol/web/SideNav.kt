package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.Core
import kotlinx.html.FlowContent

class SideNav(
    val pages: List<PageLink>,
    val selected: PageSpec?,
    val topBar: FlowContent.(Core) -> Unit = {},
)