/*
 *   Copyright (C) 2017-2019 computerfreaker
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

import com.github.cf.discord.uni.exposed.jsonbArray
import com.github.cf.discord.uni.exposed.pgArray
import org.jetbrains.exposed.sql.Table
import org.json.JSONObject

object Logs : Table() {
    val event = varchar("event", 6)

    // Message
    val messageId = long("messageId")
    val content = varchar("content", 2000)
    val attachments = pgArray<String>("attachments", "text")
    val embeds = jsonbArray<JSONObject>("embeds")
    val timestamp = long("timestamp")

    // Author
    val authorId = long("authorId")
    val authorName = varchar("authorName", 33)
    val authorDiscrim = varchar("authorDiscrim", 4)
    val authorAvatar = text("authorAvatar").nullable()
    val authorNick = varchar("authorNick", 33)

    // Guild
    val guildId = long("guildId")
    val guildName = varchar("guildName", 100)

    // Channel
    val channelId = long("channelId")
    val channelName = varchar("channelName", 100)
}
