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
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.*
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Member
import java.awt.Color
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@CommandGroup("system")
class BotInfoCommand {

    private val startTime = Instant.now()

    @Command(
            prefix = "${EnvVars.PREFIX}",
            aliases = ["bot", "botinfo"],
            id = "bot",
            description = "get the bot info for this bot"
    )
    @Permissions(
            allowDm = true
    )
    fun botInfoCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        val member : Member? = event.guild.getMember(event.jda.selfUser)
        if (author!!.isBot) return
        else {
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val embedColor = Color(randomColor, randomColor1, randomColor2)

            val time =  OffsetDateTime.parse(event.jda.selfUser.creationTime.toString()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val embed = EmbedBuilder()
                    .setAuthor("Bot Info", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                    .setColor(embedColor)
                    .addField("Bot Devs: ", "computerfreaker#4054\n<@!138302166619258880>", true)
                    .addField("Bot Name: ", "${event.jda.selfUser.name}", true)
                    .addField("Bot Id: ", "${event.jda.selfUser.id}", true)
                    .addField("JDA Version: ", "${JDAInfo.VERSION}", true)
                    .addField("System Uptime: ", "${Duration.between(startTime, Instant.now()).formatDuration()}", true)
                    .addField("Guild Count: ", "${event.jda.guilds.size}", true)
                    .addField("Users:", "${event.jda.users.size}", true)
                    .addField("Creation Date: ", "$time", true)
                    .addField("Joined ${event.guild!!.name} on: ", "${member!!.joinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                    .addField("Ping: ", "${event.jda.ping}ms", true)
                    .addField("Uni invite: ", "[invite me](https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=2146958591)", true)
                    .addField("Support Server Invite: ", "[support server](https://discord.gg/rMVju6a)", true)
                    .addField("CFs API server", "[API server](https://discord.gg/gzWwtWG )", true)
                    .setFooter("requested by ${event.author.name}#${event.author.discriminator} (${event.author.id})", "${event.author.avatarUrl}")
                    .build()
            event.textChannel.sendMessage(embed).queue()
        }
    }

    private fun Duration.formatDuration(): String {
        val seconds = Math.abs(this.seconds)
        return "**${seconds / 86400}**d **${(seconds % 86400) / 3600}**h **${(seconds % 3600) / 60}**min **${seconds % 60}**s"
    }
}