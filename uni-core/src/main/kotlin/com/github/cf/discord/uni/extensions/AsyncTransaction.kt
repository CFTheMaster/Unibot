package com.github.cf.discord.uni.extensions

import kotlinx.coroutines.future.await
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * Represents a transaction performed in a thread pool
 */
class AsyncTransaction<T>(private val pool: ExecutorService, private val body: Transaction.() -> T) {
    /**
     * Execute the transaction
     * @return[CompletableFuture<T>] Return value of the transaction
     */
    fun execute(): CompletableFuture<T> {
        val fut = CompletableFuture<T>()
        pool.execute {
            try {
                fut.complete(transaction {
                    body()
                })
            } catch (e: Throwable) {
                fut.completeExceptionally(e)
            }
        }
        return fut
    }

    /**
     * Execute and await the transaction
     * To be used with coroutines
     * @return[T] Return value of the transaction
     */
    suspend fun await(): T {
        return execute().await()
    }
}