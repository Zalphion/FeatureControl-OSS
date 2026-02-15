package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.JsonExport

abstract class PluginFactory<P: Plugin>(
    val jsonExport: JsonExport? = null
) {
    abstract fun create(core: Core): P
}

fun <P: Plugin> P.toFactory() = object: PluginFactory<P>() {
    override fun create(core: Core) = this@toFactory
}