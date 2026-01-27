package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.plugins.Extendable
import com.zalphion.featurecontrol.plugins.Extensions

data class Environment(
    val name: EnvironmentName,
    val description: String,
    val colour: Colour,
    override val extensions: Extensions
): Extendable<Environment> {
    override fun with(extensions: Extensions) = copy(extensions = this.extensions + extensions)
}