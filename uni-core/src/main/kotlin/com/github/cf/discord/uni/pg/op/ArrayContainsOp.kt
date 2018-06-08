package com.github.cf.discord.uni.pg.op

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder

class ArrayContainsOp(private val expr1: Expression<Array<*>>, private val expr2: Expression<*>) : Op<Boolean>() {
    override fun toSQL(queryBuilder: QueryBuilder): String {
        // TODO: This is bad
        return "${expr1.toSQL(queryBuilder)} @> ${expr2.toSQL(queryBuilder)}"
    }
}