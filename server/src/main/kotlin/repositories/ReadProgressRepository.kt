package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.exceptions.not_found.ReadRecordNotFoundException
import dev.jovanni0.records.ReadProgressRecord
import dev.jovanni0.tables.tenant.ReadProgressTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.math.RoundingMode
import java.time.OffsetDateTime
import kotlin.uuid.Uuid

class ReadProgressRepository(
    private val read_record_repository: ReadRecordRepository
)
{
    fun findAllByReadId(schema_name: String, read_id: Uuid): List<ReadProgressRecord>
    {
        return tenantTransaction(schema_name)
        {
            val target_read = read_record_repository.findById(schema_name, read_id)

            if (target_read == null)
            {
                throw ReadRecordNotFoundException(read_id.toString())
            }

            ReadProgressTable
                .selectAll()
                .where { ReadProgressTable.read_record_id eq read_id }
                .orderBy(ReadProgressTable.recorded_at)
                .map {
                    ReadProgressRecord(
                        id = it[ReadProgressTable.id],
                        read_record_id = it[ReadProgressTable.read_record_id],
                        recorded_at = it[ReadProgressTable.recorded_at],
                        progress = it[ReadProgressTable.progress],
                    )
                }
        }
    }


    fun create(schema_name: String, read_record_id: Uuid, progress: Double): ReadProgressRecord
    {
        return tenantTransaction(schema_name)
        {
            val target_read = read_record_repository.findById(schema_name, read_record_id)

            if (target_read == null)
            {
                throw ReadRecordNotFoundException(read_record_id.toString())
            }

            val insert_statement = ReadProgressTable.insert {
                it[ReadProgressTable.read_record_id] = read_record_id
                it[ReadProgressTable.recorded_at] = OffsetDateTime.now()
                it[ReadProgressTable.progress] = progress.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            }

            ReadProgressRecord(
                id = insert_statement[ReadProgressTable.id],
                read_record_id = insert_statement[ReadProgressTable.read_record_id],
                recorded_at = insert_statement[ReadProgressTable.recorded_at],
                progress = insert_statement[ReadProgressTable.progress],
            )
        }
    }
}