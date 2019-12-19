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
package com.github.cf.discord.uni.exposed

import com.github.cf.discord.uni.exposed.func.ArrayAppend
import com.github.cf.discord.uni.exposed.func.ArrayCat
import com.github.cf.discord.uni.exposed.op.ArrayContainedByOp
import com.github.cf.discord.uni.exposed.op.ArrayContainsOp
import com.github.cf.discord.uni.exposed.type.ArrayColumnType
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
