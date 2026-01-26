package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.memory
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId
import dev.andrewohara.utils.pagination.Page
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MemberStorageTest: CoreTestDriver(storageDriver = StorageDriver.memory(PageSize.of(2))) {

    private val team1 = TeamId.of("team0001")
    private val team2 = TeamId.of("team0002")
    private val team3 = TeamId.of("team0003")

    private val user1 = UserId.of("user0001")
    private val user2 = UserId.of("user0002")
    private val user3 = UserId.of("user0003")
    private val user4 = UserId.of("user0004")

    private val testObj = core.members

    @Test
    fun `get - found`() {
        val member1 = createMember(team1, user1)
        val member2 = createMember(team1, user2)

        testObj[team1, user1] shouldBe member1
        testObj[team1, user2] shouldBe member2
    }

    @Test
    fun `get - not found`() {
        testObj[team1, user1] shouldBe null
    }

    @Test
    fun update() {
        val member = createMember(team1, user1, UserRole.Developer)
        val updated = member.copy(role = UserRole.Admin)
        testObj += updated

        testObj[team1, user1] shouldBe updated
    }

    @Test
    fun `list team members`() {
        val member1 = createMember(team1, user1)
        val member2 = createMember(team1, user2)
        val member3 = createMember(team1, user3)
        val member4 = createMember(team2, user4)
        val member5 = createMember(team2, user1)

        testObj.list(team1)[null] shouldBe Page(
            items = listOf(member1, member2),
            next = member2.userId
        )

        testObj.list(team1)[member2.userId] shouldBe Page(
            items = listOf(member3),
            next = null
        )

        testObj.list(team2)[null] shouldBe Page(
            items = listOf(member5, member4),
            next = null
        )
    }

    @Test
    fun `list teams`() {
        val member1 = createMember(team1, user1)
        val member2 = createMember(team1, user2)
        val member3 = createMember(team1, user3)
        val member4 = createMember(team2, user4)
        val member5 = createMember(team2, user1)
        val member6 = createMember(team2, user2)
        val member7 = createMember(team3, user1)

        testObj.list(user1)[null] shouldBe Page(
            items = listOf(member1, member5),
            next = member5.teamId
        )

        testObj.list(user1)[member5.teamId] shouldBe Page(
            items = listOf(member7),
            next = null
        )

        testObj.list(user2)[null] shouldBe Page(
            items = listOf(member2, member6),
            next = null
        )

        testObj.list(user3)[null] shouldBe Page(
            items = listOf(member3),
            next = null
        )

        testObj.list(user4)[null] shouldBe Page(
            items = listOf(member4),
            next = null
        )
    }

    @Test
    fun `delete - found`() {
        val member1 = createMember(team1, user1)
        val member2 = createMember(team1, user2)

        testObj -= member1
        testObj.list(team1).toList()
            .shouldContainExactlyInAnyOrder(member2)
    }

    @Test
    fun `delete - not found`() {
        val member1 = createMember(team1, user1)
        val member2 = createMember(team1, user2)

        testObj -= member1
        testObj -= member1

        testObj.list(team1).toList()
            .shouldContainExactlyInAnyOrder(member2)
    }

    private fun createMember(
        teamId: TeamId,
        userId: UserId,
        role: UserRole = UserRole.Developer
    ) = Member(
        teamId = teamId,
        userId = userId,
        role = role,
        invitedBy = null,
        invitationExpiresOn = null
    ).also(testObj::plusAssign)
}