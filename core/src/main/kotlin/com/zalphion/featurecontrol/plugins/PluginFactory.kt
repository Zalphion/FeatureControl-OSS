package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.JsonExport
import com.zalphion.featurecontrol.storage.StorageDriver
import org.http4k.format.AutoMarshalling

abstract class PluginFactory<P: Plugin>(
    val jsonExport: JsonExport? = null
) {
    abstract fun create(json: AutoMarshalling, storage: StorageDriver): P
}

fun <P: Plugin> P.toFactory() = object: PluginFactory<P>() {
    override fun create(json: AutoMarshalling, storage: StorageDriver) = this@toFactory
}