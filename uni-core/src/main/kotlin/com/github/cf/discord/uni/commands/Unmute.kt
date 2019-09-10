/*
 *   Copyright (C) 2017-2019 computerfreaker
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

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Arguments
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.PermissionException

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

        ctx.guild
                .removeRoleFromMember(user, role)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send("unmuted user: ${user.user.name}")
                }) {
                    if (it is PermissionException) {
                        ctx.send("missing permission")
                    } else {
                        ctx.sendError(it)
                    }
                }
    }
}
