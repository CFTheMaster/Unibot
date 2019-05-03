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