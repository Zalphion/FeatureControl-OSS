package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import java.lang.AutoCloseable

abstract class ModalUi(protected val locator: Locator): AutoCloseable {

    init {
        // wait for the modal to be visible
        locator.waitFor(Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE))

        // wait for any alpine updates to occur
        locator.evaluate("() => new Promise(resolve => window.Alpine.nextTick(resolve))")
    }

    override fun close() {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Close")).click()
    }
}