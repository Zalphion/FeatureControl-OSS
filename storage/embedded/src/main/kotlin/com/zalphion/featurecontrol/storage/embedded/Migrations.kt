package com.zalphion.featurecontrol.storage.embedded

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.migration.Context
import org.flywaydb.core.api.migration.JavaMigration
import org.h2.jdbcx.JdbcConnectionPool

internal fun JdbcConnectionPool.migrate() {
    Flyway.configure()
        .dataSource(this@migrate)
        .javaMigrations(V1CreateMonoTable())
        .createSchemas(true)
        .load()
        .migrate()
}

private class V1CreateMonoTable: JavaMigration {
    override fun getVersion(): MigrationVersion = MigrationVersion.fromVersion("1")
    override fun getDescription() = "Create mono table"
    override fun getChecksum() = sql.hashCode()
    override fun canExecuteInTransaction() = true

    private val sql = """CREATE TABLE $TABLE_NAME (
    $COLLECTION_NAME VARCHAR(256) NOT NULL,
    $GROUP_ID_COLUMN VARCHAR(256) NOT NULL,
    $ITEM_ID_COLUMN VARCHAR(256) NOT NULL,
    $DOC_COLUMN TEXT NOT NULL,
    PRIMARY KEY ($COLLECTION_NAME, $GROUP_ID_COLUMN, $ITEM_ID_COLUMN)
);

CREATE INDEX idx_inverse
    ON $TABLE_NAME ($COLLECTION_NAME, $ITEM_ID_COLUMN, $GROUP_ID_COLUMN);"""

    override fun migrate(context: Context) {
        context.connection.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }
}