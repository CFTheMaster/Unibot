package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

@Load
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
            setDescription("Uni Upvote: [upvote me](https://discordbots.org/bot/${ctx.jda.selfUser.idLong}/vote)")
            setFooter("requested by ${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id})", "${ctx.author.avatarUrl}")
        }

        ctx.send(embed.build())
    }
}