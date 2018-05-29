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
package com.github.cf.discord.uni.listeners

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.core.EnvVars
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.impl.JDAImpl
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.time.format.DateTimeFormatter

class GuildJoinLeaveListener : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        Uni.LOGGER.info("New guild: ${event.guild.name} (${event.guild.id})")
        updateStats(event.jda)
        event.jda.getGuildById(138303776170835969).getTextChannelById(440833941335703572).sendMessage(EmbedBuilder()
                .setAuthor("Joined guild", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setColor(java.lang.Integer.parseInt("#6600cc".replaceFirst("#", ""), 16))
                .addField("Joined Guild: ", "${event.guild.name}", true)
                .addField("Guild ID: ", "${event.guild.idLong}", true)
                .addField("Server Owner: ", "${event.guild.owner.user.name}", true)
                .addField("Server Owner ID: ", "${event.guild.owner.user.idLong}", true)
                .addField("Creation Date: ", "${event.guild.creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                .addField("Guild Members: ", "${event.guild.members.filter { it.user.isBot }.size}", true)
                .addField("Guild Members + Bots: ","${event.guild.members.size}", true)
                .addField("Highest role: ", "${event.guild.roles.get(0).name ?: "none"}\n", true)
                .addField("Text Channels: ", "${event.guild.textChannels.size}", true)
                .addField("Voice Channels: ", " ${event.guild.voiceChannels.size} ", true)
                .addField("Total amount of guilds: ", "${event.jda.guilds.size}", true)
                .build()).queue()
    }


    override fun onGuildLeave(event: GuildLeaveEvent) {
        Uni.LOGGER.info("Left guild: ${event.guild.name} (${event.guild.id}")
        updateStats(event.jda)
        event.jda.getGuildById(138303776170835969).getTextChannelById(440833941335703572).sendMessage(EmbedBuilder()
                .setAuthor("Left guild", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setColor(java.lang.Integer.parseInt("#6600cc".replaceFirst("#", ""), 16))
                .addField("Left Guild: ", "${event.guild.name}", true)
                .addField("Guild ID: ", "${event.guild.idLong}", true)
                .addField("Server Owner: ", "${event.guild.owner.user.name}", true)
                .addField("Server Owner ID: ", "${event.guild.owner.user.idLong}", true)
                .addField("Creation Date: ", "${event.guild.creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                .addField("Guild Members: ", "${event.guild.members.filter { it.user.isBot }.size}", true)
                .addField("Guild Members + Bots: ","${event.guild.members.size}", true)
                .addField("Highest role: ", "${event.guild.roles.get(0).name ?: "none"}\n", true)
                .addField("Text Channels: ", "${event.guild.textChannels.size}", true)
                .addField("Voice Channels: ", " ${event.guild.voiceChannels.size} ", true)
                .addField("Total amount of guilds: ", "${event.jda.guilds.size}", true)
                .build()).queue()
    }

    companion object {
        fun updateStats(jda: JDA) {
            val client = (jda as JDAImpl).httpClientBuilder.build()
            val body = JSONObject().put("server_count", jda.guilds.size)

            if (EnvVars.DBL_TOKEN?.isNotEmpty()!!) {
                client.newRequest({
                    post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    url("https://discordbots.org/api/bots/${jda.selfUser.id}/stats")
                    header("Authorization", EnvVars.DBL_TOKEN)
                    header("Content-Type", "application/json")
                }).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) = response.close()

                    override fun onFailure(call: Call, e: IOException)
                    {
                        Uni.LOGGER.error("Failed to send information to bots.discord.pw", e)
                    }
                })
            }

            if (EnvVars.TERMINAL_TOKEN?.isNotEmpty()!!) {
                client.newRequest({
                    post(RequestBody.create(MediaType.parse("application/json"), body.toString()))
                    url("https://ls.terminal.ink/api/v1/bots/${jda.selfUser.id}")
                    header("Authorization", EnvVars.TERMINAL_TOKEN)
                    header("Content-Type", "application/json")
                }).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) = response.close()

                    override fun onFailure(call: Call, e: IOException)
                    {
                        Uni.LOGGER.error("Failed to send information to ls.terminal.ink", e)
                    }
                })
            }
        }
        private inline fun OkHttpClient.newRequest(lazy: Request.Builder.() -> Unit) : Call {
            val builder = Request.Builder()
            builder.lazy()
            return newCall(builder.build())
        }
    }
}