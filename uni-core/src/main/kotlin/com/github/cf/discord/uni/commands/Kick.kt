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

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Arguments
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.commands.system.Category
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
class Kick : Command(){
    override val desc = "kick a user"
    override val guildOnly = true
    override val cate = Category.MODERATION.name

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as Member

        if (!ctx.member!!.canInteract(user)) {
            return ctx.send("user cant be banned")
        }

        if (!ctx.selfMember!!.canInteract(user)) {
            return ctx.send("bot can't be banned")
        }

        ctx.guild!!
                .kick(user)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send("<@!${user.user.idLong}> (${user.user.idLong}): has been kicked")
                }) { err ->
                    if (err is PermissionException) {
                        ctx.send("permissions missing can't ban the user")
                    } else {
                        ctx.sendError(err)
                    }
                }
    }
}
