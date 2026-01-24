package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.zalphion.featurecontrol.teams.TeamId

private val applicationsPageRegex = ".*/teams/([^/]+)/applications.*".toRegex()

class ApplicationsPageUi(private val page: Page) {

    init {
        assertThat(page).hasURL(applicationsPageRegex.toPattern())
    }

    val applications get() = page.applicationsList()

    val teamId = TeamId.parse(applicationsPageRegex.find(page.url())!!.groupValues[1])
}