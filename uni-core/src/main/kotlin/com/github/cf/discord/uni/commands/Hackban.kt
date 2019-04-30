package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Arguments
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.exceptions.PermissionException

@Load
@Perm(Permission.BAN_MEMBERS)
@Arguments(
        Argument("user", "string"),
        Argument("reason", "string", true)
)
class Hackban : Command(){
    override val desc = "Ban members from the guild"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val users = (ctx.args["user"] as String).split("||")
        for (user in users){
            ctx.guild!!.controller
                    .ban(user, 0)
                    .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                    .queue({
                        ctx.send("<@!$user> ($user): has been banned")
                    }) { err ->
                        if (err is PermissionException) {
                            ctx.send("permissions missing can't ban the user")
                        } else {
                            ctx.sendError(err)
                        }
                    }
        }

    }
}