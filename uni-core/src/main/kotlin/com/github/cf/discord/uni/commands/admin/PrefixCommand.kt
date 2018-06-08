package com.github.cf.discord.uni.commands.admin

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.async.asyncTransaction
import com.github.cf.discord.uni.db.schema.*
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.utils.I18n
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.*

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class AddPrefix : Command() {
    override val guildOnly = true
    override val desc = "Add a prefix"

    override fun run(ctx: Context) {
        val prefix = ctx.args["prefix"] as String

        asyncTransaction(Uni.pool) {
            val guild = Guilds.select {
                Guilds.id.eq(ctx.guild!!.idLong)
            }.first()

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    it[prefixes] = guild[Guilds.prefixes] + prefix
                }
                ctx.send(
                        I18n.parse(
                                ctx.lang.getString("prefix_added"),
                                mapOf("prefix" to prefix)
                        )
                )
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }.execute()
    }
}

@Perm(Permission.MANAGE_SERVER)
@Argument("prefix", "string")
class RemPrefix : Command() {
    override val guildOnly = true
    override val desc = "Remove a prefix"

    override fun run(ctx: Context) {
        val prefix = ctx.args["prefix"] as String

        asyncTransaction(Uni.pool) {
            if (ctx.storedGuild!!.prefixes.isEmpty()) {
                return@asyncTransaction ctx.send(ctx.lang.getString("remove_no_prefix"))
            }

            try {
                Guilds.update({
                    Guilds.id.eq(ctx.guild!!.idLong)
                }) {
                    val list = ctx.storedGuild.prefixes.toMutableList()
                    list.remove(prefix)
                    it[prefixes] = list.toTypedArray()
                }
                ctx.send(
                        I18n.parse(
                                ctx.lang.getString("prefix_removed"),
                                mapOf("prefix" to prefix)
                        )
                )
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }.execute()
    }
}

@Load
class Prefix : Command(){
    override val guildOnly = true
    override val desc = "Add, view or delete the guild's prefixes"

    init {
        addSubcommand(AddPrefix(), "add")
        addSubcommand(RemPrefix(), "remove")
    }

    override fun run(ctx: Context)
            = ctx.send(
            I18n.parse(
                    ctx.lang.getString("current_prefixes"),
                    mapOf("prefixes" to if (ctx.storedGuild!!.prefixes.isEmpty()) "none" else ctx.storedGuild.prefixes.joinToString(", "))
            )
    )
}