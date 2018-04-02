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
class LewdCatgirlCommand {
    companion object {
        const val BASE_URL = "https://nekos.life/api/v2/img/lewd"
    }
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "lewdneko",
            aliases = ["lewdcatgirl", "lewdneko"],
            description = "get a random lewd catgirl.",
            usage = "<execute the command and get a random lewd catgirl>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        val author = event.author
        val textChannel = event.textChannel
        if (textChannel!!.isNSFW){
            if(author!!.isBot) {
                return
            } else {
                val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val embedColor = Color(randomColor, randomColor1, randomColor2)

                val embed = EmbedBuilder()
                        .setColor(embedColor)
                        .setImage(getLewdCatgirl())
                        .setFooter("powered by https://nekos.life", null)
                        .build()
                event.channel.sendMessage(embed).queue()
            }
        }else{
            event.message.channel.sendMessage("no lewds here try a NSFW enabled channel").queue()
        }
    }

    private fun getLewdCatgirl(): String {
        val json = JSONObject(OkHttpClient().newCall(Request.Builder().url(BASE_URL).build()).execute().body()!!.string())
        return json.getString("url")
    }
}