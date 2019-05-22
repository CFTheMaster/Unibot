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

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

@Load
class Invite : Command(){
    override val desc = "get the invite to the bot"
    override val guildOnly = false

    override fun run(ctx: Context) {
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val embed = EmbedBuilder().apply {
            setTitle("invite for bot/support server")
            setColor(embedColor)
            setDescription("Uni invite: [click me](https://discordapp.com/oauth2/authorize?client_id=${ctx.jda.selfUser.id}&scope=bot&permissions=-1)")
            addField("invite to my server ", "[server invite]( https://discord.gg/rMVju6a)", true)
            addField("invite to my API server", "[API server invite](https://discord.gg/gzWwtWG)", true)
            setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", "${ctx.author.avatarUrl}")
        }
        ctx.send(embed.build())
    }
}
