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
package com.github.cf.discord.uni.commands.owner

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color

@CommandGroup("owner")
class TestApiCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "hentai_api",
            aliases = ["hentai", "testapi", "hentaiapi"],
            description = "get a random picture from my hentai API",
            usage = "<execute to get a random picture from my hentai API>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        if(event.textChannel.isNSFW) {
            val author = event.author
            if(author!!.isBot) {
                return
            } else {
                val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val embedColor = Color(randomColor, randomColor1, randomColor2)

                val myAss = getTestApi()

                val embed = EmbedBuilder()
                        .setAuthor("hentai in my city", "$myAss", "https://computerfreaker.cf/profile/profile.png")
                        .setColor(embedColor)
                        .setImage("$myAss")
                        .setFooter("powered by: https://api.computerfreaker.cf", "${event.jda.getUserById(138302166619258880).avatarUrl}")
                        .build()
                event.channel.sendMessage(embed).queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "anime_api",
            aliases = ["anime", "animetestapi", "animeapi"],
            description = "get a somewhat random picture from my own API",
            usage = "<execute to get a random picture from my API>"
    )
    @Permissions(
            allowDm = true
    )
    fun onAnimeCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val embedColor = Color(randomColor, randomColor1, randomColor2)

            val ohMyGod = getAnimeTestApi()

            val embed = EmbedBuilder()
                    .setAuthor("anime in my city", "$ohMyGod", "https://computerfreaker.cf/profile/profile.png")
                    .setColor(embedColor)
                    .setImage("$ohMyGod")
                    .setFooter("powered by: https://api.computerfreaker.cf", "${event.jda.getUserById(138302166619258880).avatarUrl}")
                    .build()
            event.channel.sendMessage(embed).queue()
        }
    }

    private fun getTestApi(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://api.computerfreaker.cf/v1/hentai")
                .build()).execute()

        return if (response.isSuccessful) {
            val content = JSONObject(response.body()?.string())
            response.body()?.close()
            content.getString("url")
        } else {
            response.body()?.close()
            null
        }
    }

    private fun getAnimeTestApi(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://api.computerfreaker.cf/v1/anime")
                .build()).execute()

        return if (response.isSuccessful) {
            val content = JSONObject(response.body()?.string())
            response.body()?.close()
            content.getString("url")
        } else {
            response.body()?.close()
            null
        }
    }
}