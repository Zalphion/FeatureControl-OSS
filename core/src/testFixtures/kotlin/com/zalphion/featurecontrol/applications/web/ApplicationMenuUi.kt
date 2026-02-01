package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.web.DeleteModalUi
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.web.getModal

class ApplicationMenuUi(private val section: Locator, private val appName: AppName) {

    fun update(block: (ApplicationCreateUpdateUi) -> Unit = {}): ApplicationCreateUpdateUi {
        section.getElement(AriaRole.BUTTON, "Update Application").click()

        val updateModal = section.page().getModal("Update $appName")
        return ApplicationCreateUpdateUi.update(updateModal).also(block)
    }

    fun delete(
        block: (DeleteModalUi<AppName, ApplicationsPage>) -> Unit = {}
    ): DeleteModalUi<AppName, ApplicationsPage> {
        section.getElement(AriaRole.BUTTON, "Delete Application").click()

        val deleteModal = section.page().getModal("Delete $appName")
        return DeleteModalUi(deleteModal, AppName, ::ApplicationsPage).also(block)
    }
}