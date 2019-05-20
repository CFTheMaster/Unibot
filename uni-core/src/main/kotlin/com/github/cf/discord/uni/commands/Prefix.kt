package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.database.schema.Guilds
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import net.dv8tion.jda.core.Permission
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
                ctx.send("Guild prefix changed $prefixes")
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

    override fun run(ctx: Context) {
        val prefixes = (ctx.args["prefix"] as String).toLowerCase().replace("remove ", "").toByteArray()
        val encode = Base64.getEncoder().encodeToString(prefixes)

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
                ctx.send("Guild prefix has been removed $prefixes")
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

        ctx.send("current prefix ${
        if(ctx.storedGuild!!.prefix.isNullOrEmpty()) "none"
        else ctx.storedGuild.prefix }")
    }
}