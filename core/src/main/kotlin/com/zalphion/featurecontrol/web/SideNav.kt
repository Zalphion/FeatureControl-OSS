package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.FeatureControl
import kotlinx.html.ASIDE

class SideNav(
    val pages: List<PageLink>,
    val selected: PageSpec?,
    val topBar: ASIDE.(FeatureControl) -> Unit = {}
)