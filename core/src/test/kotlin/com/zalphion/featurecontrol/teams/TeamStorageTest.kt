package com.zalphion.featurecontrol.teams

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.StorageTestDriver
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TeamStorageTest: CoreTestDriver() {

    private val testObj = StorageTestDriver().create(TeamStorage)

    private val id1 = TeamId.of("team0001")
    private val id2 = TeamId.of("team0002")
    private val id3 = TeamId.of("team0003")

    @Test
    fun `get - not found`() {
        testObj[id1] shouldBe null
    }

    @Test
    fun `get - found`() {
        val team1 = createTeam(id1)
        val team2 = createTeam(id2)

        testObj[id1] shouldBe team1
        testObj[id2] shouldBe team2
    }

    @Test
    fun update() {
        val team = createTeam(id1)
        val updated = team.copy(teamName = TeamName.of("new name"))

        testObj += updated

        testObj[id1] shouldBe updated
    }

    @Test
    fun `batch get`() {
        val team1 = createTeam(id1)
        val team2 = createTeam(id2)

        testObj.batchGet(listOf(id1, id2, id3)).shouldContainExactlyInAnyOrder(team1, team2)
    }

    @Test
    fun `delete - found`() {
        val team1 = createTeam(id1)
        val team2 = createTeam(id2)

        testObj -= team1

        testObj[id1] shouldBe null
        testObj[id2] shouldBe team2
    }

    @Test
    fun `delete - not found`() {
        val team1 = createTeam(id1)
        val team2 = createTeam(id2)

        testObj -= team2
        testObj -= team2

        testObj[id1] shouldBe team1
        testObj[id2] shouldBe null
    }

    private fun createTeam(
        id: TeamId,
        name: TeamName = TeamName.of("team $id")
    ) = Team(
        teamId = id,
        teamName = name,
    ).also(testObj::plusAssign)
}