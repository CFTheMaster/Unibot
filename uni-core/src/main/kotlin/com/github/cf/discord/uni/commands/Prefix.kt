/*
 *   Copyright (C) 2017-2021 computerfreaker
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.database.schema.Guilds
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import net.dv8tion.jda.api.Permission
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.*

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class AddPrefix : Command(){
    override val guildOnly = true
    override val desc = "Add a prefix"

    override fun run(ctx: Context) {
        val prefixes = (ctx.args["prefix"] as String).toLowerCase().replace("add ", "").toByteArray()
        val encode = Base64.getEncoder().encodeToString(prefixes)

        val decoded = String(Base64.getDecoder().decode(encode))

        asyncTransaction(Uni.pool) {
            val guild = Guilds.select {
                Guilds.id.eq(ctx.guild!!.idLong)
            }.first()

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    it[prefix] = encode
                }
                ctx.send("Guild prefix changed $decoded")
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }.execute()
    }
}

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class RemPrefix : Command(){
    override val guildOnly = true
    override val desc = "Remove a prefix"
    override val cate = Category.MODERATION.name

    override fun run(ctx: Context) {
        val prefixes = (ctx.args["prefix"] as String).toLowerCase().replace("remove ", "").toByteArray()
        val encode = Base64.getEncoder().encodeToString(prefixes)

        val decoded = String(Base64.getDecoder().decode(encode))

        asyncTransaction(Uni.pool) {
            if (ctx.storedGuild!!.prefix!!.isEmpty()) {
                return@asyncTransaction ctx.send("No Guild prefix found!")
            }

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    val list = ctx.storedGuild.prefix

                    it[prefix] = list + encode
                }
                ctx.send("Guild prefix has been removed $decoded")
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }.execute()
    }
}

@Load
class Prefix : Command() {
    override val guildOnly = true
    override val desc = "Add, view or delete the guild's prefix"

    init {
        addSubcommand(AddPrefix(), "add")
        addSubcommand(RemPrefix(), "remove")
    }

    override fun run(ctx: Context){
        val decoded = String(Base64.getDecoder().decode(ctx.storedGuild?.prefix ?: ""))
        ctx.send("current prefix ${
        if(ctx.storedGuild!!.prefix.isNullOrEmpty()) "none"
        else decoded }")
    }
}
