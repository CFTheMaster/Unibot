package com.github.cf.discord.uni.pg.func

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder

class ArrayCat<T>(
        override val columnType: IColumnType,
        private val arr1: Expression<Array<T>>,
        private val arr2: Expression<Array<T>>
) : Function<Array<T>>() {
    override fun toSQL(queryBuilder: QueryBuilder): String {
        return "array_cat(${arr1.toSQL(queryBuilder)}, ${arr2.toSQL(queryBuilder)})"
    }
}