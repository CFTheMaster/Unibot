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
import com.github.cf.discord.uni.utils.CFApi
import net.dv8tion.jda.api.EmbedBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Load
@Alias("neko")
class Catgirl : Command(){
    override val desc = "Change the volume of the music"
    override val guildOnly = false

    override fun run(ctx: Context) {
        val catgirl = if (CFApi.getCFApi("neko") != null) CFApi.getCFApi("neko") else getNeko()

        val embed = EmbedBuilder().apply {
            setTitle("image link", catgirl)
            setColor(ctx.member?.colorRaw ?: 6684876)
            setImage(catgirl)
            if(catgirl != null){
                setFooter("powered by https://api.computerfreaker.cf", null)
            }else{
                setFooter("powered by https://nekos.life", null)
            }
        }
        ctx.send(embed.build())
    }


    private fun getNeko(): String?{
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://nekos.life/api/v2/img/neko")
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
