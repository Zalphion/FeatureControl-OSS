package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.teams.TeamId

class StaticEntitlementsPlugin(val entitlements: Entitlements): Plugin {
    override fun getEntitlements(teamId: TeamId) = entitlements
}

fun Plugin.Companion.staticEntitlements(entitlements: Entitlements) = object: PluginFactory<StaticEntitlementsPlugin>() {
    override fun createInternal(core: Core) = StaticEntitlementsPlugin(entitlements)
}