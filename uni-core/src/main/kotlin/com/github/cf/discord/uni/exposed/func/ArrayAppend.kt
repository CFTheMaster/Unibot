package com.github.cf.discord.uni.exposed.func

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder

class ArrayAppend<T>(
        override val columnType: IColumnType,
        private val arr: Expression<Array<T>>,
        private val e: Expression<*>
) : Function<Array<T>>() {
    override fun toSQL(queryBuilder: QueryBuilder): String {
        return "array_append(${arr.toSQL(queryBuilder)}, ${e.toSQL(queryBuilder)})"
    }
}