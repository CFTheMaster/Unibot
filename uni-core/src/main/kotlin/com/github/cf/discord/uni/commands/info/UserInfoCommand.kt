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
package com.github.cf.discord.uni.commands.info

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.extensions.*
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.requests.restaction.MessageAction
import java.awt.Color
import java.time.format.DateTimeFormatter

@CommandGroup("system")
class UserInfoCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            aliases = ["user", "userinfo"],
            id = "user",
            description = "get the user info for the user tagged or yourself"
    )
    fun userInfoCommand(context: CommandContext, event: MessageReceivedEvent): MessageAction? {
        val query = context.args
        val temp : Member? = if(event.isFromType(ChannelType.TEXT)) {
            if(query == null)
                event.member
            else {
                val found = event.guild.findMembers(query)
                when {
                    found.isEmpty() -> null
                    found.size>1 -> return event.channel.sendMessage(found.multipleMembers(query))
                    else -> found[0]
                }
            }
        } else null

        val user : User = when {
            temp!=null -> temp.user
            query!!.isEmpty() -> event.author
            else -> {
                val found =  event.jda.findUsers(query)
                when {
                    found.isEmpty() -> return event.channel.sendMessage(noMatch("users", query))
                    found.size>1    -> return event.channel.sendMessage(found.multipleUsers(query))
                    else            -> found[0]
                }
            }
        }

        val member : Member? = if(temp == null && event.isFromType(ChannelType.TEXT)) event.guild.getMember(user) else temp
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
        val embedColor = Color(randomColor, randomColor1, randomColor2)
        val embed = EmbedBuilder()
                .setAuthor("User Info", null, "${member?.user?.avatarUrl}")
                .setThumbnail("${member?.user?.avatarUrl}")
                .setColor(embedColor)
                .addField("User Status: ", "${member?.onlineStatus?.key}", true)
                .addField("User ID: ", "${member?.user?.id}", true)
                .addField("Highest role: ", "${member?.roles?.sortedBy { it.position }?.last()?.name ?: "none"}\n", true)
                .addField("Username: ", "${member?.user?.name}", true)
                .addField("Nickname: ", "${member?.nickname ?: "none"}", true)
                .addField("Joined Discord On: ", "${member?.user?.creationTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                .addField("Joined this server on: ", "${member?.joinDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                .addField("Playing: ", "${member?.game?.name ?: "nothing"}", true)
                .addField("Avatar Url: ", "[Avatar]( ${member?.user?.avatarUrl} )", true)
                .addField("Is a bot: ", "${member?.user?.isBot}", true)
                .setFooter("requested by ${event.author.name}#${event.author.discriminator} (${event.author.id})", "${event.author.avatarUrl}")
                .build()
        val author = event.author
        if(author!!.isBot){

        }
        else{
            event.channel.sendMessage(embed).queue()
        }
        return null
    }
}