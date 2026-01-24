package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.web.toCheckboxProperty
import com.zalphion.featurecontrol.web.toInputProperty
import org.http4k.lens.StringBiDiMappings

class FeatureEditUi(private val section: Locator) {

    init {
        PlaywrightAssertions.assertThat(section).isVisible()
    }

    var description by section
        .getByLabel("Description")
        .first() // must specify first because there are duplicate inputs within the variant rows
        .toInputProperty()

    val variants get() = section
        .locator("tbody tr").all()
        .map { VariantUi(it) }

    fun newVariant(block: (VariantUi) -> Unit = {}): VariantUi {
        section.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("New Variant")).click()
        return variants.last().also(block)
    }
}

class VariantUi(private val section: Locator) {
    var name by section
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Name"))
        .toInputProperty(Variant.toBiDiMapping())

    var description by section
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Description"))
        .toInputProperty()

    var default by section
        .getByRole(AriaRole.RADIO, Locator.GetByRoleOptions().setName("Default"))
        .toCheckboxProperty()

    fun remove() {
        section.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Remove Variant")).click()
    }
}