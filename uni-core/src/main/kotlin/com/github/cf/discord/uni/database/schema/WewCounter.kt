package com.github.cf.discord.uni.database.schema

import org.jetbrains.exposed.sql.Table

object WewCounter : Table(){
    val amount = long("amount")
            .primaryKey()
}