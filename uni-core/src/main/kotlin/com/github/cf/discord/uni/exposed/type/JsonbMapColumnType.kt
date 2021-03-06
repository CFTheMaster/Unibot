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
package com.github.cf.discord.uni.exposed.type


import org.jetbrains.exposed.sql.ColumnType
import org.json.JSONException
import org.json.JSONObject
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

/**
 * JsonbMap column type
 */
class JsonbMapColumnType<K, V> : ColumnType() {
    /**
     * SQL type string
     * @return[String] jsonb
     */
    override fun sqlType() = "jsonb"

    /**
     * Sets a statement parameter
     * @param[PreparedStatement] stmt Statement
     * @param[Int] index Index
     * @param[Any?] value Value, should be Map<K, V>
     */
    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val actualValue: String
        try {
            @Suppress("UNCHECKED_CAST")
            actualValue = JSONObject(value!! as Map<K, V>).toString()
        } catch (e: Throwable) {
            when (e) {
                is ClassCastException -> throw RuntimeException("value did not conform Map<K, V>")
                is NullPointerException -> throw RuntimeException("value cannot be null")
                else -> throw e
            }
        }
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = actualValue
        stmt.setObject(index, obj)
    }

    /**
     * Value from DB
     * @param[Any] value Value
     * @return[Map<K, V>]
     */
    override fun valueFromDB(value: Any): Any {
        if (value is PGobject) {
            try {
                @Suppress("UNCHECKED_CAST")
                return JSONObject(value.value).toMap() as Map<K, V>
            } catch (e: Throwable) {
                when (e) {
                    is ClassCastException -> throw RuntimeException("value did not conform Map<K, V>")
                    is JSONException -> throw RuntimeException("value is not valid jsonb")
                    else -> throw e
                }
            }
        }
        throw RuntimeException("value is not pgobject")
    }
}
