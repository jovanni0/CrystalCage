package dev.jovanni0.repositories

import dev.jovanni0.data_types.UserRole
import dev.jovanni0.database.transactions.sharedTransaction
import dev.jovanni0.exceptions.InvalidInviteCodeException
import dev.jovanni0.records.UserRecord
import dev.jovanni0.tables.InviteCodeTable
import dev.jovanni0.tables.User
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid



class UserRepository(
    private val invite_code_repo: InviteCodeRepository,
)
{
    /**
     * finds a record ny username.
     */
    fun findByUsername(username: String): UserRecord?
    {
        return sharedTransaction {
            User
                .selectAll()
                .where { User.username eq username}
                .map { row ->
                    UserRecord(
                        id = row[User.id],
                        username = row[User.username],
                        password_hash = row[User.password_hash],
                        role = row[User.role],
                        created_at = row[User.created_at]
                    )
                }
                .singleOrNull()
        }
    }


    /**
     * find a record by id.
     */
    fun findById(id: String): UserRecord?
    {
        return sharedTransaction {
            User
                .selectAll()
                .where { User.id eq Uuid.parse(id) }
                .map{ row ->
                    UserRecord(
                        id = row[User.id],
                        username = row[User.username],
                        password_hash = row[User.password_hash],
                        role = row[User.role],
                        created_at = row[User.created_at]
                    )
                }
                .singleOrNull()
        }
    }


    fun create(code: String, username: String, password_hash: String, role: UserRole): UserRecord?
    {
        return sharedTransaction {
            val username_exists = User
                .selectAll()
                .where { User.username eq username }
                .any()

            if (username_exists)
            {
                return@sharedTransaction null
            }

            val is_code_valid = invite_code_repo.isValid(code)

            if (!is_code_valid) return@sharedTransaction null

            val insert_statement = User.insert {
                it[User.username] = username
                it[User.password_hash] = password_hash
                it[User.role] = role.toString()
            }

            val consumed_rows = InviteCodeTable.update({
                (InviteCodeTable.code eq code) and
                InviteCodeTable.used_by.isNull() and
                (InviteCodeTable.expires_at.isNull() or (InviteCodeTable.expires_at greater CurrentTimestampWithTimeZone))
            })
            {
                it[InviteCodeTable.used_by] = insert_statement[User.id]
                it[InviteCodeTable.used_at] = CurrentTimestampWithTimeZone
            }

            if (consumed_rows == 0)
            {
                throw InvalidInviteCodeException("code ${code} is not valid for insertion")
            }

            UserRecord(
                id = insert_statement[User.id],
                username = insert_statement[User.username],
                password_hash = insert_statement[User.password_hash],
                role = insert_statement[User.role],
                created_at = insert_statement[User.created_at],
            )
        }
    }


    fun delete(id: Uuid)
    {
        return sharedTransaction {
            User.deleteWhere { User.id eq id }
        }
    }
}
