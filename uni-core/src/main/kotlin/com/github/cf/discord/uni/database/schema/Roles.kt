package com.github.cf.discord.uni.database.schema

import org.jetbrains.exposed.sql.Table

object Roles : Table() {
    val roleId = long("roleId")
    val userId = long("userId")
    val guildId = long("guildId")
}