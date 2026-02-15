package com.zalphion.featurecontrol.web

import com.microsoft.playwright.BrowserContext
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.teams.TeamCreateUpdateData
import com.zalphion.featurecontrol.teams.TeamName
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class MainMainNavBarUiTest: CoreTestDriver() {

    private val member = core.users.create(idp1Email1).shouldBeSuccess()
    private val team2 = core.teams.create(member.user.userId, TeamCreateUpdateData(TeamName.parse("new team")))
        .invoke(member.user, app)
        .shouldBeSuccess()

    @RegisterExtension
    val playwright = playwright()

    @Test
    fun `show team selector`(context: BrowserContext) {
        context.asUser(app, member.user) { page ->
            page.mainNavBar.openTeams { teams ->
                teams.options.shouldContainExactlyInAnyOrder(member.team.teamName, team2.teamName)
            }
        }
    }

    @Test
    fun `switch teams`(context: BrowserContext) {
        context.asUser(app, member.user) { page ->
            page.mainNavBar.openTeams { teams ->
                teams.goToTeam(team2.teamName) { result ->
                    result.mainNavBar.currentTeam shouldBe team2.teamName
                }
            }
        }
    }

    @Test
    fun `create team`(context: BrowserContext) {
        val name = TeamName.parse("super team")

        context.asUser(app, member.user) { page ->
            page.mainNavBar.openTeams { teams ->
                teams.createTeam { team ->
                    team.name = name
                }.create { result ->
                    result.mainNavBar.currentTeam shouldBe name
                    result.mainNavBar.openTeams { teams ->
                        teams.options.shouldContainExactlyInAnyOrder(
                            member.team.teamName, team2.teamName, name
                        )
                    }

                    result.mainNavBar.goToApplications { applications ->
                        applications.mainNavBar.currentTeam shouldBe name
                    }
                }
            }
        }
    }
}