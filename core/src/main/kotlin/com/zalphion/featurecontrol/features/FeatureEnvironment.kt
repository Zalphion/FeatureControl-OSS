package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.plugins.Extendable
import com.zalphion.featurecontrol.plugins.Extensions
import dev.forkhandles.values.ComparableValue
import dev.forkhandles.values.IntValue
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.length
import dev.forkhandles.values.minValue

data class FeatureEnvironment(
    val weights: Map<Variant, Weight>,
    val overrides: Map<SubjectId, Variant>, // illegal to have a subjectId point to more than one variant
    override val extensions: Extensions
): Extendable<FeatureEnvironment> {
    override fun with(extensions: Extensions) = copy(extensions = this.extensions + extensions)
}

class SubjectId private constructor(value: String): StringValue(value), ComparableValue<SubjectId, String> {
    companion object: StringValueFactory<SubjectId>(::SubjectId, (1..64).length)
}

class Weight private constructor(value: Int): IntValue(value), ComparableValue<Weight, Int> {
    companion object: IntValueFactory<Weight>(::Weight, 0.minValue)
}