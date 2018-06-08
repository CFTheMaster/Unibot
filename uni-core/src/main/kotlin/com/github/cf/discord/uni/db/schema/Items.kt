package com.github.cf.discord.uni.db.schema

import org.jetbrains.exposed.sql.Table

object Items : Table() {
    val id = varchar("id", 20)
            .primaryKey()
            .uniqueIndex()
    val type = varchar("type", 20)
    val description = varchar("description", 500)
    val content = varchar("item", 50)
    val price = integer("price")
}