package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.filesystem.AssetCategory
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.filesystem.FilenameBuilder
import dev.jovanni0.records.cover.CoverRow
import dev.jovanni0.tables.tenant.BookTable
import dev.jovanni0.tables.tenant.CoverTable
import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.setIfPresent
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.booleanLiteral
import org.jetbrains.exposed.v1.core.case
import org.jetbrains.exposed.v1.core.coalesce
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import kotlin.uuid.Uuid



class CoverRepository(val cover_storage: AssetStorage)
{
    fun findAllByBookId(schema_name: String, book_id: Uuid): List<CoverRow>
    {
        return tenantTransaction(schema_name)
        {
            val is_default = case()
                .When(BookTable.cover_id eq CoverTable.id, booleanLiteral(true))
                .Else(booleanLiteral(false)).alias("is_default")

            CoverTable
                .join(
                    BookTable,
                    JoinType.LEFT,
                    CoverTable.id,
                    BookTable.cover_id
                )
                .select(CoverTable.id, CoverTable.book_id, CoverTable.label, CoverTable.added_at, is_default)
                .where { CoverTable.book_id eq book_id }
                .orderBy(CoverTable.added_at, SortOrder.DESC)
                .map {
                    CoverRow(
                        id = it[CoverTable.id],
                        book_id = it[CoverTable.book_id],
                        label = it[CoverTable.label],
                        added_at = it[CoverTable.added_at],
                        is_default = it[is_default]
                    )
                }
        }
    }


    fun findById(schema_name: String, id: Uuid): CoverRow?
    {
        return tenantTransaction(schema_name)
        {
            val is_default = case()
                .When(BookTable.cover_id eq CoverTable.id, booleanLiteral(true))
                .Else(booleanLiteral(false)).alias("is_default")

            CoverTable
                .join(
                    BookTable,
                    JoinType.LEFT,
                    CoverTable.id,
                    BookTable.cover_id
                )
                .select(CoverTable.id, CoverTable.book_id, CoverTable.label, CoverTable.added_at, is_default)
                .where { CoverTable.id eq id }
                .map {
                    CoverRow(
                        id = it[CoverTable.id],
                        book_id = it[CoverTable.book_id],
                        label = it[CoverTable.label],
                        added_at = it[CoverTable.added_at],
                        is_default = it[is_default]
                    )
                }
                .firstOrNull()
        }
    }


    fun create(
        schema_name: String,
        book_id: Uuid,
        label: String?,
        filename: String,
        file: ByteArray,
        book_context: BookFilenameContext,
    ): CoverRow
    {
        var stored_filename: String? = null

        try
        {
            return tenantTransaction(schema_name)
            {
                val extension = filename.substringAfterLast('.', "")

                val insert_statement = CoverTable.insert {
                    it[CoverTable.book_id] = book_id
                    it[CoverTable.label] = label
                    it[CoverTable.added_at] = LocalDate.now()
                }

                val computed_filename = FilenameBuilder.build(
                    book_context,
                    AssetCategory.COVER,
                    label,
                    insert_statement[CoverTable.id],
                    extension,
                )

                cover_storage.store(schema_name, AssetCategory.COVER, computed_filename, file.inputStream())
                stored_filename = computed_filename

                CoverRow(
                    id = insert_statement[CoverTable.id],
                    book_id = book_id,
                    label = label,
                    added_at = insert_statement[CoverTable.added_at],
                    is_default = false
                )
            }
        }
        catch (e: Exception)
        {
            if (stored_filename != null)
            {
                cover_storage.delete(schema_name, AssetCategory.COVER, stored_filename)
            }

            throw e
        }
    }


    fun update(schema_name: String, id: Uuid, label: Patch<String>)
    {
        if (label is Patch.Undefined)
        {
            return
        }

        tenantTransaction(schema_name)
        {
            CoverTable.update({ CoverTable.id eq id })
            {
                it.setIfPresent(CoverTable.label, label)
            }
        }
    }


    fun delete(schema_name: String, id: Uuid): Boolean
    {
        return tenantTransaction(schema_name)
        {
            val cover = CoverTable
                .selectAll()
                .where { CoverTable.id eq id }
                .firstOrNull()
                ?: return@tenantTransaction false

            val book_id = cover[CoverTable.book_id]

            val was_default = BookTable
                .select(BookTable.id)
                .where { (BookTable.cover_id eq id) and (BookTable.id eq book_id) }
                .any()

            if (was_default)
            {
                val replacement_id = CoverTable
                    .select(CoverTable.id)
                    .where { (CoverTable.book_id eq book_id) and (CoverTable.id neq id) }
                    .orderBy(CoverTable.added_at, SortOrder.DESC)
                    .firstOrNull()
                    ?.get(CoverTable.id)

                BookTable.update({ BookTable.id eq book_id })
                {
                    it[BookTable.cover_id] = replacement_id
                }
            }

            val rows_deleted = CoverTable.deleteWhere { CoverTable.id eq id }

            rows_deleted > 0
        }
    }
}
