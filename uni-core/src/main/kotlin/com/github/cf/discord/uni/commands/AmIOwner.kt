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
class AmIOwner : Command(){
    override val desc = "check if you are my owner"

    override fun run(ctx: Context) {
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        if (ctx.author.idLong == 138302166619258880){
            ctx.send(EmbedBuilder().apply {
                setAuthor("Am I Owner", null, "${if(ctx.author.avatarUrl != null) ctx.author.avatarUrl else null}")
                setColor(embedColor)
                setDescription("""Well since you created me <@!${ctx.author.id}> yes you are <a:Jigglypuff:573815063538958337><a:HomuraRun:573815103279857664>
                                I mean you created the source code so why shouldn't you be my owner
                                In the end... Yes you are my owner""")
            }.build())
        }else{
            ctx.send(EmbedBuilder().apply {
                setAuthor("Am I Owner", null, "${if(ctx.author.avatarUrl != null) ctx.author.avatarUrl else null}")
                setColor(embedColor)
                setDescription("To make a long story short no <a:Sakura:404593137059627009>\n ")

            }.build())
        }
    }
}
