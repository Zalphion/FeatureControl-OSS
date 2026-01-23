package com.zalphion.featurecontrol.storage

import com.zalphion.featurecontrol.lib.asBiDiMapping
import org.http4k.format.AutoMarshalling
import org.http4k.lens.BiDiMapping
import kotlin.reflect.KClass

abstract class StorageCompanion<Doc: Any, GroupId: Any, ItemId: Any>(
    private val groupIdMapping: BiDiMapping<String, GroupId>,
    private val itemIdMapping: BiDiMapping<String, ItemId>,
    private val documentType: KClass<Doc>
) {
    fun create(name: String, storageDriver: StorageDriver, json: AutoMarshalling) = storageDriver.create(
        name = name,
        groupIdMapper = groupIdMapping,
        itemIdMapper = itemIdMapping,
        documentMapper = json.asBiDiMapping(documentType)
    )
}