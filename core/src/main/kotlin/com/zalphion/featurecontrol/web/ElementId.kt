package com.zalphion.featurecontrol.web

import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.and
import dev.forkhandles.values.minLength
import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.id

// TODO unused
class ElementId private constructor(value: String): StringValue(value) {
    companion object: StringValueFactory<ElementId>(
        fn = ::ElementId,
        validation = 1.minLength.and { it.matches("[a-zA-Z0-9-_]+".toRegex()) }
    )
}

var CoreAttributeGroupFacade.elementId: ElementId?
    get() = id.takeIf { it.isNotEmpty() }?.let(ElementId::parse)
    set(value) { id = value?.value.orEmpty() }
