package com.zalphion.featurecontrol.users

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamName
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

class UserServiceTest: CoreTestDriver() {

    @Test
    fun `create user - success`() {
        val expected = MemberDetails(
            member = Member(
                userId = idp1Email1.toUserId(),
                teamId = TeamId.of("ds3rjam1"),
                invitedBy = null,
                invitationExpiresOn = null,
                extensions = emptyMap()
            ),
            user = User(
                userId = idp1Email1.toUserId(),
                userName = "User One",
                photoUrl = Uri.of("http://photo.url"),
            ),
            team = Team(
                teamId = TeamId.of("ds3rjam1"),
                teamName = TeamName.of("User One's Team"),
            )
        )

        users.create(
            UserCreateData(
                emailAddress = idp1Email1,
                userName = "User One",
                photoUrl = Uri.of("http://photo.url"),
            )
        ) shouldBeSuccess expected

        core.users[expected.user.userId] shouldBe expected.user
        core.members[expected.member.teamId, expected.member.userId] shouldBe expected.member
        core.teams[expected.member.teamId] shouldBe expected.team
    }

    @Test
    fun `create user - already exists`() {
        users.create(idp1Email1).shouldBeSuccess()
        users.create(idp1Email1) shouldBeFailure userAlreadyExists(idp1Email1)
    }
}