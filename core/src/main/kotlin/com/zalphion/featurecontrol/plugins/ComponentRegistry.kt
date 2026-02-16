package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import kotlinx.html.FlowContent
import kotlin.reflect.KClass

class ComponentRegistry private constructor(private val components: Map<KClass<*>, Component<*>>) {

    constructor(): this(emptyMap())

    inline fun <reified T: Any> with(component: Component<T>) = with(T::class, component)

    fun <T: Any> with(type: KClass<T>, component: Component<T>) = ComponentRegistry(components + (type to component))

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> render(type: KClass<T>, flow: FlowContent, core: Core, data: T) =
        (components[type] as? Component<T>)
        ?.invoke(flow, core,data)
        ?: error("Could not find $type component")

    operator fun plus(other: ComponentRegistry) = ComponentRegistry(components + other.components)

    operator fun plus(others: Collection<ComponentRegistry>) = others.fold(this) { acc, next ->
        acc + next
    }
}

fun interface Component<T: Any>: (FlowContent, Core, T) -> Unit