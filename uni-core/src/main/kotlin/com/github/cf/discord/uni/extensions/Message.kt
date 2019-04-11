package com.github.cf.discord.uni.extensions

import com.github.cf.discord.uni.Uni.Companion.pool
import com.github.cf.discord.uni.database.schema.Logs
import com.google.gson.Gson
import org.jetbrains.exposed.sql.insert
import net.dv8tion.jda.core.entities.Message
import org.json.JSONObject

fun Message.log(ev: String = "CREATE") {
    val gson = Gson()

    asyncTransaction(pool) {
        Logs.insert {
            it[event] =  ev
            it[messageId] = idLong
            it[content] = contentRaw
            it[attachments] = this@log.attachments.map { it.url }.toTypedArray()
            it[embeds] = this@log.embeds.map { JSONObject(gson.toJson(it)) }.toTypedArray()
            it[timestamp] = this@log.creationTime.toInstant().toEpochMilli() //.getLong(ChronoField.MILLI_OF_SECOND)
            it[authorId] = author.idLong
            it[authorName] = author.name
            it[authorDiscrim] = author.discriminator
            it[authorAvatar] = author.avatarUrl
            it[authorNick] = member?.nickname ?: ""
            it[guildId] = guild.idLong
            it[guildName] = guild.name
            it[channelId] = channel.idLong
            it[channelName] = channel.name
        }
    }.execute().get()
}