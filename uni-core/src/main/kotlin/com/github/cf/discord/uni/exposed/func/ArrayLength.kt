/*
 *   Copyright (C) 2017-2020 computerfreaker
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
package com.github.cf.discord.uni.exposed.func

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.QueryBuilder

class ArrayLength<T>(private val arr: Expression<Array<T>>) : Function<IntegerColumnType>() {
    override val columnType = IntegerColumnType()
    override fun toSQL(queryBuilder: QueryBuilder): String {
        return "array_length(${arr.toSQL(queryBuilder)})"
    }
}
