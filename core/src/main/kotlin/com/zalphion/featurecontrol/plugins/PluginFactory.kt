package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.JsonExport
import com.zalphion.featurecontrol.applications.ApplicationCreateData
import com.zalphion.featurecontrol.applications.ApplicationUpdateData
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.auth.PermissionsFactory
import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.members.MemberUpdateData
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.web.PageLink
import org.http4k.format.AutoMarshalling

interface PluginFactory<P: Plugin> {
    fun create(core: Core): P

    fun getJson() = JsonExport(
        moshi = { this },
        mapping = { this }
    )

    fun buildPermissionsFactory(core: Core): PermissionsFactory? = null

    // Register links to the top navbar
    fun getPages(teamId: TeamId, entitlements: Entitlements): Collection<PageLink> = emptyList()

    // get the entitlements provided by the team
    fun getEntitlements(teamId: TeamId): Entitlements = emptySet()

    // extract required entitlements from the data
    fun getRequirements(data: ApplicationCreateData): Entitlements = emptySet()
    fun getRequirements(data: ApplicationUpdateData): Entitlements = emptySet()
     fun getRequirements(data: FeatureCreateData): Entitlements = emptySet()
    fun getRequirements(data: FeatureUpdateData): Entitlements = emptySet()
    fun getRequirements(environment: Environment): Entitlements = emptySet()
    fun getRequirements(data: MemberCreateData): Entitlements = emptySet()
    fun getRequirements(data: MemberUpdateData): Entitlements = emptySet()

    fun getComponentRegistry(json: AutoMarshalling, storage: StorageDriver): ComponentRegistry = ComponentRegistry()
    fun getLensRegistry(json: AutoMarshalling): LensRegistry = LensRegistry()
}

fun <P: Plugin> P.toFactory() = object: PluginFactory<P> {
    override fun create(core: Core) = this@toFactory
}