/*
 *   Copyright (C) 2017-2018 computerfreaker
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

import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.entities.*
import com.github.cf.discord.uni.utils.I18n
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.exceptions.PermissionException

@Load
@Perm(Permission.BAN_MEMBERS)
@Arguments(
        Argument("user", "user"),
        Argument("reason", "string", true)
)
class Ban : Command(){
    override val desc = "Ban members from the guild"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as Member

        if (!ctx.member!!.canInteract(user)) {
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("user_cant_ban"),
                            mapOf("username" to ctx.author.name)
                    )
            )
        }

        if (!ctx.selfMember!!.canInteract(user)) {
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("bot_cant_ban"),
                            mapOf("username" to ctx.author.name)
                    )
            )
        }

        ctx.guild!!.controller
                .ban(user, 24)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send(
                            I18n.parse(
                                    ctx.lang.getString("banned_user"),
                                    mapOf("username" to user.user.name)
                            )
                    )
                }) { err ->
                    if (err is PermissionException) {
                        ctx.send(
                                I18n.parse(
                                        ctx.lang.getString("perm_cant_ban"),
                                        mapOf(
                                                "username" to user.user.name,
                                                "permission" to I18n.permission(ctx.lang, err.permission.name)
                                        )
                                )
                        )
                    } else {
                        ctx.sendError(err)
                    }
                }
    }
}


@Load
@Perm(Permission.BAN_MEMBERS)
class HackBan : Command(){
    override val desc = "Hack Ban members from the guild"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val author = ctx.author
        val member = ctx.args
        if(member.isEmpty()) ctx.channel.sendMessage("please provide an user ID").queue()
        else if(ctx.member!!.hasPermission(Permission.BAN_MEMBERS)){
            ctx.guild!!.controller.ban("$member", 0, "hackbanned by ${ctx.author.name}").queue()
        }
    }
}

@Load
@Perm(Permission.KICK_MEMBERS)
@Arguments(
        Argument("user", "user"),
        Argument("reason", "string", true)
)
class Kick : Command() {
    override val desc = "Kick members from the guild"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as Member

        if (!ctx.member!!.canInteract(user)) {
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("user_cant_kick"),
                            mapOf("username" to ctx.author.name)
                    )
            )
        }

        if (!ctx.selfMember!!.canInteract(user)) {
            return ctx.send(
                    I18n.parse(
                            ctx.lang.getString("bot_cant_kick"),
                            mapOf("username" to ctx.author.name)
                    )
            )
        }

        ctx.guild!!.controller
                .kick(user)
                .reason("[ ${ctx.author.name}#${ctx.author.discriminator} ] ${ctx.args.getOrDefault("reason", "none")}")
                .queue({
                    ctx.send(
                            I18n.parse(
                                    ctx.lang.getString("kicked_user"),
                                    mapOf("username" to user.user.name)
                            )
                    )
                }) { err ->
                    if (err is PermissionException) {
                        ctx.send(
                                I18n.parse(
                                        ctx.lang.getString("perm_cant_kick"),
                                        mapOf(
                                                "username" to user.user.name,
                                                "permission" to I18n.permission(ctx.lang, err.permission.name)
                                        )
                                )
                        )
                    } else {
                        ctx.sendError(err)
                    }
                }
    }
}