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
package com.github.cf.discord.uni.database.schema

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = long("userId")
            .uniqueIndex()
            .primaryKey()
    val expPoints = long("expPoints")
    val level = long("level")
    val lastLevelUp =  datetime("lastLevelUp")
    val accountCreationDate = datetime("accountCreationDate")
    val lastMessage = long("lastMsg")
    val customPrefix = varchar("customPrefix", 20)
            .nullable()
    val localExp = long("localExp")
    val localLevel = long("localLevel")
}
