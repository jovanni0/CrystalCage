package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.tags.TagBookRow
import dev.jovanni0.records.tags.TagCountRow
import dev.jovanni0.records.tags.TagDetailRow
import dev.jovanni0.records.tags.TagRow
import dev.jovanni0.tables.tenant.BookTable
import dev.jovanni0.tables.tenant.BookTagTable
import dev.jovanni0.tables.tenant.TagTable
import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.setIfPresent
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid



class TagRepository
{
    fun findAll(schema_name: String): List<TagCountRow>
    {
        return tenantTransaction(schema_name)
        {
            val count = BookTagTable.book_id.count()

            TagTable
                .join(
                    BookTagTable,
                    JoinType.LEFT,
                    onColumn = TagTable.id,
                    otherColumn = BookTagTable.tag_id
                )
                .select(TagTable.id, TagTable.name, TagTable.description, count)
                .groupBy(TagTable.id, TagTable.name, TagTable.description)
                .orderBy(TagTable.name, SortOrder.ASC)
                .map {
                    TagCountRow(
                        id = it[TagTable.id],
                        name = it[TagTable.name],
                        description = it[TagTable.description],
                        book_count = it[count].toInt(),
                    )
                }
        }
    }


    fun findById(schema_name: String, id: Uuid): TagDetailRow?
    {
        return tenantTransaction(schema_name)
        {
            val rows = TagTable
                .join(
                    BookTagTable,
                    JoinType.LEFT,
                    onColumn = TagTable.id,
                    otherColumn = BookTagTable.tag_id
                )
                .join(
                    BookTable,
                    JoinType.LEFT,
                    onColumn = BookTagTable.book_id,
                    otherColumn = BookTable.id
                )
                .select(TagTable.id, TagTable.name, TagTable.description, BookTable.id, BookTable.title, BookTable.series_id, BookTable.cover_id)
                .where { TagTable.id eq id }
                .toList()

            if (rows.isEmpty())
                return@tenantTransaction null

            val tag_row = rows.first()

            val books = rows
                .filter { it.getOrNull(BookTable.id) != null }
                .map {
                    TagBookRow(
                        id = it[BookTable.id],
                        title = it[BookTable.title],
                        series_id = it[BookTable.series_id],
                        cover_id = it[BookTable.cover_id],
                    )
                }

            TagDetailRow(
                id = tag_row[TagTable.id],
                name = tag_row[TagTable.name],
                description = tag_row[TagTable.description],
                books = books,
            )
        }
    }


    fun create(schema_name: String, name: String, description: String?): TagRow
    {
        return tenantTransaction(schema_name)
        {
            val insert_query = TagTable.insert {
                it[TagTable.name] = name
                it[TagTable.description] = description
            }

            TagRow(
                id = insert_query[TagTable.id],
                name = insert_query[TagTable.name],
                description = insert_query[TagTable.description]
            )
        }
    }


    fun update(schema_name: String, id: Uuid, name: Patch<String>, description: Patch<String>, added_book_ids: List<Uuid>, removed_book_ids: List<Uuid>)
    {
        tenantTransaction(schema_name)
        {
            if (name !is Patch.Undefined || description !is Patch.Undefined)
            {
                TagTable.update({ TagTable.id eq id })
                {
                    it.setIfPresent(TagTable.name, name)
                    it.setIfPresent(TagTable.description, description)
                }
            }

            if (added_book_ids.isNotEmpty())
            {
                BookTagTable.batchInsert(added_book_ids, ignore = true)
                { book_id ->
                    this[BookTagTable.tag_id] = id
                    this[BookTagTable.book_id] = book_id
                }
            }

            if (removed_book_ids.isNotEmpty())
            {
                BookTagTable.deleteWhere {
                    (BookTagTable.tag_id eq id) and (BookTagTable.book_id inList removed_book_ids)
                }
            }
        }
    }


    fun delete(schema_name: String, id: Uuid): Int
    {
        return tenantTransaction(schema_name)
        {
            TagTable.deleteWhere {
                TagTable.id eq id
            }
        }
    }
}
