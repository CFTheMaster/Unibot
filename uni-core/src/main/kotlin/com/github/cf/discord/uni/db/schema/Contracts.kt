package com.github.cf.discord.uni.db.schema

import com.github.cf.discord.uni.pg.jsonbArray
import org.jetbrains.exposed.sql.Table

object Contracts : Table() {
    val userId = long("userId")
            .primaryKey()
            .uniqueIndex()
    val date = date("date")
    val wish = varchar("wish", 2000)
    val level = integer("level")
    val experience = integer("experience")
    val gem = varchar("gem", 20)
    val corruption = integer("corruption")
    val inventory = jsonbArray<String>("inventory")
    val balance = integer("balance")
}