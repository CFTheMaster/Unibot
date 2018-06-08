package com.github.cf.discord.uni.pg.type

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