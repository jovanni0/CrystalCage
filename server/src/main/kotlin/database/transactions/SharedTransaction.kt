package dev.jovanni0.database.transactions

import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction



/**
 *
 */
fun <TypeT> sharedTransaction(block: Transaction.() -> TypeT): TypeT
{
    return transaction {
        exec("SET search_path TO public")
        block()
    }
}
