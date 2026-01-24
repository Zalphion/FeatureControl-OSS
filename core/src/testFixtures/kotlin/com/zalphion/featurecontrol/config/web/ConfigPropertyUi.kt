package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.configs.web.ConfigPropertyDto
import com.zalphion.featurecontrol.configs.web.PropertyTypeDto
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.toSelectProperty
import io.kotest.matchers.nulls.shouldNotBeNull
import org.http4k.lens.StringBiDiMappings

class ConfigPropertyUi(private val section: Locator) {

    init {
        PlaywrightAssertions.assertThat(section).isVisible()
    }

    var key by section
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Key"))
        .toInputProperty(PropertyKey.toBiDiMapping())

    var type by section
        .getByRole(AriaRole.COMBOBOX, Locator.GetByRoleOptions().setName("Type"))
        .toSelectProperty(StringBiDiMappings.enum<PropertyTypeDto>())

    var description by section
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Description"))
        .toInputProperty()

    fun remove() {
        section.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Remove Property")).click()
    }

    fun toDto() = ConfigPropertyDto(
        key = key.shouldNotBeNull(),
        type = type.shouldNotBeNull(),
        description = description.orEmpty()
    )
}