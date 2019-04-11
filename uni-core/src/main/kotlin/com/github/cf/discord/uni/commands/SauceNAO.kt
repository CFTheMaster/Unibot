package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color

@Load
class SauceNAO : Command(){
    override val desc = "post an attachement with your command"
    override val guildOnly = true

    override fun run(ctx: Context) {
        fun getSauceNAO(): String? {
            val response = OkHttpClient().newCall(Request.Builder()
                    .url("https://saucenao.com/search.php?db=999&output_type=2&api_key=${EnvVars.SAUCENAO!!}&numres=1&url=${ctx.msg.attachments.first().url}")
                    .build()).execute()

            var result: String? = null
            if(response.isSuccessful)
                result = JSONObject(response.body()?.string()).getJSONArray("results").getJSONObject(0).getJSONObject("data").getJSONArray("ext_urls").getString(0)


            response.body()?.close()
            return result
        }

        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val embed = EmbedBuilder().apply{
            setColor(embedColor)
            setDescription(getSauceNAO())
            setFooter("Image Sauce", null)
        }
        ctx.send(embed.build())
    }
}