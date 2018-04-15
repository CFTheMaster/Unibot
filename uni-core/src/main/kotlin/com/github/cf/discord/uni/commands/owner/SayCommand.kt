package com.github.cf.discord.uni.commands.owner

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.data.authorOnly
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

@CommandGroup("owner")
class SayCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "say",
            aliases = ["say", "repeat"],
            description = "Repeat the current message",
            usage = "<input to make it repeat a message>"
    )
    @Permissions(
            allowDm = true
    )
    fun sayCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) return
        else if(event.message.author.id in authorOnly.authors){
            event.channel.sendMessage(context.args).queue()
        }else{
            event.channel.sendMessage(EmbedBuilder()
                    .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                    .setTitle("Please don't do this command")
                    .setDescription("doing this command makes me angry please don't do it again <:OhISee:397902772865073154>")
                    .build()
            ).queue()
        }
    }
}