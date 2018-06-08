package com.github.cf.discord.uni.pg.func

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