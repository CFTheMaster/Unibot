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

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.HelpCommand.Companion.WEBSITE_URL
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDAInfo
import net.dv8tion.jda.core.entities.Member
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Load
@Alias("bot")
class BotInfo : Command(){
    override val desc = "gives you the bots current stats"
    override val guildOnly = false

    private val startTime = Instant.now()

    override fun run(ctx: Context){
        val ramUsedRaw = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val ramUsedMB = ramUsedRaw / 1048576
        val member : Member? = ctx.guild!!.getMember(ctx.jda.selfUser)

        val totalDays = ChronoUnit.DAYS.between(member!!.user.creationTime.toLocalDate(), OffsetDateTime.now().toLocalDate())

        val time =  OffsetDateTime.parse(ctx.jda.selfUser.creationTime.toString()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val embed = EmbedBuilder().apply{
            setAuthor("Uni v${HelpCommand.VERSION_NUMBER}", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
            setColor(ctx.member!!.color)
            addField("Bot Devs: ", "${ctx.jda.getUserById(138302166619258880).name}#${ctx.jda.getUserById(138302166619258880).discriminator}\n<@!138302166619258880>", true)
            addField("Bot Name: ", ctx.jda.selfUser.name, true)
            addField("Bot Id: ", ctx.jda.selfUser.id, true)
            addField("JDA Version: ", JDAInfo.VERSION, true)
            addField("LavaPlayer Version: ", PlayerLibrary.VERSION, true)
            addField("System Uptime: ", Duration.between(startTime, Instant.now()).formatDuration(), true)
            addField("Used Memory: ", "$ramUsedMB MB", true)
            addField("Guild Count: ", "${Uni.shardManager.guilds.size}", true)
            addField("Total Users: ", "${Uni.shardManager.users.filter { !it.isBot }.size}", true)
            addField("Total Bots:", "${Uni.shardManager.users.filter { it.isBot }.size}", true)
            addField("Current Shard: ", "${ctx.jda.shardInfo.shardId}", true)
            addField("Total Shards: ", "${ctx.jda.shardInfo.shardTotal}", true)
            addField("Creation Date: ", time, true)
            addField("Total Days Since Creation:", totalDays.toString(), true)
            addField("Joined This server On: ", ctx.member.joinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), true)
            addField("Ping: ", "${ctx.jda.ping}ms", true)
            addField("Avatar URL: ", "[Avatar URL](  ${ctx.jda.selfUser.avatarUrl} )", true)
            addField("Uni Invite: ", "[Invite Me](https://discordapp.com/oauth2/authorize?client_id=${ctx.jda.selfUser.id}&scope=bot&permissions=-1)", true)
            addField("Support Server Invite: ", "[Support Server](https://discord.gg/DDRbw7W)", true)
            addField("CFs API Server", "[API Server](https://discord.gg/gzWwtWG )", true)
            addField("CFs Github: ", "[CFs Github](https://github.com/CFTheMaster)", true)
            addField("Uni Website:", "[Uni website]($WEBSITE_URL)", true)
            setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", ctx.author.avatarUrl)
        }

        ctx.send(embed.build())

    }

    private fun Duration.formatDuration(): String {
        val seconds = Math.abs(this.seconds)
        return "**${seconds / 86400}**d **${(seconds % 86400) / 3600}**h **${(seconds % 3600) / 60}**min **${seconds % 60}**s"
    }
}
