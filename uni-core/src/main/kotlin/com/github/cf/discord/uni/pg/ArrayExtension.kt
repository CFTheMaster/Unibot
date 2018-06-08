
package com.github.cf.discord.uni.pg

import com.github.cf.discord.uni.pg.func.ArrayAppend
import com.github.cf.discord.uni.pg.func.ArrayCat
import com.github.cf.discord.uni.pg.op.ArrayContainedByOp
import com.github.cf.discord.uni.pg.op.ArrayContainsOp
import com.github.cf.discord.uni.pg.type.ArrayColumnType
import org.jetbrains.exposed.sql.*

fun<T> Table.pgArray(name: String, baseType: String) = registerColumn<Array<T>>(name, ArrayColumnType<T>(baseType))
fun<T> ExpressionWithColumnType<Array<T>>.append(e: Expression<T>) = ArrayAppend(columnType, this, e)
fun<T> ExpressionWithColumnType<Array<T>>.append(e: T, baseType: IColumnType) = append(QueryParameter(e, baseType))
fun<T> ExpressionWithColumnType<Array<T>>.cat(e: Expression<Array<T>>) = ArrayCat(columnType, this, e) /* TODO: Figure out a use for this, maybe synthetic pg array make fn? */
fun<T> ExpressionWithColumnType<Array<T>>.cat(e: Array<T>, baseType: IColumnType) = cat(QueryParameter(e, baseType))
inline fun<reified T> ExpressionWithColumnType<Array<T>>.containsAll(
        t: Expression<Array<T>>
) = ArrayContainsOp(this, t)
inline fun<reified T> ExpressionWithColumnType<Array<T>>.containsAll(
        t: Array<T>,
        baseType: String
) = containsAll(QueryParameter(t, ArrayColumnType<T>(baseType)))
inline fun<reified T> ExpressionWithColumnType<Array<T>>.contains(
        t: T,
        baseType: String
) = containsAll(arrayOf(t), baseType)
inline fun<reified T> ExpressionWithColumnType<Array<T>>.isContainedBy(
        t: Expression<Array<T>>
) = ArrayContainedByOp(this, t)
inline fun<reified T> ExpressionWithColumnType<Array<T>>.isContainedBy(
        t: Array<T>,
        baseType: String
) = isContainedBy(QueryParameter(t, ArrayColumnType<T>(baseType)))