package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.teams.TeamId

fun Plugin.Companion.staticEntitlements(entitlements: Entitlements) = object: PluginFactory<Plugin> {
    override fun getEntitlements(teamId: TeamId) = entitlements

    override fun create(core: Core) = object: Plugin {}
}