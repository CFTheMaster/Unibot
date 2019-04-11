package com.github.cf.discord.uni.extensions

import org.jetbrains.exposed.sql.Transaction
import java.util.concurrent.ExecutorService

/**
 * Asynchronously perform a transaction
 * @param[ExecutorService] pool Thread pool to perform the transaction in
 * @param[Transaction.() -> T] body Transaction body
 * @return[AsyncTransaction<T>]
 */
fun<T> asyncTransaction(pool: ExecutorService, body: Transaction.() -> T): AsyncTransaction<T> {
    return AsyncTransaction(pool, body)
}