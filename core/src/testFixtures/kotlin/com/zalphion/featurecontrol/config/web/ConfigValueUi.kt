package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.toNumberProperty
import com.zalphion.featurecontrol.web.toSelectProperty
import org.http4k.lens.BiDiMapping

class ConfigValueUi(private val locator: Locator) {

    val key get() = PropertyKey.parse(locator.getByRole(AriaRole.HEADING).textContent())

    var textValue by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Value"))
        .toInputProperty()

    var booleanValue by locator
        .getByRole(AriaRole.COMBOBOX, Locator.GetByRoleOptions().setName("Value"))
        .toSelectProperty(booleanMapping)

    var numberValue by locator
        .getByRole(AriaRole.SPINBUTTON, Locator.GetByRoleOptions().setName("Value"))
        .toNumberProperty()
}

private val booleanMapping = BiDiMapping<String, Boolean?>(
    asIn = { it?.toString() ?: "" },
    asOut = { when(it.lowercase().trim()) {
        "true" -> true
        "false" -> false
        else -> null
    } }
)