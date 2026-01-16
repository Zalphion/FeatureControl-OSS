package com.zalphion.featurecontrol.storage

import org.http4k.core.Credentials
import org.http4k.core.Uri
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
class PostgresStorageTest: StorageContract(Storage.postgres(
    uri = Uri.of(postgres.jdbcUrl),
    credentials = Credentials(postgres.username, postgres.password)
)) {

    companion object {
        @Container
        @JvmStatic
        val postgres: JdbcDatabaseContainer<*> = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("feature-control")
            .withUsername("test")
            .withPassword("test")
    }
}