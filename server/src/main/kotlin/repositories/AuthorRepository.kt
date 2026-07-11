package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.AuthorRecord
import dev.jovanni0.tables.Author
import org.jetbrains.exposed.v1.jdbc.selectAll



class AuthorRepository
{
    fun findAll(tenant_schema: String): List<AuthorRecord>
    {
        return tenantTransaction(tenant_schema)
        {
            Author
                .selectAll()
                .map {
                    AuthorRecord(
                        id = it[Author.id].toString(),
                        first_name = it[Author.first_name],
                        last_name = it[Author.last_name]
                    )
                }
        }
    }
}
