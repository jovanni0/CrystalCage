package dev.jovanni0.repositories


import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.filesystem.AssetCategory
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.filesystem.FilenameBuilder
import dev.jovanni0.records.edition.EditionRow
import dev.jovanni0.tables.tenant.CoverTable
import dev.jovanni0.tables.tenant.EditionTable
import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.setIfPresent
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import kotlin.uuid.Uuid



class EditionRepository(val edition_storage: AssetStorage)
{
    fun findById(schema_name: String, id: Uuid): EditionRow?
    {
        return tenantTransaction(schema_name)
        {
            EditionTable
                .selectAll()
                .where { EditionTable.id eq id }
                .map {
                    EditionRow(
                        id = it[EditionTable.id],
                        book_id = it[EditionTable.book_id],
                        label = it[EditionTable.label],
                        release_date = it[EditionTable.release_date],
                        format = it[EditionTable.format],
                        added_at = it[EditionTable.added_at],
                    )
                }
                .firstOrNull()
        }
    }

    fun findAllByBookId(schema_name: String, book_id: Uuid): List<EditionRow>
    {
        return tenantTransaction(schema_name)
        {
            EditionTable
                .selectAll()
                .where { EditionTable.book_id eq book_id }
                .orderBy(EditionTable.added_at, SortOrder.ASC)
                .map {
                    EditionRow(
                        id = it[EditionTable.id],
                        book_id = it[EditionTable.book_id],
                        label = it[EditionTable.label],
                        release_date = it[EditionTable.release_date],
                        format = it[EditionTable.format],
                        added_at = it[EditionTable.added_at],
                    )
                }
        }
    }


    fun create(
        schema_name: String,
        book_id: Uuid,
        label: String?,
        release_date: LocalDate?,
        format: String,
        filename: String,
        file: ByteArray,
        book_context: BookFilenameContext,
    ): EditionRow
    {
        var stored_filename: String? = null

        try
        {
            return tenantTransaction(schema_name)
            {
                val extension = filename.substringAfterLast('.', "")

                val insert_statement = EditionTable.insert {
                    it[EditionTable.book_id] = book_id
                    it[EditionTable.label] = label
                    it[EditionTable.release_date] = release_date
                    it[EditionTable.added_at] = LocalDate.now()
                    it[EditionTable.format] = format
                }

                val computed_filename = FilenameBuilder.build(
                    book_context,
                    AssetCategory.EDITION,
                    label,
                    insert_statement[EditionTable.id],
                    extension,
                )

                edition_storage.store(schema_name, AssetCategory.EDITION, computed_filename, file.inputStream())
                stored_filename = computed_filename

                EditionRow(
                    id = insert_statement[EditionTable.id],
                    book_id = insert_statement[EditionTable.book_id],
                    label = insert_statement[EditionTable.label],
                    release_date = insert_statement[EditionTable.release_date],
                    format = insert_statement[EditionTable.format],
                    added_at = insert_statement[EditionTable.added_at],
                )
            }
        }
        catch (e: Exception)
        {
            if (stored_filename != null)
            {
                edition_storage.delete(schema_name, AssetCategory.EDITION, stored_filename)
            }

            throw e
        }
    }


    fun update(schema_name: String, id: Uuid, label: Patch<String>, release_date: Patch<LocalDate>, format: Patch<String>): EditionRow?
    {
        // `format` is not nullable in the DB. this check needs to exist until `Patch` is updated to not set every value nullable.
        if (format is Patch.Value && format.value == null)
        {
            return null
        }

        if (label is Patch.Undefined && release_date is Patch.Undefined && format is Patch.Undefined)
        {
            return null
        }

        return tenantTransaction(schema_name)
        {
            EditionTable.update({ EditionTable.id eq id })
            {
                it.setIfPresent(EditionTable.label, label)
                it.setIfPresent(EditionTable.release_date, release_date)
                it.setIfPresent(EditionTable.format, format)
            }

            findById(schema_name, id)
        }
    }


    fun delete(schema_name: String, id: Uuid): Boolean
    {
        return tenantTransaction(schema_name)
        {
            EditionTable.deleteWhere { EditionTable.id eq id } == 1
        }
    }


    fun exists(schema_name: String, id: Uuid): Boolean
    {
        return tenantTransaction(schema_name)
        {
            EditionTable
                .selectAll()
                .where { EditionTable.id eq id }
                .empty()
                .not()
        }
    }
}
