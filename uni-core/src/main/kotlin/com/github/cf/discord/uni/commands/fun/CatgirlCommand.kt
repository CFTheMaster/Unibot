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
import org.jetbrains.kotlin.js.parser.sourcemaps.parseJson
import org.json.JSONObject
import java.awt.Color

@CommandGroup("fun")
class CatgirlCommand {
    companion object {
        const val BASE_URL = "https://nekos.life/api/v2/img/neko"
    }
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "neko",
            aliases = ["catgirl", "neko"],
            description = "get a random catgirl.",
            usage = "<execute the command and get a random catgirl>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
            val embedColor = Color(randomColor, randomColor1, randomColor2)

            val embed = EmbedBuilder()
                    .setColor(embedColor)
                    .setImage(getCatgirl())
                    .setFooter("powered by https://nekos.life", null)
                    .build()
            event.channel.sendMessage(embed).queue()
        }
    }

    private fun getCatgirl(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url(BASE_URL)
                .build()).execute()

        if (response.isSuccessful) {
            val content = JSONObject(response.body()?.string())
            response.body()?.close()
            return content.getString("url")
        } else {
            response.body()?.close()
            return null
        }
    }
}