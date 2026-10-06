package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.TagCategoryRecord
import dev.jovanni0.tables.tenant.TagCategoryTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid



class TagCategoryRepository
{
    fun findAll(schema_name: String): List<TagCategoryRecord>
    {
        return tenantTransaction(schema_name)
        {
            TagCategoryTable
                .selectAll()
                .orderBy(TagCategoryTable.name)
                .map {
                    TagCategoryRecord(
                        id = it[TagCategoryTable.id],
                        name = it[TagCategoryTable.name]
                    )
                }
        }
    }


    fun create(schema_name: String, name: String): TagCategoryRecord
    {
        return tenantTransaction(schema_name)
        {
            val insert_query = TagCategoryTable.insert {
                it[TagCategoryTable.name] = name
            }

            TagCategoryRecord(
                id = insert_query[TagCategoryTable.id],
                name = insert_query[TagCategoryTable.name],
            )
        }
    }


    fun update(schema_name: String, id: Uuid, name: String): TagCategoryRecord?
    {
        return tenantTransaction(schema_name)
        {
            TagCategoryTable.update({ TagCategoryTable.id eq id })
            {
                it[TagCategoryTable.name] = name
            }

            TagCategoryTable
                .selectAll()
                .where { TagCategoryTable.id eq id }
                .map {
                    TagCategoryRecord(
                        id = it[TagCategoryTable.id],
                        name = it[TagCategoryTable.name],
                    )
                }
                .firstOrNull()
        }
    }



    fun delete(schema_name: String, id: Uuid): Int
    {
        return tenantTransaction(schema_name)
        {
            TagCategoryTable.deleteWhere {
                TagCategoryTable.id eq id
            }
        }
    }
}