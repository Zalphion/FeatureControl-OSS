package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.web.mainNavBar

private val applicationsPageRegex = ".*/teams/([^/]+)/applications.*".toRegex()

class ApplicationsPage(private val page: Page) {

    init {
        assertThat(page).hasURL(applicationsPageRegex.toPattern())
    }

    val mainNavBar get() = page.mainNavBar()
    val applications get() = page.applicationsList()

    val teamId = TeamId.parse(applicationsPageRegex.find(page.url())!!.groupValues[1])
}