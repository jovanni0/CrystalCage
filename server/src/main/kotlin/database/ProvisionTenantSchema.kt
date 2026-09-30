package dev.jovanni0.database

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import javax.sql.DataSource



fun provisionTenantSchema(data_source: DataSource, user_id: String)
{
    val schema_name = "user_$user_id"
    transaction {
        exec("CREATE SCHEMA \"$schema_name\"")
    }

    Flyway
        .configure()
        .dataSource(data_source)
        .schemas(schema_name)
        .locations("classpath:db/migration/tenant")
        .load()
        .migrate()
}
