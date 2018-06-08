package com.github.cf.discord.uni.db.schema

import org.jetbrains.exposed.sql.Table

object Restrictions : Table() {
    val guildId = long("guildId").nullable()
    val userId = long("userId").nullable()

    val everyone = bool("everyone")
    val global = bool("global")
    val command = varchar("command", 50)
    val reason = varchar("reason", 1000)
}