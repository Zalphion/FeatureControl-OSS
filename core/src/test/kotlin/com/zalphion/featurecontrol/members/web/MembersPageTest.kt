package com.zalphion.featurecontrol.members.web

import com.microsoft.playwright.BrowserContext
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.IDP1
import com.zalphion.featurecontrol.addTo
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.idp1Email2
import com.zalphion.featurecontrol.idp1Email3
import com.zalphion.featurecontrol.idp1Email4
import com.zalphion.featurecontrol.idp2Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.members.InviteUser
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.web.asUser
import com.zalphion.featurecontrol.web.playwright
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class MembersPageTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member1 = users.create(idp1Email1, userName = "lolcats").shouldBeSuccess()

    private val member2 = users.create(idp1Email2, userName = "user2")
        .shouldBeSuccess()
        .user.addTo(core, member1.team)

    private val member3 = users.create(idp1Email3)
        .shouldBeSuccess()
        .user.addTo(core, member1.team)

    private val invitation = InviteUser(
        teamId = member1.team.teamId,
        sender = member1.user.userId,
        data = MemberCreateData(idp2Email1, emptyMap())
    )
        .invoke(member1.user, core)
        .shouldBeSuccess()

    @Test
    fun `show members`(context: BrowserContext) {
        context.asUser(core, member1.user) { page ->
            page.mainNavBar.openTeams().manageTeam { page ->
                page.members.map { it.emailAddress }.shouldContainExactlyInAnyOrder(
                    member1.user.emailAddress,
                    member2.userId.toEmailAddress(),
                    member3.userId.toEmailAddress(),
                    invitation.user.emailAddress
                )

                page.members.find { it.emailAddress == member1.user.emailAddress } shouldNotBeNull {
                    username shouldBe "lolcats"
                    active shouldBe true
                    expires shouldBe null
                }

                page.members.find { it.emailAddress == member2.userId.toEmailAddress() } shouldNotBeNull {
                    username shouldBe "user2"
                    active shouldBe true
                    expires shouldBe null
                }

                page.members.find { it.emailAddress == member3.userId.toEmailAddress() } shouldNotBeNull {
                    username shouldBe null
                    active shouldBe true
                    expires shouldBe null
                }

                page.members.find { it.emailAddress == invitation.user.emailAddress } shouldNotBeNull {
                    username shouldBe null
                    active shouldBe false
                    expires shouldBe time + invitationRetention
                }
            }

        }
    }

    @Test
    fun `search members`(context: BrowserContext) {
        context.asUser(core, member1.user) { page ->
            page.mainNavBar.openTeams().manageTeam { page ->
                page.searchTerm = IDP1

                page.members.map { it.emailAddress }.shouldContainExactlyInAnyOrder(
                    idp1Email1, idp1Email2, idp1Email3
                )
            }
        }
    }

    @Test
    fun `invite member`(context: BrowserContext) {
        context.asUser(core, member1.user) { page ->
            page.mainNavBar.openTeams().manageTeam { page ->
                page.inviteMember { form ->
                    form.emailAddress = idp1Email4
                }.send { result ->
                    result.members.find { it.emailAddress == idp1Email4 } shouldNotBeNull {
                        username shouldBe null
                        active shouldBe false
                        expires shouldBe time + invitationRetention
                    }
                }
            }
        }
    }
}