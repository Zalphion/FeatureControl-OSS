package com.zalphion.featurecontrol.users

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.idp1Email2
import com.zalphion.featurecontrol.idp1Email3
import com.zalphion.featurecontrol.idp2Email1
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class UserStorageTest: CoreTestDriver() {

    private val user1 = User( idp1Email1.toUserId(), "user1", null)
        .also(core.users::plusAssign)
    private val user2 = User(idp1Email2.toUserId(), "user2", null)
        .also(core.users::plusAssign)
    private val user3 = User( idp1Email3.toUserId(), "user3", null)
        .also(core.users::plusAssign)
    private val user4 = User(idp2Email1.toUserId(), "user4", null)
        .also(core.users::plusAssign)

    @Test
    fun `get user - found`() {
        core.users[user1.userId] shouldBe user1
    }

    @Test
    fun `get user - not found`() {
        core.users[EmailAddress.parse("missing@foo.com").toUserId()].shouldBeNull()
    }

    @Test
    fun `get user by email - found`() {
        core.users[user4.emailAddress] shouldBe user4
    }

    @Test
    fun `get user by email - not found`() {
        core.users[EmailAddress.of("not@found.com")].shouldBeNull()
    }

    @Test
    fun `update user`() {
        core.users += user2.copy(
            userName = "user1",
        )
        core.users[user2.userId] shouldBe user2.copy(
            userName = "user1",
        )
    }
}