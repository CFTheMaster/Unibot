package com.github.cf.discord.uni.database.schema

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = long("userId")
            .uniqueIndex()
            .primaryKey()
    val expPoints = long("expPoints")
    val level = long("level")
    val lastLevelUp =  datetime("lastLevelUp")
    val accountCreationDate = datetime("accountCreationDate")
    val lastMessage = long("lastMsg")
    val customPrefix = varchar("customPrefix", 20)
            .nullable()
}