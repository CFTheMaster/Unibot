/*
 *   Copyright (C) 2017-2021 computerfreaker
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
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
