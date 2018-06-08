package com.github.cf.discord.uni.db.schema

import org.jetbrains.exposed.sql.Table

object Tags : Table() {
    val tagName = varchar("name", 15)
            .uniqueIndex()
            .primaryKey()
    val ownerId = long("ownerId")
    val tagContent = varchar("content", 2000)
}