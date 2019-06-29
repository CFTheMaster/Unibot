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

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.utils.CFApi
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Load
@Argument("user", "user", true)
class Hug : Command(){
    override val desc = "hug a user"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val smolHug = CFApi.getCFApi("hug")

        ctx.send(EmbedBuilder().apply {
            val mem = ctx.args["user"] as Member
            setTitle(if (ctx.args["user"] == null || ctx.author.id == mem.user.id) "trying to hug yourself " else "${mem.user.name}, you got a hug from ${ctx.member!!.user.name}", smolHug)
            setImage(smolHug)
            setColor(ctx.member?.colorRaw ?: 6684876)
            setFooter("powered by: https://api.computerfreaker.cf", ctx.jda.getUserById(138302166619258880).avatarUrl)
        }.build())
    }
}
