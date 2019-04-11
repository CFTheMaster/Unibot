package com.github.cf.discord.uni.exposed.func

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function

class ArrayDims<T>(private val arr: Expression<Array<T>>) : Function<StringColumnType>() {
    override val columnType = StringColumnType()

    override fun toSQL(queryBuilder: QueryBuilder): String {
        return "array_dims(${arr.toSQL(queryBuilder)})"
    }
}