package com.github.cf.discord.uni.commands.info

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.awt.Color
import java.time.format.DateTimeFormatter

@CommandGroup("system")
class ServerInfoCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            aliases = ["server", "serverinfo", "guild", "guildinfo"],
            id = "serverinfo",
            description = "get the current server info"
    )
    fun serverInfoCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) return
        else{
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
            val embedColor = Color(randomColor, randomColor1, randomColor2)
            val embed = EmbedBuilder()
                    .setAuthor("Server/Guild Info", null, "${event.guild.iconUrl}")
                    .setThumbnail("${event.guild.iconUrl}")
                    .setColor(embedColor)
                    .addField("Guild Name: ", "${event.guild.name}", true)
                    .addField("Guild ID: ", "${event.guild.id}", true)
                    .addField("Guild Owner: ", "${event.guild.owner.user.name}", true)
                    .addField("Guild Region: ", "${event.guild.region}", true)
                    .addField("Guild Creation Date: ", "${event.guild.creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                    .addField("Guild Members: ", "${event.guild.members.size}", true)
                    .addField("Highest role: ", "${event.guild.roles.get(0).name ?: "none"}\n", true)
                    .addField("Text Channels: ", "${event.guild.textChannels.size}", true)
                    .addField("Voice Channels: ", " ${event.guild.voiceChannels.size} ", true)
                    .addField("Custom Emojis: ", "${event.guild.emotes.size}", true)
                    .setFooter("requested by ${event.author.name}#${event.author.discriminator} (${event.author.id})", "${event.author.avatarUrl}")
                    .build()
            event.channel.sendMessage(embed).queue()
        }
    }
}