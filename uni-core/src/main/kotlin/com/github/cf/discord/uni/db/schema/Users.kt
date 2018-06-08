package com.github.cf.discord.uni.db.schema

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = long("id")
            .uniqueIndex()
            .primaryKey()
    val username = varchar("username", 33)
    val discriminator = varchar("discriminator", 4)
    val lang = varchar("lang", 5)
    val marriedUserId = long("marriedUserId")
            .nullable()
}