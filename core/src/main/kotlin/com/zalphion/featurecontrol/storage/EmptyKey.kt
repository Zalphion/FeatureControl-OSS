package com.zalphion.featurecontrol.storage

import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory

class EmptyKey private constructor(value: String): StringValue(value) {
    companion object: StringValueFactory<EmptyKey>(::EmptyKey, { it == "_" }) {
        val INSTANCE = of("_")
    }
}