package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.utils.CFApi
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.awt.Color

@Load
class Hentai : Command(){
    override val nsfw = true
    override val desc = "execute to get a random picture from my hentai API"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val myAss = CFApi.getCFApi("hentai")

        val embed = EmbedBuilder().apply {
            setAuthor("hentai in my city", "$myAss", "https://computerfreaker.cf/profile/profile.png")
            setColor(embedColor)
            setImage("$myAss")
            setFooter("powered by: https://api.computerfreaker.cf", "${ctx.jda.getUserById(138302166619258880).avatarUrl}")
        }

        ctx.send(embed.build())

    }
}