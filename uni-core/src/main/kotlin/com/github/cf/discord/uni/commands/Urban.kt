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

import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.utils.Http
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import org.json.JSONObject

@Load
@Argument("term", "string")
@Alias("ud", "urbandictionary", "urban")
class Urban : Command() {
    override val desc = "Search on the urban dictionary!"
    override val nsfw = true

    override fun run(ctx: Context) {
        Http.get(HttpUrl.Builder().apply{
            scheme("https")
            host("api.urbandictionary.com")
            addPathSegment("v0")
            addPathSegment("define")
            addQueryParameter("term", ctx.args["term"] as String)
        }.build()).thenAccept{ res ->
            val json = JSONObject(res.body()!!.string())


            if (json.getString("result_type") == "no_results") {
                return@thenAccept ctx.send("couldn't find anything")
            }

            val list = json.getJSONArray("list")

            if (list.count() == 0) {
                return@thenAccept ctx.send("couldn't find anything")
            }

            val item = list.getJSONObject(0)

            val embed = EmbedBuilder().apply {
                setAuthor(item.getString("author"))
                setTitle(item.getString("word"), item.getString("permalink"))
                descriptionBuilder.append(item.getString("definition"))
                descriptionBuilder.append("\n\n${item.getString("example")}")
                setFooter("${item.getInt("thumbs_up")} \uD83D\uDC4D | ${item.getInt("thumbs_down")} \uD83D\uDC4E", null)
            }

            ctx.send(embed.build())
            res.close()
        }.thenApply {}.exceptionally {
            LOGGER.error("Error while trying to get definition from urban dictionary", it)
            ctx.sendError(it)
        }
    }
}
