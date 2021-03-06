/*
 *   Copyright (C) 2017-2021 computerfreaker
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
import com.github.cf.discord.uni.database.DatabaseWrapper
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.ThreadedCommand
import com.github.cf.discord.uni.listeners.EventListener
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color
import java.util.concurrent.TimeUnit

@Load
@Arguments(
        Argument("link", "string", true)
)
@Flags(
        Flag("link", 'l', "Get source of link"),
        Flag("image", 'i', "Get source of image")
)
class SauceNAO : ThreadedCommand(){
    override val desc = "post an attachement with your command"
    override val guildOnly = true

    override fun threadedRun(ctx: Context) {

        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val parser = when {
            ctx.flags.argMap.containsKey("link") || ctx.flags.argMap.containsKey("l") -> (ctx.args["link"] as? String)
            ctx.flags.argMap.containsKey("image") || ctx.flags.argMap.containsKey("i") -> ctx.msg.attachments.first().url
            else -> null
        }

        if(parser == null){
            ctx.send("please do\n`uni!saucenao --help`\non how to use this command")
        } else {
            ctx.send(EmbedBuilder().apply {
                setDescription(getSauceNAO(parser))
                setColor(embedColor)
                setFooter("Image sauce", null)
            }.build())
        }
    }
    private fun getSauceNAO(image: String): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://saucenao.com/search.php?db=999&output_type=2&api_key=${DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).sauceNaoToken}&numres=1&url=$image")
                .build()).execute()

        var result: String? = null
        if(response.isSuccessful)
            result = JSONObject(response.body()?.string()).getJSONArray("results").getJSONObject(0).getJSONObject("data").getJSONArray("ext_urls").getString(0)


        response.body()?.close()
        return result
    }
}
