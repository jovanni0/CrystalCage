package dev.jovanni0.repositories

import dev.jovanni0.database.transactions.sharedTransaction
import dev.jovanni0.records.UserRecord
import dev.jovanni0.tables.User
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid



class UserRepository
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
                        id = row[User.id].toString(),
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
                        id = row[User.id].toString(),
                        username = row[User.username],
                        password_hash = row[User.password_hash],
                        role = row[User.role],
                        created_at = row[User.created_at]
                    )
                }
                .singleOrNull()
        }
    }
}
