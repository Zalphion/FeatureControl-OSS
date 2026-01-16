package com.zalphion.featurecontrol.teams

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.addTo
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.forbidden
import com.zalphion.featurecontrol.getMyTeam
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.idp1Email2
import com.zalphion.featurecontrol.idp2Email1
import com.zalphion.featurecontrol.memberAlreadyExists
import com.zalphion.featurecontrol.memberNotFound
import com.zalphion.featurecontrol.members.CreateMember
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.members.RemoveMember
import com.zalphion.featurecontrol.members.UpdateMember
import com.zalphion.featurecontrol.members.UserRole
import com.zalphion.featurecontrol.setRole
import dev.andrewohara.utils.pagination.Page
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration

class TeamServiceTest: CoreTestDriver() {

    @Test
    fun `remove member - not admin`() {
        val myUser = users.create(idp1Email1).shouldBeSuccess().user
        val (myMember, _, _, myTeam) = myUser.getMyTeam(core).shouldNotBeNull()
        myMember.setRole(core, UserRole.Developer)

        val otherUser = users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core, myTeam)

        RemoveMember(otherMember.teamId, otherMember.userId)
            .invoke(myUser, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `remove member - team you don't have access to`() {
        val myUser = users.create(idp1Email1).shouldBeSuccess().user

        val otherOrgUser = users.create(idp2Email1).shouldBeSuccess().user
        val (otherOrg, _) = otherOrgUser.getMyTeam(core).shouldNotBeNull()

        RemoveMember(otherOrg.teamId, otherOrgUser.userId)
            .invoke(myUser, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `remove member - success`() {
        val (myMember, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()

        val otherUser = users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core, myTeam)

        RemoveMember(teamId = otherMember.teamId, userId = otherMember.userId)
            .invoke(myUser, core)
            .shouldBeSuccess(
                MemberDetails(
                    member = otherMember,
                    team = myTeam,
                    user = otherUser,
                    invitedBy = null
                )
            )

        core.members.list(myTeam.teamId, 100).toList()
            .shouldContainExactlyInAnyOrder(myMember)
    }

    @Test
    fun `invite user - success as admin`() {
        val (_, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()
        val otherUser = users.create(idp1Email2).shouldBeSuccess().user

        val memberDetails = CreateMember(myTeam.teamId, myUser.userId, otherUser.emailAddress, UserRole.Developer)
            .invoke(myUser, core)
            .shouldBeSuccess()

        memberDetails shouldBe MemberDetails(
            member = Member(
                teamId = myTeam.teamId,
                userId = otherUser.userId,
                role = UserRole.Developer,
                invitedBy = myUser.userId,
                invitationExpiresOn = time + Duration.ofDays(1),
            ),
            user = otherUser,
            team = myTeam,
            invitedBy = myUser
        )

        core.members.list(myTeam.teamId, 100)
            .shouldContain(memberDetails.member)
    }

    @Test
    fun `invite user - forbidden as non-admin`() {
        val (myMember, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()
        myMember.setRole(core, UserRole.Developer)

        CreateMember(myTeam.teamId, myUser.userId, idp1Email2, UserRole.Developer)
            .invoke(myUser, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `invite user - cannot invite self`() {
        val (myMember, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()

        CreateMember(myTeam.teamId, myUser.userId, myUser.emailAddress, UserRole.Admin)
            .invoke(myUser, core)
            .shouldBeFailure(memberAlreadyExists(myMember))
    }

    @Test
    fun `invite user - already a member`() {
        val (_, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()

        val otherUser = users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core, myTeam)

        CreateMember(myTeam.teamId, myUser.userId, otherUser.emailAddress, UserRole.Admin)
            .invoke(myUser, core)
            .shouldBeFailure(memberAlreadyExists(otherMember))
    }

    @Test
    fun `update member - cannot change own`() {
        val (member, user, _, _) = users.create(idp1Email1).shouldBeSuccess()

        UpdateMember(teamId = member.teamId, member.userId, UserRole.Developer)
            .invoke(user, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `update member - in other team`() {
        val myUser = users.create(idp1Email1).shouldBeSuccess().user
        val otherMember = users.create(idp2Email1).shouldBeSuccess().member

        UpdateMember(otherMember.teamId, otherMember.userId, UserRole.Developer)
            .invoke(myUser, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `update member - member not found`() {
        val (_, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()
        val otherUser = users.create(idp1Email2).shouldBeSuccess().user

        UpdateMember(myTeam.teamId, otherUser.userId, UserRole.Developer)
            .invoke(myUser, core)
            .shouldBeFailure(memberNotFound(myTeam.teamId, otherUser.userId))
    }

    @Test
    fun `update member - success as admin`() {
        val (myMember, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()

        val otherUser = users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core, myTeam, UserRole.Tester)

        time += Duration.ofMinutes(10)
        val expected = otherMember.copy(
            role = UserRole.Developer,
        )

        UpdateMember(otherMember.teamId, otherMember.userId, UserRole.Developer)
            .invoke(myUser, core)
            .shouldBeSuccess(expected)

        core.members.list(myTeam.teamId, 100).toList()
            .shouldContainExactlyInAnyOrder(myMember, expected)
    }

    @Test
    fun `update member - forbidden as non-admin`() {
        val (myMember, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()
        myMember.setRole(core, UserRole.Developer)

        val otherUser = users.create(idp1Email2).shouldBeSuccess().user
        val otherMember = otherUser.addTo(core,myTeam, role = UserRole.Tester)

        UpdateMember(myTeam.teamId, otherMember.userId, UserRole.Developer)
            .invoke(myUser, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `create team`() {
        val (myMember, myUser, _, myTeam) = users.create(idp1Email1).shouldBeSuccess()

        val otherTeam = CreateTeam(myUser.userId, TeamCreateUpdateData(TeamName.of("Other Team")))
            .invoke(myUser, core)
            .shouldBeSuccess()

        core.teams[myTeam.teamId] shouldBe myTeam
        core.teams[otherTeam.teamId] shouldBe otherTeam

        core.members.list(myUser.userId, 100)[null] shouldBe Page(
            items = listOf(
                Member(
                    teamId = otherTeam.teamId,
                    userId = myUser.userId,
                    role = UserRole.Admin,
                    invitedBy = null,
                    invitationExpiresOn = null
                ),
                myMember
            ),
            next = null
        )
    }
}