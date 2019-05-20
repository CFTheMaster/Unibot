package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.database.schema.Users
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import org.jetbrains.exposed.sql.update
import java.util.*

@Argument("prefix", "string")
class AddUserPrefix : Command(){
    override val desc = "add user prefix"

    override fun run(ctx: Context) {
        val prefix = (ctx.args["prefix"] as String).toLowerCase().replace("add ", "").toByteArray()
        val encode = Base64.getEncoder().encodeToString(prefix)

        asyncTransaction(Uni.pool) {

            try {
                Users.update({
                    Users.id.eq(ctx.author.idLong)
                }) {
                    it[customPrefix] = encode
                }
                ctx.send("User (${ctx.author.name+"#"+ctx.author.discriminator+" (${ctx.author.idLong}) "}) prefix changed $prefix")
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }.execute()
    }
}

@Argument("prefix", "string")
class RemUserPrefix : Command(){
    override val desc = "remove user prefix"

    override fun run(ctx: Context) {
        val prefix = (ctx.args["prefix"] as String).toLowerCase().replace("remove", "").toByteArray()
        val encode = Base64.getEncoder().encodeToString(prefix)

        asyncTransaction(Uni.pool) {
            if (ctx.storedUser.customPrefix!!.isEmpty()) {
                return@asyncTransaction ctx.send("No User (${ctx.author.name+"#"+ctx.author.discriminator+" (${ctx.author.idLong}) "}) prefix found!")
            }

            try {
                Users.update({
                    Users.id.eq(ctx.author.idLong)
                }) {
                    val pre = encode

                    it[customPrefix] = pre
                }
                ctx.send("User (${ctx.author.name+"#"+ctx.author.discriminator+" (${ctx.author.idLong}) "}) prefix has been removed $prefix")
            } catch (e: Throwable) {
                ctx.sendError(e)
            }
        }.execute()

    }
}

@Load
class UserPrefix : Command(){
    override val desc = "Add, view or delete the user prefix"

    init {
        addSubcommand(AddUserPrefix(), "add")
        addSubcommand(RemUserPrefix(), "remove")
    }

    override fun run(ctx: Context){

        ctx.send("current User (${ctx.author.name+"#"+ctx.author.discriminator+" (${ctx.author.idLong}) "}) prefix ${
        if(ctx.storedUser.customPrefix.isNullOrEmpty()) "none"
        else ctx.storedUser.customPrefix }")
    }

}