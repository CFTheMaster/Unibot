/*
 *   Copyright (C) 2017-2021 computerfreaker
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

import com.github.cf.discord.uni.exposed.pgArray
import org.jetbrains.exposed.sql.Table

object Starboard : Table() {
    val messageId = long("messageId")
            .uniqueIndex()
            .primaryKey()
    val guildId = long("guildId")
    val channelId = long("channelId")
    val starId = long("starId")
    val stargazers = pgArray<Long>("stargazers", "BIGINT")
    val content = varchar("content", 2000)
    val attachments = pgArray<String>("attachments", "text")
}
