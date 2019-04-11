package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.util.concurrent.ThreadLocalRandom

@Load
class EightBall : Command(){
    companion object {
        private val lines = listOf(
                "It is certain",
                "It is decidedly so",
                "Without a doubt",
                "Yes, definitely",
                "You may rely on it",
                "As I see it, yes",
                "Most likely",
                "Outlook good",
                "Yes",
                "Signs point to yes",
                "Reply hazy try again",
                "Ask again later",
                "Better not tell you now",
                "Cannot predict now",
                "Concentrate and ask again",
                "Don't count on it",
                "My reply is no",
                "My sources say no",
                "Outlook not so good",
                "Very doubtful")
        const val EIGHTBALL_EMOJI = "\uD83C\uDFB1"
    }

    override val desc = "Change the volume of the music"
    override val guildOnly = false

    override fun run(ctx: Context) {
        ctx.send("**Question:** ${ctx.args}\n$EIGHTBALL_EMOJI**: ${randAnswer()}**")
    }

    private fun randAnswer(): String {
        return lines[ThreadLocalRandom.current().nextInt(lines.size)]
    }
}