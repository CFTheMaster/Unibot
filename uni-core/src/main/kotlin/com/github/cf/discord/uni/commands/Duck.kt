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

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.api.EmbedBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Load
@Alias("quack")
class Duck : Command(){
    override val desc = "Change the volume of the music"
    override val guildOnly = false
    override val cate = Category.IMAGE.name

    override fun run(ctx: Context) {
        val aDuck = getDucky()

        val embed = EmbedBuilder().apply {
            setTitle("image link", aDuck)
            setColor(ctx.member?.colorRaw ?: 6684876)
            setImage(aDuck)
            setFooter("powered by https://random-d.uk/", null)
        }
        ctx.send(embed.build())
    }

    private fun getDucky(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://random-d.uk/api/v1/random")
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
