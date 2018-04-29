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
package com.github.cf.discord.uni.commands.`fun`

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

@CommandGroup("fun")
class LewdCatgirlCommand {
    companion object {
        const val BASE_URL = "https://nekos.life/api/v2/img/lewd"
    }
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "lewdneko",
            aliases = ["lewdcatgirl", "lewdneko"],
            description = "get a random lewd catgirl.",
            usage = "<execute the command and get a random lewd catgirl>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        val textChannel = event.textChannel
        if (textChannel!!.isNSFW){
            if(author!!.isBot) {
                return
            } else {
                val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val embedColor = Color(randomColor, randomColor1, randomColor2)

                val lewdNeko = getLewdCatgirl()

                val embed = EmbedBuilder()
                        .setTitle("image link", lewdNeko)
                        .setColor(embedColor)
                        .setImage(lewdNeko)
                        .setFooter("powered by https://nekos.life", null)
                        .build()
                event.channel.sendMessage(embed).queue()
            }
        }else{
            event.message.channel.sendMessage("no lewds here try a NSFW enabled channel").queue()
        }
    }

    private fun getLewdCatgirl(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url(BASE_URL)
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