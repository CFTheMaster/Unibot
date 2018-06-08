package com.github.cf.discord.uni.db.schema

import org.jetbrains.exposed.sql.Table

object Reminders : Table() {
    val userId = long("userId")
    val channelId = long("channelId")
    val timestamp = long("timestamp")
    val reminder = varchar("reminder", 2000)
}