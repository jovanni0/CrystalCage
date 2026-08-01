package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.exceptions.AuthorNotFoundException
import dev.jovanni0.records.universe.UniverseRow
import dev.jovanni0.tables.tenant.AuthorTable
import dev.jovanni0.tables.tenant.UniverseTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid



class UniverseRepository
{
    fun exists(schema_name: String, universe_id: Uuid): Boolean
    {
        return tenantTransaction(schema_name)
        {
            UniverseTable
                .selectAll()
                .where { UniverseTable.id eq universe_id }
                .empty()
                .not()
        }
    }


    fun findAllByAuthorId(schema_name: String, author_id: Uuid): List<UniverseRow>
    {
        return tenantTransaction(schema_name)
        {
            UniverseTable
                .selectAll()
                .where { UniverseTable.author_id eq author_id }
                .orderBy(UniverseTable.name, SortOrder.ASC)
                .map {
                    UniverseRow(
                        id = it[UniverseTable.id],
                        author_id = it[UniverseTable.author_id],
                        name = it[UniverseTable.name]
                    )
                }
        }
    }


    fun findById(schema_name: String, universe_id: Uuid): UniverseRow?
    {
        return tenantTransaction(schema_name)
        {
            UniverseTable
                .selectAll()
                .where { UniverseTable.id eq universe_id }
                .map {
                    UniverseRow(
                        id = it[UniverseTable.id],
                        author_id = it[UniverseTable.author_id],
                        name = it[UniverseTable.name]
                    )
                }
                .firstOrNull()
        }
    }


    fun create(schema_name: String, name: String, author_id: Uuid): UniverseRow
    {
        return tenantTransaction(schema_name)
        {
            val author_exists = AuthorTable
                .selectAll()
                .where { AuthorTable.id eq author_id }
                .empty()
                .not()

            if (!author_exists)
            {
                throw AuthorNotFoundException(author_id.toString())
            }

            val insert_statement = UniverseTable.insert {
                it[UniverseTable.author_id] = author_id
                it[UniverseTable.name] = name
            }

            UniverseRow(
                id = insert_statement[UniverseTable.id],
                author_id = insert_statement[UniverseTable.author_id],
                name = insert_statement[UniverseTable.name]
            )
        }
    }
}
