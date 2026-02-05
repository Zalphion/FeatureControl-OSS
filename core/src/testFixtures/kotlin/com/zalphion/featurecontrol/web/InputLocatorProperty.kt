package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.zalphion.featurecontrol.lib.toBiDiMapping
import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory
import org.http4k.lens.BiDiMapping
import kotlin.reflect.KProperty

class InputLocatorProperty<T>(
    private val locator: Locator,
    private val mapping: BiDiMapping<String, T>
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return locator.inputValue()
            .takeIf { it.isNotEmpty() }
            ?.let(mapping::invoke)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        locator.fill(value?.let(mapping::invoke) ?: "")
    }
}

inline fun <reified DOMAIN: Value<PRIM>, PRIM: Any> Locator.toInputProperty(valueFactory: ValueFactory<DOMAIN, PRIM>) =
    toInputProperty(valueFactory.toBiDiMapping())

fun <T> Locator.toInputProperty(mapping: BiDiMapping<String, T>) = InputLocatorProperty(this, mapping)

fun Locator.toInputProperty() = InputLocatorProperty(this, BiDiMapping({ it }, { it }))