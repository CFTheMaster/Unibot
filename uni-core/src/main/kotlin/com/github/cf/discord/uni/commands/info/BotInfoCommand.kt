package com.github.cf.discord.uni.commands.info

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.*
import net.dv8tion.jda.core.JDAInfo
import java.awt.Color
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@CommandGroup("system")
class BotInfoCommand {

    private val startTime = Instant.now()

    @Command(
            prefix = "${EnvVars.PREFIX}",
            aliases = ["bot", "botinfo"],
            id = "bot",
            description = "get the bot info for this bot"
    )
    @Permissions(
            allowDm = true
    )
    fun botInfoCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if (author!!.isBot) return
        else {
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val embedColor = Color(randomColor, randomColor1, randomColor2)

            val time =  OffsetDateTime.parse(event.jda.selfUser.creationTime.toString()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val embed = EmbedBuilder()
                    .setAuthor("Bot Info", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                    .setColor(embedColor)
                    .addField("Bot Devs: ", "computerfreaker#4054\n<@!138302166619258880>", true)
                    .addField("Bot Name: ", "${event.jda.selfUser.name}", true)
                    .addField("Bot Id: ", "${event.jda.selfUser.id}", true)
                    .addField("JDA Version: ", "${JDAInfo.VERSION}", true)
                    .addField("System Uptime: ", "${Duration.between(startTime, Instant.now()).formatDuration()}", true)
                    .addField("Guild Count: ", "${event.jda.guilds.size}", true)
                    .addField("Creation Date: ", "$time", true)
                    .addField("Uni invite: ", "[invite me](https://discordapp.com/oauth2/authorize?client_id=${event.jda.selfUser.id}&scope=bot&permissions=2146958591)", true)
                    .addField("Support Server Invite: ", "[support server](https://discord.gg/WmDyx7C)", true)
                    .addField("CFs API server", "[API server](https://discord.gg/gzWwtWG )", true)
                    .build()
            event.textChannel.sendMessage(embed).queue()
        }
    }

    private fun Duration.formatDuration(): String {
        val seconds = Math.abs(this.seconds)
        return "**${seconds / 86400}**d **${(seconds % 86400) / 3600}**h **${(seconds % 3600) / 60}**min **${seconds % 60}**s"
    }
}