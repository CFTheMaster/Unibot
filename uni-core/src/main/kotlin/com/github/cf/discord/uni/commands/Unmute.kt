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
class Unmute : Command() {
    override val guildOnly = true
    override val desc = "Unmute people."

    override fun run(ctx: Context) {
        if (ctx.storedGuild!!.mutedRole == null) {
            return ctx.send("no muted role found: ${ctx.author.name}")
        }

        val user = ctx.args["user"] as Member
        val role = ctx.guild!!.getRoleById(ctx.storedGuild.mutedRole!!)
                ?: return ctx.send("muted role has been deleted")

        if (role !in user.roles) {
            return ctx.send("${ctx.author.name} not muted")
        }

        ctx.guild.controller
                .removeSingleRoleFromMember(user, role)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send("unmuted_user: ${user.user.name}")
                }) {
                    if (it is PermissionException) {
                        ctx.send("missing permission")
                    } else {
                        ctx.sendError(it)
                    }
                }
    }
}