package com.zalphion.featurecontrol.storage

import com.zalphion.featurecontrol.lib.asBiDiMapping
import org.http4k.format.AutoMarshalling
import org.http4k.lens.BiDiMapping
import kotlin.reflect.KClass

abstract class StorageCompanion<Storage: Any, Doc: Any, GroupId: Any, ItemId: Any>(
    private val groupIdMapping: BiDiMapping<String, GroupId>,
    private val itemIdMapping: BiDiMapping<String, ItemId>,
    private val documentType: KClass<Doc>,
    private val createFn: (Repository<Doc, GroupId, ItemId>) -> Storage
) {
    fun create(name: String, storageDriver: StorageDriver, json: AutoMarshalling) = storageDriver.create(
        collectionName = name,
        groupIdMapper = groupIdMapping,
        itemIdMapper = itemIdMapping,
        documentMapper = json.asBiDiMapping(documentType)
    ).let(createFn)
}