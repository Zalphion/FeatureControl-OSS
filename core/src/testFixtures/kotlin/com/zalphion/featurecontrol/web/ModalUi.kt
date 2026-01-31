package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import java.lang.AutoCloseable

abstract class ModalUi(protected val locator: Locator): AutoCloseable {

    init {
        PlaywrightAssertions.assertThat(locator).isVisible()
    }

    override fun close() {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Close")).click()
    }
}