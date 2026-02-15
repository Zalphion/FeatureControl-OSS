package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageCompanion
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.h2db.h2DbInMemory
import dev.andrewohara.utils.IdGenerator
import kotlin.random.Random
import kotlin.random.asJavaRandom

open class StorageTestDriver(plugins: List<PluginFactory<*>> = emptyList()) {

    val random = Random(1337)

    private val storageDriver = StorageDriver.h2DbInMemory(PageSize.of(2))
    private val idGen = IdGenerator(random.asJavaRandom())
    private val json = buildJson(plugins)

    fun <Storage: Any, Doc: Any, GroupId: Any, ItemId: Any> create(
        companion: StorageCompanion<Storage, Doc, GroupId, ItemId>
    ) = companion.create(
        name = idGen.nextBase36(4),
        storageDriver = storageDriver,
        json = json
    )
}