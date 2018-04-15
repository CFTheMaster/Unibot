package com.github.cf.discord.uni.commands.owner

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.data.authorOnly
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONObject
import java.awt.Color

@CommandGroup("owner")
class TestApiCommand {
    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "api",
            aliases = ["testapi"],
            description = "just a test",
            usage = "<test>"
    )
    @Permissions(
            allowDm = true
    )
    fun onCommand(context: CommandContext, event: MessageReceivedEvent){
        if(event.textChannel.isNSFW) {
            if (event.message.author.id in authorOnly.authors) {
                val author = event.author
                if(author!!.isBot) {
                    return
                } else {
                    val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
                    val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
                    val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
                    val embedColor = Color(randomColor, randomColor1, randomColor2)

                    val myAss = getTestApi()

                    val embed = EmbedBuilder()
                            .setAuthor("hentai in my city", "$myAss", null)
                            .setColor(embedColor)
                            .setImage("$myAss")
                            .build()
                    event.channel.sendMessage(embed).queue()
                }
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "animeapi",
            aliases = ["animetestapi"],
            description = "just a test",
            usage = "<test>"
    )
    @Permissions(
            allowDm = true
    )
    fun onAnimeCommand(context: CommandContext, event: MessageReceivedEvent){
        if (event.message.author.id in authorOnly.authors) {
            val author = event.author
            if(author!!.isBot) {
                return
            } else {
                val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt();
                val embedColor = Color(randomColor, randomColor1, randomColor2)

                val ohMyGod = getAnimeTestApi()

                val embed = EmbedBuilder()
                        .setAuthor("anime in my city", "$ohMyGod", null)
                        .setColor(embedColor)
                        .setImage("$ohMyGod")
                        .build()
                event.channel.sendMessage(embed).queue()
            }
        }
    }

    private fun getTestApi(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://computerfreaker.cf/api/hentai/read.php")
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

    private fun getAnimeTestApi(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://computerfreaker.cf/api/anime/read.php")
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