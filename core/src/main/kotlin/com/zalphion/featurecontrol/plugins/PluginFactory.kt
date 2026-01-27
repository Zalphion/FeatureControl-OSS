package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.JsonExport
import com.zalphion.featurecontrol.auth.PermissionsFactory

abstract class PluginFactory<P: Plugin>(
    val jsonExport: JsonExport? = null,
    val permissionsFactoryFn: (Core) -> PermissionsFactory? = { null },
    val lensExports: (Core) -> List<LensContainer<*>> = { emptyList() },
    val componentExports: (Core) -> List<ComponentContainer<*>> = { emptyList() },
    private val onCreate: (P) -> Unit = {},
) {
    protected abstract fun createInternal(core: Core): P

    fun create(core: Core) = createInternal(core).also(onCreate)
}