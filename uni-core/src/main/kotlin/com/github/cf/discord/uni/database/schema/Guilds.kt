package com.github.cf.discord.uni.database.schema

import org.jetbrains.exposed.sql.Table

object Guilds : Table(){
    val id = long("id")
            .uniqueIndex()
            .primaryKey()
    val name = varchar("name", 300)
    val prefix = varchar("prefix", 20)
            .nullable()
    val starboard = bool("starboard")
    val starboardChannel = long("starboardChannel")
            .nullable()
    val logs = bool("logs")
    val modlogs = bool("modlogs")
    val modLogChannel = long("modlogChannel")
            .nullable()
    val welcome = bool("welcome")
    val welcomeChannel = long("welcomeChannel")
            .nullable()
    val welcomeMessage = varchar("welcomeMessage", 2000)
    val leaveMessage = varchar("leaveMessage", 2000)
    val levelMessages = bool("levelMessages")
    val mutedRole = long("mutedRole")
            .nullable()
    val antiInvite = bool("antiInvite")
    val userRole = bool("userRole")
    val autoRole = long("autoRole")
            .nullable()
}