package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.featureNotFound
import com.zalphion.featurecontrol.lib.mapItem
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.storage.StorageCompanion
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.result4k.asResultOr
import se.ansman.kotshi.JsonSerializable

class FeatureStorage private constructor(private val repository: Repository<StoredFeature, AppId, FeatureKey>) {
    fun list(appId: AppId) = repository.list(appId).mapItem { it.toModel() }
    operator fun get(appId: AppId, featureKey: FeatureKey) = repository[appId, featureKey]?.toModel()
    operator fun plusAssign(feature: Feature) = repository.save(feature.appId, feature.key, feature.toStored())
    operator fun minusAssign(feature: Feature) = repository.delete(feature.appId, feature.key)

    fun getOrFail(appId: AppId, featureKey: FeatureKey) =
        get(appId, featureKey).asResultOr { featureNotFound(appId, featureKey) }

    companion object: StorageCompanion<FeatureStorage, StoredFeature, AppId, FeatureKey>(
        documentType = StoredFeature::class,
        groupIdMapping = AppId.toBiDiMapping(),
        itemIdMapping = FeatureKey.toBiDiMapping(),
        createFn = ::FeatureStorage
    )
}

@JsonSerializable
data class StoredFeature(
    val teamId: TeamId,
    val appId: AppId,
    val key: FeatureKey,
    val variants: Map<Variant, String>,
    val environments: Map<EnvironmentName, StoredFeatureEnvironment>,
    val defaultVariant: Variant,
    val description: String,
    val extensions: Extensions
)

@JsonSerializable
data class StoredFeatureEnvironment(
    val weights: Map<Variant, Weight>,
    val overrides: Map<SubjectId, Variant>, // illegal to have a subjectId point to more than one variant
    val extensions: Extensions
)

private fun StoredFeature.toModel() = Feature(
    teamId = teamId,
    appId = appId,
    key = key,
    variants = variants,
    environments = environments.mapValues { it.value.toModel() },
    defaultVariant = defaultVariant,
    description = description,
    extensions = extensions
)

private fun StoredFeatureEnvironment.toModel() = FeatureEnvironment(
    weights = weights,
    overrides = overrides,
    extensions = extensions
)

private fun Feature.toStored() = StoredFeature(
    teamId = teamId,
    appId = appId,
    key = key,
    variants = variants,
    environments = environments.mapValues { it.value.toStored() },
    defaultVariant = defaultVariant,
    description = description,
    extensions = extensions
)

private fun FeatureEnvironment.toStored() = StoredFeatureEnvironment(
    weights = weights,
    overrides = overrides,
    extensions = extensions
)
