package com.github.cf.discord.uni.db.schema

import org.jetbrains.exposed.sql.Table

object Scripts : Table() {
    val script = varchar("script", 2000)
    val scriptName = varchar("scriptName", 50)
    val ownerId = long("ownerId")
    val guildId = long("guildId")
    val ownerOnly = bool("ownerOnly")
}