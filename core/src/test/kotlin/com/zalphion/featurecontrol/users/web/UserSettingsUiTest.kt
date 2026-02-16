package com.zalphion.featurecontrol.users.web

import org.http4k.playwright.Http4kBrowser
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.addTo
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.idp1Email4
import com.zalphion.featurecontrol.idp2Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.web.asUser
import com.zalphion.featurecontrol.web.playwright
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class UserSettingsUiTest: CoreTestDriver() {

    private val member = core.users.create(idp1Email1).shouldBeSuccess()

    @RegisterExtension
    val playwright = playwright()

    @Test
    fun `show blank page`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.mainNavBar.user.username shouldBe null
            page.mainNavBar.user.email shouldBe idp1Email1

            page.mainNavBar.user.open().goToSettings { settings ->
                settings.memberships.shouldHaveSize(1)
                settings.memberships[0].also { membership ->
                    membership.teamName shouldBe member.team.teamName
                    membership.role shouldBe "Member"
                }

                settings.invitations.shouldHaveSize(0)
            }
        }
    }

    @Test
    fun `show additional memberships and invitations`(browser: Http4kBrowser) {
        val otherMember1 = core.users.create(idp2Email1).shouldBeSuccess()
        member.user.addTo(core, otherMember1.team)

        val otherMember2 = core.users.create(idp1Email4).shouldBeSuccess()
        val invitation = core.members.invite(
            teamId = otherMember2.team.teamId,
            sender = otherMember2.user.userId,
            data = MemberCreateData(member.user.emailAddress, emptyMap())
        ).invoke(core, otherMember2.user,).shouldBeSuccess()

        browser.asUser(core, member.user) { page ->
            page.mainNavBar.user.open().goToSettings { settings ->
                settings.memberships
                    .map { it.teamName }
                    .shouldContainExactlyInAnyOrder(member.team.teamName, otherMember1.team.teamName)

                settings.memberships.find { it.teamName == member.team.teamName } shouldNotBeNull {
                    role shouldBe "Member"
                }

                settings.memberships.find { it.teamName == member.team.teamName } shouldNotBeNull {
                    role shouldBe "Member"
                }

                settings.invitations.shouldHaveSize(1)
                settings.invitations[0].also {
                    it.teamName shouldBe otherMember2.team.teamName
                    it.invitedBy shouldBe otherMember2.user.emailAddress
                    it.expires shouldBe invitation.member.invitationExpiresOn.shouldNotBeNull()
                }
            }
        }
    }

    @Test
    fun `leave team`(browser: Http4kBrowser) {
        val otherMember = core.users.create(idp1Email4).shouldBeSuccess()
        member.user.addTo(core, otherMember.team)

        browser.asUser(core, member.user) { page ->
            page.mainNavBar.user.open().goToSettings { settings ->
                settings.memberships
                    .find { it.teamName == otherMember.team.teamName }
                    .shouldNotBeNull()
                    .leave().confirm { result ->
                        result.memberships.map { it.teamName }.shouldContainExactlyInAnyOrder(member.team.teamName)
                    }
            }
        }
    }

    @Test
    fun `accept invitation`(browser: Http4kBrowser) {
        val otherMember = core.users.create(idp1Email4).shouldBeSuccess()
        core.members.invite(
            teamId = otherMember.team.teamId,
            sender = otherMember.user.userId,
            data = MemberCreateData(member.user.emailAddress, emptyMap())
        ).invoke(core, otherMember.user).shouldBeSuccess()

        browser.asUser(core, member.user) { page ->
            page.mainNavBar.user.open().goToSettings { settings ->
                settings.invitations
                    .find { it.teamName == otherMember.team.teamName }
                    .shouldNotBeNull()
                    .accept { result ->
                        // should be on the applications page of the new team
                        result.mainNavBar.currentTeam shouldBe otherMember.team.teamName
                    }
                    .mainNavBar.user.open().goToSettings { settings ->
                        // confirm memberships and invitations have been updated
                        settings.memberships.map { it.teamName }
                            .shouldContainExactlyInAnyOrder(member.team.teamName, otherMember.team.teamName)

                        settings.invitations.shouldBeEmpty()
                    }
            }
        }
    }
}

