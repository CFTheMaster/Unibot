package com.github.cf.discord.uni.db.schema

import com.github.cf.discord.uni.pg.jsonbMap
import com.github.cf.discord.uni.pg.pgArray
import org.jetbrains.exposed.sql.Table

object Guilds : Table() {
    val id = long("id")
            .uniqueIndex()
            .primaryKey()
    val name = varchar("name", 100)
    val lang = varchar("lang", 5)
    val prefixes = pgArray<String>("prefixes", "varchar")
    val forceLang = bool("forceLang")
    val starboard = bool("starboard")
    val starboardChannel = long("starboardChannel")
            .nullable()
    val logs = bool("logs")
    val modlogs = bool("modlogs")
    val modlogChannel = long("modlogChannel")
            .nullable()
    val rolemeRoles = jsonbMap<String, Long>("rolemeRoles")
    val welcome = bool("welcome")
    val welcomeChannel = long("welcomeChannel")
            .nullable()
    val welcomeMessage = varchar("welcomeMessage", 2000)
    val leaveMessage = varchar("leaveMessage", 2000)
    val ignoredChannels = pgArray<Long>("ignoredChannels", "BIGINT")
    val levelMessages = bool("levelMessages")
    val mutedRole = long("mutedRole")
            .nullable()
    val antiInvite = bool("antiInvite")
}