package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.utils.CFApi
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Load
@Alias("lewdneko")
class LewdCatgirl : Command(){
    override val desc = "Change the volume of the music"
    override val guildOnly = false
    override val nsfw = true

    override fun run(ctx: Context) {
        val catgirl = if (CFApi.getCFApi("nsfwneko") != null) CFApi.getCFApi("nsfwneko") else getNeko()

        val embed = EmbedBuilder().apply {
            setTitle("image link", catgirl)
            setColor(6684876)
            setImage(catgirl)
            setFooter("powered by https://nekos.life", null)
        }
        ctx.send(embed.build())
    }

    private fun getNeko(): String?{
        val response = OkHttpClient().newCall(Request.Builder()
                .url("https://nekos.life/api/v2/img/lewd")
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
