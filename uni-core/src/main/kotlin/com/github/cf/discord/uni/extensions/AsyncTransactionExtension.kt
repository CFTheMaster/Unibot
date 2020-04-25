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
