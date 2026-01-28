package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.events.Event
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.members.MemberUpdateData
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.web.PageLink
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import org.http4k.routing.RoutingHttpHandler

interface Plugin {
    fun onEvent(event: Event): Result4k<Unit, AppError> = Unit.asSuccess()

    // get the entitlements provided by the team
    fun getEntitlements(teamId: TeamId): Entitlements = emptySet()

    // extract required entitlements from the data
    fun getRequirements(data: FeatureCreateData): Entitlements = emptySet()
    fun getRequirements(data: FeatureUpdateData): Entitlements = emptySet()
    fun getRequirements(environment: Environment): Entitlements = emptySet()
    fun getRequirements(data: MemberCreateData): Entitlements = emptySet()
    fun getRequirements(data: MemberUpdateData): Entitlements = emptySet()

    // Applied to the HTTP root (bring-your-own security)
    fun getRoutes(): RoutingHttpHandler? = null

    // Applied within the existing web routes, using its security
    fun getWebRoutes(): RoutingHttpHandler? = null

    // Register links to the top navbar
    fun getPages(teamId: TeamId): Collection<PageLink> = emptyList()

    companion object
}