package com.github.cf.discord.uni.commands.owner

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.data.authorOnly.authors
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color

@CommandGroup("owner")
class AmIOwnerCommand(){
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "amiowner",
            aliases = ["amiowner"],
            description = "see in which guilds i'm admin in",
            usage = "<>"
    )
    @Permissions(
            allowDm = true
    )
    fun amIOwner(context: CommandContext, event: MessageReceivedEvent){
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
        val embedColor = Color(randomColor, randomColor1, randomColor2)
        val author = event.author
        if(author!!.isBot) return
        if (event.author.idLong == 138302166619258880){
            event.channel.sendMessage(EmbedBuilder()
                    .setAuthor("Am I Owner", null, "${event.author.avatarUrl}")
                    .setColor(embedColor)
                    .setDescription("Well since you created me <@!${event.author.id}> yes you are <:OhISee:397902772865073154><a:Sakura:404593137059627009>\n " +
                            "I mean you created the source code so why shouldn't you be my owner\n "+
                    "In the end... Yes you are my owner")
                    .build()
            ).queue()
        }else{
            event.channel.sendMessage(EmbedBuilder()
                    .setAuthor("Am I Owner", null, "${event.author.avatarUrl}")
                    .setColor(embedColor)
                    .setDescription("To make a long story short no <a:Sakura:404593137059627009>\n ")
                    .build()
            ).queue()
        }
    }
}