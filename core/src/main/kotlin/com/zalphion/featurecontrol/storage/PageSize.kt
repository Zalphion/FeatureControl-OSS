package com.zalphion.featurecontrol.storage

import dev.forkhandles.values.IntValue
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.minValue

class PageSize private constructor(value: Int): IntValue(value) {
    companion object: IntValueFactory<PageSize>(::PageSize, 1.minValue)
}