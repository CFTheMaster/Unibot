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
package com.github.cf.discord.uni.commands.admin

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.utils.PermissionUtil

@CommandGroup("admin")
class AdminCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "ban",
            aliases = ["ban"],
            description = "ban a certained user by tagging",
            usage = "<input to ban a certained user>"
    )
    fun ban(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        val guild = event.guild
        val mentioned = event.message.mentionedUsers
        if (author!!.isBot) return
        else if (mentioned.isEmpty()) event.channel.sendMessage("please provide user/users to be banned")
        else if (event.member.hasPermission(Permission.BAN_MEMBERS) || event.member.hasPermission(Permission.ADMINISTRATOR)) {
            var hasBanned = false
            mentioned.forEach {
                try {
                    if (PermissionUtil.canInteract(event.guild.selfMember, event.message.guild.getMemberById(it.id)) || event.jda.selfUser.equals(mentioned)){
                        event.guild.controller.ban(it.id, 0,"banned by ${event.author.name}").queue()
                        hasBanned = true
                    }else{
                        event.channel.sendMessage(EmbedBuilder()
                                .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                                .setTitle("person to high ranked")
                                .setDescription("i can't ban <@!${it.id}>")
                                .build()).queue()
                    }
                } catch (e: Exception) {
                    event.channel.sendMessage("Please make sure I have the proper permission to ban ${it.toString()}")
                }
            }
            if(hasBanned)
                event.channel.sendMessage("i have banned the user(s)").queue()
        } else {
            event.channel.sendMessage("You need the `Ban Members` permission to use this command!").queue()
        }
    }
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "hackban",
            aliases = ["hackban"],
            description = "hackban a certained user by id",
            usage = "<input to hackban a certained user>"
    )
    fun hackban(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        val member = context.args
        if (author!!.isBot) {
            return
        } else {
            if (member!!.isEmpty()) event.channel.sendMessage("please provide an user ID")
            else if (event.member.hasPermission(Permission.BAN_MEMBERS) || event.member.hasPermission(Permission.ADMINISTRATOR)) {
                event.guild.controller.ban(member, 0, "Hackbanned by ${event.author.name}").queue()
                event.channel.sendMessage("i have banned the user").queue()
            } else {
                event.channel.sendMessage("You need the `Ban Members` permission to use this command!").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "kick",
            aliases = ["kick"],
            description = "kick a certained user by mention",
            usage = "<input to kick a certained user>"
    )
    fun kick(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        val guild = event.guild
        val mentioned = event.message.mentionedUsers
        if (author!!.isBot) return
        else if (mentioned.isEmpty()) event.channel.sendMessage("please provide user/users to be kicked")
        else if (event.member.hasPermission(Permission.KICK_MEMBERS) || event.member.hasPermission(Permission.ADMINISTRATOR)) {
            var hasKicked = false
            mentioned.forEach {
                try {
                    if (PermissionUtil.canInteract(event.guild.selfMember, event.message.guild.getMemberById(it.id)) || event.jda.selfUser.equals(mentioned)){
                        event.guild.controller.kick(it.id, "Kicked by ${event.author.name}").queue()
                        hasKicked = true
                    }else{
                        event.channel.sendMessage(EmbedBuilder()
                                .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                                .setTitle("person to high ranked")
                                .setDescription("i can't kick <@!${it.id}>")
                                .build()).queue()
                    }
                } catch (e: Exception) {
                    event.channel.sendMessage("Please make sure I have the proper permission to kick ${it.toString()}")
                }
            }
            if(hasKicked)
                event.channel.sendMessage("i have kicked the user(s)").queue()
        } else {
            event.channel.sendMessage("You need the `Kick Members` permission to use this command!").queue()
        }
    }
}