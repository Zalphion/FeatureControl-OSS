package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.ApplicationCreateData
import com.zalphion.featurecontrol.applications.ApplicationUpdateData
import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.events.Event
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.applications.web.Card
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.configs.Property
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.members.MemberUpdateData
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.web.PageLink
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import kotlinx.html.FlowContent
import org.http4k.lens.BodyLens
import org.http4k.routing.RoutingHttpHandler

interface Plugin {
    fun onEvent(event: Event): Result4k<Unit, AppError> = Unit.asSuccess()

    // entitlements
    fun getRequirements(team: TeamId): Entitlements = emptySet()
    fun getRequirements(data: FeatureCreateData): Entitlements = emptySet()
    fun getRequirements(data: FeatureUpdateData): Entitlements = emptySet()
    fun getRequirements(environment: Environment): Entitlements = emptySet()
    fun getRequirements(data: MemberCreateData): Entitlements = emptySet()
    fun getRequirements(data: MemberUpdateData): Entitlements = emptySet()

    // HTTP
    fun getRoutes(): RoutingHttpHandler? = null
    fun getWebRoutes(): RoutingHttpHandler? = null
    fun getPages(teamId: TeamId): Collection<PageLink> = emptyList()

    // applications
    fun configCard(application: Application): Card? = null
    fun renderNewApplicationModal(flow: FlowContent, team: Team): String? = null
    fun renderUpdateApplicationModal(flow: FlowContent, application: Application): String? = null
    fun createApplicationCreateDataLens(): BodyLens<ApplicationCreateData>? = null
    fun createApplicationUpdateDataLens(): BodyLens<ApplicationUpdateData>? = null

    // features
    fun featureCard(application: Application, feature: Feature): Card? = null
    fun newFeatureModal(flow: FlowContent, application: Application): String? = null
    fun createFeatureCreateDataLens(): BodyLens<FeatureCreateData>? = null
    fun createFeatureUpdateDataLens(): BodyLens<FeatureUpdateData>? = null
    fun createFeatureEnvironmentDataLens(): BodyLens<FeatureEnvironment>? = null
    fun featureContent(flow: FlowContent, application: Application, feature: Feature): Unit? = null
    fun featureEnvironmentContent(flow: FlowContent, application: Application, feature: Feature, name: EnvironmentName, environment: FeatureEnvironment): Unit? = null

    // configs
    fun createConfigSpecDataLens(): BodyLens<Map<PropertyKey, Property>>? = null
    fun createConfigEnvironmentDataLens(): BodyLens<Map<PropertyKey, String>>? = null
    fun configSpecContent(flow: FlowContent, application: Application, spec: ConfigSpec): Unit? = null
    fun configEnvironmentContent(flow: FlowContent, application: Application, spec: ConfigSpec, values: ConfigEnvironment): Unit? = null

    companion object
}