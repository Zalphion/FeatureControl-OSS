package com.zalphion.featurecontrol.teams

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.addTo
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.forbidden
import com.zalphion.featurecontrol.getMyTeam
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.idp1Email2
import com.zalphion.featurecontrol.idp2Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.memberAlreadyExists
import com.zalphion.featurecontrol.memberNotFound
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.members.MemberUpdateData
import dev.andrewohara.utils.pagination.Page
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TeamServiceTest: CoreTestDriver() {

    @Test
    fun `remove member - forbidden`() {
        val myUser = core.users.create(idp1Email1).shouldBeSuccess().user

        val otherOrgUser = core.users.create(idp2Email1).shouldBeSuccess().user
        val (otherOrg, _) = otherOrgUser.getMyTeam(core).shouldNotBeNull()

        core.members.remove(otherOrg.teamId, otherOrgUser.userId)
            .invoke(myUser, app)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `remove member - success`() {
        val (myMember, myUser, myTeam) = core.users.create(idp1Email1).shouldBeSuccess()

        val otherUser = core.users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core, myTeam)

        core.members.remove(teamId = otherMember.teamId, userId = otherMember.userId)
            .invoke(myUser, app)
            .shouldBeSuccess(MemberDetails(otherMember, otherUser, myTeam))

        core.memberStorage.list(myTeam.teamId).toList()
            .shouldContainExactlyInAnyOrder(myMember)
    }

    @Test
    fun `invite user - success`() {
        val (_, myUser, myTeam) = core.users.create(idp1Email1).shouldBeSuccess()
        val otherUser = core.users.create(idp1Email2).shouldBeSuccess().user

        val memberDetails = core.members.invite(
            teamId = myTeam.teamId,
            sender = myUser.userId,
            data = MemberCreateData(otherUser.emailAddress, emptyMap())
        )
            .invoke(myUser, app)
            .shouldBeSuccess()

        memberDetails shouldBe MemberDetails(
            member = Member(
                teamId = myTeam.teamId,
                userId = otherUser.userId,
                invitedBy = myUser.userId,
                invitationExpiresOn = time + invitationRetention,
                extensions = emptyMap()
            ),
            user = otherUser,
            team = myTeam
        )

        core.memberStorage.list(myTeam.teamId)
            .shouldContain(memberDetails.member)
    }

    @Test
    fun `invite user - forbidden`() {
        val (_, myUser, _) = core.users.create(idp1Email1).shouldBeSuccess()
        val otherTeam = core.users.create(idp1Email2).shouldBeSuccess().team

        core.members.invite(otherTeam.teamId, myUser.userId, MemberCreateData(idp1Email2, emptyMap()))
            .invoke(myUser, app)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `invite user - cannot invite self`() {
        val (myMember, myUser, myTeam) = core.users.create(idp1Email1).shouldBeSuccess()

        core.members.invite(myTeam.teamId, myUser.userId, MemberCreateData(myUser.emailAddress, emptyMap()))
            .invoke(myUser, app)
            .shouldBeFailure(memberAlreadyExists(myMember))
    }

    @Test
    fun `invite user - already a member`() {
        val (_, myUser, myTeam) = core.users.create(idp1Email1).shouldBeSuccess()

        val otherUser = core.users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core, myTeam)

        core.members.invite(myTeam.teamId, myUser.userId, MemberCreateData(otherUser.emailAddress, emptyMap()))
            .invoke(myUser, app)
            .shouldBeFailure(memberAlreadyExists(otherMember))
    }

    @Test
    fun `update member - cannot change own`() {
        val (member, user, _) = core.users.create(idp1Email1).shouldBeSuccess()

        core.members.update(teamId = member.teamId, member.userId, MemberUpdateData((mapOf("foo" to "bar"))))
            .invoke(user, app)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `update member - in other team`() {
        val myUser = core.users.create(idp1Email1).shouldBeSuccess().user
        val otherMember = core.users.create(idp2Email1).shouldBeSuccess().member

        core.members.update(otherMember.teamId, otherMember.userId, MemberUpdateData(mapOf("foo" to "bar")))
            .invoke(myUser, app)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `update member - member not found`() {
        val (_, myUser, myTeam) = core.users.create(idp1Email1).shouldBeSuccess()
        val otherUser = core.users.create(idp1Email2).shouldBeSuccess().user

        core.members.update(myTeam.teamId, otherUser.userId, MemberUpdateData(mapOf("foo" to "bar")))
            .invoke(myUser, app)
            .shouldBeFailure(memberNotFound(myTeam.teamId, otherUser.userId))
    }

    @Test
    fun `update member - success`() {
        val (myMember, myUser, myTeam) = core.users.create(idp1Email1).shouldBeSuccess()

        val otherUser = core.users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core, myTeam)
        val expected = otherMember.copy(extensions = mapOf("foo" to "bar"))

        core.members.update(otherMember.teamId, otherMember.userId, MemberUpdateData(mapOf("foo" to "bar")))
            .invoke(myUser, app)
            .shouldBeSuccess(expected)

        core.memberStorage.list(myTeam.teamId).toList()
            .shouldContainExactlyInAnyOrder(myMember, expected)
    }

    @Test
    fun `create team`() {
        val (myMember, myUser, myTeam) = core.users.create(idp1Email1).shouldBeSuccess()

        val otherTeam = core.teams.create(myUser.userId, TeamCreateUpdateData(TeamName.of("Other Team")))
            .invoke(myUser, app)
            .shouldBeSuccess()

        core.teamStorage[myTeam.teamId] shouldBe myTeam
        core.teamStorage[otherTeam.teamId] shouldBe otherTeam

        core.memberStorage.list(myUser.userId)[null] shouldBe Page(
            items = listOf(
                Member(
                    teamId = otherTeam.teamId,
                    userId = myUser.userId,
                    invitedBy = null,
                    invitationExpiresOn = null,
                    extensions = emptyMap()
                ),
                myMember
            ),
            next = null
        )
    }
}