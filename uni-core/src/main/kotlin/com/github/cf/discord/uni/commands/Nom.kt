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

//WIP (Need to ask for token)

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.natanbc.weeb4j.image.NsfwFilter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member

class Nom : Command() {
    override val desc = "Nom people."
    override val guildOnly = true

    override fun run(ctx: Context) {

        val api = Uni.wolkeApi
        api.getRandomImage("hug", null, null, NsfwFilter.NO_NSFW, null).async {
            image ->
            val mentionedPeople: List<Member> = ctx.msg.mentionedMembers.filterNotNull().toList()
            ctx.send(EmbedBuilder().apply {
                setTitle(
                        if (ctx.args["user"] == null || ctx.msg.mentionedMembers[0].user.idLong == ctx.author.idLong)  { "nommed yourself " }
                        else {
                            if(mentionedPeople.isEmpty()) null
                            else mentionedPeople.asSequence().joinToString { it.user.name }.plus(", ") + "you have been nommed ${ctx.member!!.user.name}"
                        }, image.url
                )
                setImage(image.url)
                setColor(ctx.member?.colorRaw ?: 6684876)
                setFooter("powered by: https://weeb.sh", ctx.jda.getUserById(138302166619258880)!!.avatarUrl)
            }.build())
        }

    }
}
