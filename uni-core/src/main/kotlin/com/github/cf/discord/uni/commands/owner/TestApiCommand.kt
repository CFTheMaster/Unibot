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
import com.github.cf.discord.uni.data.authorOnly
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONObject
import java.awt.Color

@CommandGroup("owner")
class TestApiCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "api",
            aliases = ["testapi"],
            description = "just a test",
            usage = "<test>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        if(event.textChannel.isNSFW) {
            if (event.message.author.id in authorOnly.authors) {
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
                            .setAuthor("hentai in my city", "$myAss", null)
                            .setColor(embedColor)
                            .setImage("$myAss")
                            .setFooter("powered by: https://computerfreaker.cf", "${event.jda.getUserById(138302166619258880).avatarUrl}")
                            .build()
                    event.channel.sendMessage(embed).queue()
                }
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "animeapi",
            aliases = ["animetestapi"],
            description = "just a test",
            usage = "<test>"
    )
    @Permissions(
            allowDm = true
    )
    fun onAnimeCommand(context: CommandContext, event: MessageReceivedEvent){
        if (event.message.author.id in authorOnly.authors) {
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
                        .setAuthor("anime in my city", "$ohMyGod", null)
                        .setColor(embedColor)
                        .setImage("$ohMyGod")
                        .setFooter("powered by: https://computerfreaker.cf", "${event.jda.getUserById(138302166619258880).avatarUrl}")
                        .build()
                event.channel.sendMessage(embed).queue()
            }
        }
    }

    private fun getTestApi(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://computerfreaker.cf/api/hentai/read.php")
                .build()).execute()

        if (response.isSuccessful) {
            val content = JSONObject(response.body()?.string())
            response.body()?.close()
            return content.getString("url")
        } else {
            response.body()?.close()
            return null
        }
    }

    private fun getAnimeTestApi(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://computerfreaker.cf/api/anime/read.php")
                .build()).execute()

        if (response.isSuccessful) {
            val content = JSONObject(response.body()?.string())
            response.body()?.close()
            return content.getString("url")
        } else {
            response.body()?.close()
            return null
        }
    }
}