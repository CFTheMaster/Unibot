package com.github.cf.discord.uni.database.schema

import org.jetbrains.exposed.sql.Table

object ModLogs  : Table() {
    val messageId = long("messageId")
            .uniqueIndex()
            .primaryKey()
    val modId = long("modId")
    val guildId = long("guildId")
    val targetId = long("userId")
    val caseId = integer("caseId")
    val type = varchar("type", 10)
    val reason = varchar("reason", 512)
}