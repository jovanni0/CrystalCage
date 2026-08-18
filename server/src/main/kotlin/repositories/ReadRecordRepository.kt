package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.exceptions.ConfigurationException
import dev.jovanni0.exceptions.ConflictException
import dev.jovanni0.records.read_record.ReadRecordRow
import dev.jovanni0.tables.tenant.ReadRecordTable
import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.Update
import dev.jovanni0.utils.setIfPresent
import dev.jovanni0.utils.updateIfPresent
import dev.jovanni0.utils.validateHistoricalTransaction
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.time.LocalDate
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid



class ReadRecordRepository
{
    fun findAllByBookId(schema_name: String, book_id: Uuid): List<ReadRecordRow>
    {
        return tenantTransaction(schema_name)
        {
            ReadRecordTable
                .selectAll()
                .where { ReadRecordTable.book_id eq book_id }
                .orderBy(ReadRecordTable.start_date, SortOrder.DESC)
                .map {
                    ReadRecordRow(
                        id = it[ReadRecordTable.id],
                        book_id = it[ReadRecordTable.book_id],
                        edition_id = it[ReadRecordTable.edition_id],
                        start_date = it[ReadRecordTable.start_date],
                        end_date = it[ReadRecordTable.end_date],
                        did_not_finish = it[ReadRecordTable.did_not_finish],
                        is_historical = it[ReadRecordTable.is_historical],
                        stop_position = it[ReadRecordTable.stop_position],
                        notes = it[ReadRecordTable.notes],
                    )
                }
        }
    }


    fun findById(schema_name: String, id: Uuid): ReadRecordRow?
    {
        return tenantTransaction(schema_name)
        {
            ReadRecordTable
                .selectAll()
                .where { ReadRecordTable.id eq id }
                .map {
                    ReadRecordRow(
                        id = it[ReadRecordTable.id],
                        book_id = it[ReadRecordTable.book_id],
                        edition_id = it[ReadRecordTable.edition_id],
                        start_date = it[ReadRecordTable.start_date],
                        end_date = it[ReadRecordTable.end_date],
                        did_not_finish = it[ReadRecordTable.did_not_finish],
                        is_historical = it[ReadRecordTable.is_historical],
                        stop_position = it[ReadRecordTable.stop_position],
                        notes = it[ReadRecordTable.notes],
                    )
                }
                .firstOrNull()
        }
    }

    fun create(
        schema_name: String,
        book_id: Uuid,
        edition_id: Uuid?,
        start_date: LocalDate?,
        end_date: LocalDate?,
        did_not_finish: Boolean,
        is_historical: Boolean,
        stop_position: String?,
        notes: String?
    ): ReadRecordRow
    {
        return tenantTransaction(schema_name)
        {
            // enforce one historic read per book before the insert hits the DB, as per `docs/validation-rules##read-records`.
            // the race window between the SELECT and INSERT is covered by the DB-level check.
            // TODO: the SQL exception from DB should be mapped to a propper HTTP status code.
            if (is_historical)
            {
                val has_historical = ReadRecordTable
                    .selectAll()
                    .where { (ReadRecordTable.book_id eq book_id) and (ReadRecordTable.is_historical eq true) }
                    .empty()
                    .not()

                if (has_historical)
                {
                    throw ConflictException("book $book_id already has a historical read")
                }

                if (start_date != null || end_date != null)
                {
                    throw ConflictException("historical record cannot have start_date/end_date set")
                }
            }

            val insert_statement = ReadRecordTable.insert {
                it[ReadRecordTable.book_id] = book_id
                it[ReadRecordTable.edition_id] = edition_id
                it[ReadRecordTable.start_date] = start_date
                it[ReadRecordTable.end_date] = end_date
                it[ReadRecordTable.did_not_finish] = did_not_finish
                it[ReadRecordTable.is_historical] = is_historical
                it[ReadRecordTable.stop_position] = stop_position
                it[ReadRecordTable.notes] = notes
            }

            ReadRecordRow(
                id = insert_statement[ReadRecordTable.id],
                book_id = insert_statement[ReadRecordTable.book_id],
                edition_id = insert_statement[ReadRecordTable.edition_id],
                start_date = insert_statement[ReadRecordTable.start_date],
                end_date = insert_statement[ReadRecordTable.end_date],
                did_not_finish = insert_statement[ReadRecordTable.did_not_finish],
                is_historical = insert_statement[ReadRecordTable.is_historical],
                stop_position = insert_statement[ReadRecordTable.stop_position],
                notes = insert_statement[ReadRecordTable.notes],
            )
        }
    }


    fun update(
        schema_name: String,
        id: Uuid,
        edition_id: Update<Uuid?>,
        start_date: Update<LocalDate?>,
        end_date: Update<LocalDate?>,
        did_not_finish: Update<Boolean>,
        is_historical: Update<Boolean>,
        stop_position: Update<String?>,
        notes: Update<String?>,
    ): Int?
    {
        val can_update = edition_id !is Update.Undefined ||
            start_date !is Update.Undefined ||
            end_date !is Update.Undefined ||
            did_not_finish !is Update.Undefined ||
            is_historical !is Update.Undefined ||
            stop_position !is Update.Undefined ||
            notes !is Update.Undefined

        if (!can_update)
        {
            return null
        }

        return tenantTransaction(schema_name)
        {

            val record = ReadRecordTable
                .selectAll()
                .where { ReadRecordTable.id eq id }
                .firstOrNull()
                ?: return@tenantTransaction 0

            val book_id = record[ReadRecordTable.book_id]
            val current_start_date = record[ReadRecordTable.start_date]
            val current_end_date = record[ReadRecordTable.end_date]
            val current_is_historical = record[ReadRecordTable.is_historical]

            val effective_start_date = when(start_date)
            {
                is Update.Undefined -> current_start_date
                is Update.Value -> start_date.value
            }
            val effective_end_date = when(end_date)
            {
                is Update.Undefined -> current_end_date
                is Update.Value -> end_date.value
            }

            val is_historical_patch = when (is_historical) {
                is Update.Undefined -> Patch.Undefined
                is Update.Value -> Patch.Value(is_historical.value)
            }
            val validation_error = validateHistoricalTransaction(is_historical_patch, current_is_historical, effective_start_date, effective_end_date)

            if (validation_error != null)
            {
                throw ConflictException(validation_error)
            }

            // check for conflict because there is already a historical record.
            if (is_historical is Update.Value && is_historical.value == true)
            {
                val has_historical = ReadRecordTable
                    .selectAll()
                    .where {
                        (ReadRecordTable.book_id eq book_id) and
                        (ReadRecordTable.is_historical eq true) and
                        (ReadRecordTable.id neq id)
                    }
                    .empty()
                    .not()

                if (has_historical)
                {
                    throw ConflictException("book $book_id already has a historical read")
                }
            }

            ReadRecordTable.update({ ReadRecordTable.id eq  id })
            {
                it.updateIfPresent(ReadRecordTable.edition_id, edition_id)
                it.updateIfPresent(ReadRecordTable.start_date, start_date)
                it.updateIfPresent(ReadRecordTable.end_date, end_date)
                it.updateIfPresent(ReadRecordTable.did_not_finish, did_not_finish)
                it.updateIfPresent(ReadRecordTable.is_historical, is_historical)
                it.updateIfPresent(ReadRecordTable.stop_position, stop_position)
                it.updateIfPresent(ReadRecordTable.notes, notes)
            }
        }
    }


    fun delete(schema_name: String, id: Uuid): Int
    {
        return tenantTransaction(schema_name)
        {
            ReadRecordTable.deleteWhere { ReadRecordTable.id eq id }
        }
    }
}
