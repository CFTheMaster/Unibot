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
class Unban : Command() {
    override val desc = "Unban members from the guild."
    override val guildOnly = true

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as String

        ctx.guild!!.controller
                .unban(user)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send("unbanned_user <@$user>")
                }) { err ->
                    if (err is PermissionException) {
                        ctx.send("Missing ${err.permission.name} Permsission to ban that <@!$user>")
                    } else {
                        ctx.sendError(err)
                    }
                }
    }
}