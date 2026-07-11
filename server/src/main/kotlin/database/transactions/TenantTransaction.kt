package dev.jovanni0.database.transactions

import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction



/**
 * wrapper over Exposed `transaction`, as described in Server Issue 1.
 */
fun <TypeT> tenantTransaction(schema_name: String, block: Transaction.() -> TypeT): TypeT
{
    return transaction {
        exec("SET search_path TO \"$schema_name\"")
        block()
    }
}
