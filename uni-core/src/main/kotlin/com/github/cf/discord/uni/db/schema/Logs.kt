package com.github.cf.discord.uni.db.schema

import com.github.cf.discord.uni.pg.jsonbArray
import com.github.cf.discord.uni.pg.pgArray
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
    val authorAvatar = text("authorAvatar")
    val authorNick = varchar("authorNick", 33)

    // Guild
    val guildId = long("guildId")
    val guildName = varchar("guildName", 100)

    // Channel
    val channelId = long("channelId")
    val channelName = varchar("channelName", 100)
}