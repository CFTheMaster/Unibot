/*
 *   Copyright (C) 2017-2020 computerfreaker
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
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.findMembers
import com.github.cf.discord.uni.utils.CFApi
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.kotlin.utils.mapToIndex
import org.json.JSONObject

@Load
@Argument("user", "string", true)
class Hug : Command(){
    override val desc = "hug a user"
    override val guildOnly = true
    override val cate = Category.IMAGE.name

    override fun run(ctx: Context) {
        val smolHug = CFApi.getCFApi("hug")

        val mentionedPeople: List<Member> = ctx.msg.mentionedMembers.filterNotNull().toList()
        ctx.send(EmbedBuilder().apply {
            setTitle(
                    if (ctx.args["user"] == null || ctx.msg.mentionedMembers[0].user.idLong == ctx.author.idLong)  { "trying to hug yourself? " }
                        else {
                        if(mentionedPeople.isEmpty()) return
                            else mentionedPeople.asSequence().joinToString { it.user.name }.plus(", ") + "you got a hug from ${ctx.member!!.user.name}"
                    },smolHug)

            setImage(smolHug)
            setColor(ctx.member?.colorRaw ?: 6684876)
            setFooter("powered by: https://api.computerfreaker.cf", ctx.jda.getUserById(138302166619258880)!!.avatarUrl)
        }.build())
    }
}
