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
class Mute : Command(){
    override val desc = "Mute people."
    override val guildOnly = true

    override fun run(ctx: Context) {
        if(ctx.storedGuild!!.mutedRole == null){
            return ctx.send("no muted role found, ${ctx.author.name}")
        }

        val user = ctx.args["user"] as Member
        val role = ctx.guild!!.getRoleById(ctx.storedGuild.mutedRole!!)
                ?: return ctx.send("muted role has been deleted")

        ctx.guild.controller
                .addSingleRoleToMember(user, role)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({ctx.send("user has been muted: ${user.user.name}")}) {
                    if (it is PermissionException){
                        ctx.send("can't mute the user: ${user.user.name}, missing permission: ${it.permission.name}")
                    } else ctx.sendError(it)
                }
    }
}