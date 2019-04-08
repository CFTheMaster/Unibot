package com.github.cf.discord.uni.commands.info

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color

@CommandGroup("system")
class VoteCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            aliases = ["vote"],
            id = "vote",
            description = "get the link to vote on this bot"
    )
    fun voteCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) return
        else {
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
            val embedColor = Color(randomColor, randomColor1, randomColor2)

            val embed = EmbedBuilder()
                    .setTitle("vote for the bot", event.jda.selfUser.avatarUrl)
                    .setColor(embedColor)
                    .setDescription("Uni Upvote: [upvote me](https://discordbots.org/bot/${event.jda.selfUser.idLong}/vote)")
                    .setFooter("requested by ${event.author.name}#${event.author.discriminator} (${event.author.id})", "${event.author.avatarUrl}")
                    .build()
            event.textChannel.sendMessage(embed).queue()
        }
    }
}