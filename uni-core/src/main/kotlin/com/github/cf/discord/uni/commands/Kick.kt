package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Arguments
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.exceptions.PermissionException

@Load
@Perm(Permission.KICK_MEMBERS)
@Arguments(
        Argument("user", "user"),
        Argument("reason", "string", true)
)
class Kick : Command(){
    override val desc = "kick a user"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as Member

        if (!ctx.member!!.canInteract(user)) {
            return ctx.send("user cant be banned")
        }

        if (!ctx.selfMember!!.canInteract(user)) {
            return ctx.send("bot can't be banned")
        }

        ctx.guild!!.controller
                .kick(user)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send("kicked user")
                }) { err ->
                    if (err is PermissionException) {
                        ctx.send("permissions missing can't ban the user")
                    } else {
                        ctx.sendError(err)
                    }
                }
    }
}