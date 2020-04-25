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

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

@Load
@Alias("upvote")
class Vote : Command(){
    override val desc = "get the link to vote on this bot"
    override val guildOnly = false

    override fun run(ctx: Context) {
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val embed = EmbedBuilder().apply {
            setTitle("vote for the bot", ctx.jda.selfUser.avatarUrl)
            setColor(embedColor)
            addField("**Uni Upvote (DiscordBots):** ","[upvote me](https://discordbots.org/bot/${ctx.jda.selfUser.idLong}/vote)", true)
            addField("**Uni Upvote (Discord Boats):** ","[upvote me (Discord Boats)](https://discord.boats/bot/${ctx.jda.selfUser.idLong}/vote)", true)
            setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", "${ctx.author.avatarUrl}")
        }

        ctx.send(embed.build())
    }
}
