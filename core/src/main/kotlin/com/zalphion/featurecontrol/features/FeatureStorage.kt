package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.featureNotFound
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import dev.forkhandles.result4k.asResultOr
import org.http4k.format.AutoMarshalling

class FeatureStorage private constructor(private val repository: Repository<Feature, AppId, FeatureKey>) {
    fun list(appId: AppId, pageSize: Int) = repository.list(appId, pageSize)
    operator fun get(appId: AppId, featureKey: FeatureKey) = repository[appId, featureKey]
    operator fun plusAssign(feature: Feature) = repository.save(feature.appId, feature.key, feature)
    operator fun minusAssign(feature: Feature) = repository.delete(feature.appId, feature.key)

    fun getOrFail(appId: AppId, featureKey: FeatureKey) =
        get(appId, featureKey).asResultOr { featureNotFound(appId, featureKey) }

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = FeatureStorage(storage.create(
            name = "features",
            groupIdMapper = AppId.toBiDiMapping(),
            itemIdMapper = FeatureKey.toBiDiMapping(),
            documentMapper = json.asBiDiMapping<Feature>() // TODO DTO
        ))
    }
}