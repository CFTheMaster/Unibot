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
class ChangeNickNameCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "nickname",
            aliases = ["nickname", "nick"],
            description = "Change the current nickname of the bot",
            usage = "<input to change the bot nickname>"
    )
    @Permissions(
            allowDm = true
    )
    fun changeNickName(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        val self = event.guild.selfMember
        if(author!!.isBot) return
        else if(event.message.author.id in authorOnly.authors){
            event.guild.controller.setNickname(self, context.args).queue()
        }
        else{
            event.channel.sendMessage(
                    EmbedBuilder()
                            .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                            .setTitle("Please don't do this command")
                            .setDescription("doing this command makes me angry please don't do it again <:OhISee:397902772865073154>")
                            .build()
            ).queue()
        }
    }
}