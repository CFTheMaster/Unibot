package com.github.cf.discord.uni.exposed.type

import org.jetbrains.exposed.sql.ColumnType
import org.json.JSONArray
import org.json.JSONException
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

/**
 * JsonbArray column type
 */
class JsonbArrayColumnType<V> : ColumnType() {
    /**
     * SQL type string
     * @return[String] jsonb
     */
    override fun sqlType() = "jsonb"

    /**
     * Sets a statement parameter
     * @param[PreparedStatement] stmt Statement
     * @param[Int] index Index
     * @param[Any?] value Value, should be Array<V>
     */
    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val actualValue: String
        try {
            @Suppress("UNCHECKED_CAST")
            actualValue = JSONArray(value!! as Array<V>).toString()
        } catch (e: Throwable) {
            when (e) {
                is ClassCastException -> throw RuntimeException("value did not conform Array<V>")
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
     * @return[Array<V>]
     */
    override fun valueFromDB(value: Any): Any {
        if (value is PGobject) {
            try {
                @Suppress("UNCHECKED_CAST")
                return JSONArray(value.value).toList().toTypedArray() as Array<V>
            } catch (e: Throwable) {
                when (e) {
                    is ClassCastException -> throw RuntimeException("value did not conform Array<V>")
                    is JSONException -> throw RuntimeException("value is not valid jsonb")
                    else -> throw e
                }
            }
        }
        throw RuntimeException("value is not pgobject")
    }
}