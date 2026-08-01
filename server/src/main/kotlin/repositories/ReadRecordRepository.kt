package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.records.read_record.ReadRecordRow
import dev.jovanni0.tables.tenant.ReadRecordTable
import java.time.LocalDate
import org.jetbrains.exposed.v1.jdbc.insert
import kotlin.uuid.Uuid



class ReadRecordRepository
{
    /**
     * TODO: temporary implementation for use in unit tests. needs to be expanded.
     */
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
    ) {
        return tenantTransaction(schema_name)
        {
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
}
