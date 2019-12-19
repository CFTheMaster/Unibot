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
package com.github.cf.discord.uni.exposed.type

import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.jdbc.PgArray
import java.sql.PreparedStatement

class ArrayColumnType<T>(private val valueType: String) : ColumnType() {
    override fun sqlType() = "$valueType[]"

    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val arr: Array<out T>
        try {
            @Suppress("UNCHECKED_CAST")
            arr = value as Array<out T>
        } catch (e: Throwable) {
            when (e) {
                is ClassCastException -> throw RuntimeException("value did not conform Array<T>")
                is NullPointerException -> throw RuntimeException("value cannot be null")
                else -> throw e
            }
        }
        stmt.setArray(index, stmt.connection.createArrayOf(valueType, arr))
    }

    override fun valueFromDB(value: Any): Any {
        if (value is PgArray) {
            try {
                @Suppress("UNCHECKED_CAST")
                return value.array as Array<T>
            } catch (e: ClassCastException) {
                throw RuntimeException("value did not conform Array<T>")
            }
        }
        throw RuntimeException("value is not pgarray")
    }
}
