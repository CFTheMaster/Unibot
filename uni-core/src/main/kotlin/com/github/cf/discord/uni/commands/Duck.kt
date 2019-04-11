package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Load
@Alias("quack")
class Duck : Command(){
    override val desc = "Change the volume of the music"
    override val guildOnly = false

    override fun run(ctx: Context) {
        val embed = EmbedBuilder().apply {
            setTitle("image link", aDuck)
            setColor(6684876)
            setImage(aDuck)
            setFooter("powered by https://nekos.life", null)
        }
        ctx.send(embed.build())
    }

    private val aDuck = getDucky()

    private fun getDucky(): String? {
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://random-d.uk/api/v1/random")
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