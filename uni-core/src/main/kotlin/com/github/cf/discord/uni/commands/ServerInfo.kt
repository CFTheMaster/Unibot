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

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Load
@Alias("guild", "server", "guildinfo")
class ServerInfo : Command(){
    override val desc = "server info"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)
        val totalDays = ChronoUnit.DAYS.between(ctx.guild!!.creationTime.toLocalDate(), OffsetDateTime.now().toLocalDate())

        val embed = EmbedBuilder().apply {
            setAuthor("Guild Info", null, "${if(ctx.guild.iconUrl != null) ctx.guild.iconUrl else null}")
            setThumbnail("${if(ctx.guild.iconUrl != null) ctx.guild.iconUrl else null}")
            setColor(embedColor)
            addField("Guild Name: ", ctx.guild!!.name, true)
            addField("Guild ID: ", ctx.guild.id, true)
            addField("Guild Owner: ", ctx.guild.owner.user.name, true)
            addField("Guild Region: ", "${ctx.guild.region}", true)
            addField("Guild Creation Date: ", ctx.guild.creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), true)
            addField("Days Since Guild Creation:", totalDays.toString(), true)
            addField("Guild Members: ", "${ctx.guild.members.size}", true)
            addField("Bots: ","${ctx.guild.members.filter { it.user.isBot }.size}", true)
            addField("Highest role: ", "${ctx.guild.roles.get(0).name ?: "none"}\n", true)
            addField("Text Channels: ", "${ctx.guild.textChannels.size}", true)
            addField("Voice Channels: ", " ${ctx.guild.voiceChannels.size} ", true)
            addField("Custom Emojis: ", "${ctx.guild.emotes.size}", true)
            setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", ctx.author.avatarUrl)
        }
        ctx.send(embed.build())
    }
}
