package com.github.cf.discord.uni.commands.query

import com.github.cf.discord.uni.commands.`fun`.LewdCatgirlCommand
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

@CommandGroup("query")
class SauceNAOCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "saucenao",
            aliases = ["saucenao", "sauce"],
            description = "get the sauce from an image",
            usage = "<post an attachement with your command>"
    )
    @Permissions(
            allowDm = true
    )
    fun sauceNaoExecute(context: CommandContext, event: MessageReceivedEvent){
        fun getSauceNAO(): String? {
            val response = OkHttpClient().newCall(Request.Builder()
                    .url("https://saucenao.com/search.php?db=999&output_type=2&api_key=${EnvVars.SAUCENAO!!}&numres=1&url=${event.message.attachments.first().url}")
                    .build()).execute()

            var result: String? = null
            if(response.isSuccessful)
                result = JSONObject(response.body()?.string()).getString("results")

            response.body()?.close()
            return result
        }

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
                    .setDescription(getSauceNAO())
                    .setFooter("Image Sauce", null)
                    .build()
            event.channel.sendMessage(embed).queue()
        }
    }
}