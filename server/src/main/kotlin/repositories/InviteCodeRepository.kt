package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.sharedTransaction
import dev.jovanni0.tables.InviteCodeTable
import io.ktor.util.toLowerCasePreservingASCIIRules
import io.ktor.util.toUpperCasePreservingASCIIRules
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.time.OffsetDateTime
import kotlin.random.Random
import kotlin.uuid.Uuid


class InviteCodeRepository
{
    private val alphabet = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"

    private fun getRandomCode(): String
    {
        return (1..8)
            .map { alphabet[Random.nextInt(alphabet.length)] }
            .joinToString("")
    }


    fun isValid(code: String): Boolean
    {
        return sharedTransaction {
            InviteCodeTable
                .selectAll()
                .where {
                    (InviteCodeTable.code eq code) and
                    (InviteCodeTable.expires_at.isNull() or (InviteCodeTable.expires_at greater CurrentTimestampWithTimeZone)) and
                    InviteCodeTable.used_by.isNull()
                }
                .empty()
                .not()
        }
    }


    fun generate(created_by: Uuid, expires_at: OffsetDateTime? = null, max_attempts: Int = 5): String?
    {
        return sharedTransaction {
            repeat(max_attempts)
            {
                val candidate = getRandomCode()

                val already_exists = InviteCodeTable
                    .selectAll()
                    .where { InviteCodeTable.code eq candidate }
                    .any()

                if (!already_exists)
                {
                    InviteCodeTable.insert {
                        it[code] = candidate
                        it[InviteCodeTable.created_by] = created_by
                        it[InviteCodeTable.created_at] = CurrentTimestampWithTimeZone
                        it[InviteCodeTable.expires_at] = expires_at
                    }

                    return@sharedTransaction candidate
                }
            }

            return@sharedTransaction null
        }
    }
}