package com.github.cf.discord.uni.pg

import com.github.cf.discord.uni.pg.op.JsonbArrayContainsOp
import com.github.cf.discord.uni.pg.type.JsonbArrayColumnType
import com.github.cf.discord.uni.pg.type.JsonbMapColumnType
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryParameter
import org.jetbrains.exposed.sql.Table

// TODO(?): Refactor to take a JSONArray instead of Array
/**
 * JsonbArray column
 * @param[String] name Name of the column
 * @return[Column<Array<V>>]
 */
fun<V> Table.jsonbArray(name: String) = registerColumn<Array<V>>(name, JsonbArrayColumnType<V>())

/**
 * JsonbMap (or dict) column
 * @param[String] name Name of the column
 * @return[Column<Map<K, V>>]
 */
fun<K, V> Table.jsonbMap(name: String) = registerColumn<Map<K, V>>(name, JsonbMapColumnType<K, V>())
/**
 * Check if a jsonb array contains something
 * @param[T] t Item
 * @param[IColumnType] sqlType Type of the iteam
 * @return[JsonbArrayContainsOp]
 */
inline fun<reified T> ExpressionWithColumnType<Array<T>>.jsonbSingleArrayContains(
        t: T,
        sqlType /* TODO: Find a better way to do this */: IColumnType
) = JsonbArrayContainsOp(this, QueryParameter(t, sqlType))