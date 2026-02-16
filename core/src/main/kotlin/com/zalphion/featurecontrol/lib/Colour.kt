package com.zalphion.featurecontrol.lib

import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.regex

/**
 * Holds a colour in its hex representation.
 */
class Colour private constructor(value: String): StringValue(value) {
    companion object: StringValueFactory<Colour>(::Colour, "^#[0-9A-Fa-f]{6}$".regex)
}