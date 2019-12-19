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
package com.github.cf.discord.uni.exposed.op

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder

class JsonbArrayContainsOp(private val expr1: Expression<Array<*>>, private val expr2: Expression<*>) : Op<Boolean>() {
    override fun toSQL(queryBuilder: QueryBuilder): String {
        // TODO: This is bad
        return "${expr1.toSQL(queryBuilder)}::jsonb ?? ${expr2.toSQL(queryBuilder)}"
    }
}
