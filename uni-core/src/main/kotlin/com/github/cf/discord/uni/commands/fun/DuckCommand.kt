package com.github.cf.discord.uni.commands.`fun`

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color

@CommandGroup("fun")
class DuckCommand {
    companion object {
        const val BASE_URL = "https://random-d.uk/api/v1/random"
    }
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "duck",
            aliases = ["duck", "quack"],
            description = "get a random duck.",
            usage = "<execute the command and get a random duck>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
            val embedColor = Color(randomColor, randomColor1, randomColor2)

            val embed = EmbedBuilder()
                    .setColor(embedColor)
                    .setImage(getDucky())
                    .setFooter("Powered by https://random-d.uk", null)
                    .build()
            event.channel.sendMessage(embed).queue()
        }
    }

    private fun getDucky(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url(BASE_URL)
                .build()).execute()

        return if (response.isSuccessful) {
            val content = JSONObject(response.body()?.string())
            response.body()?.close()
            content.getString("url")
        } else {
            response.body()?.close()
            null
        }
    }
}