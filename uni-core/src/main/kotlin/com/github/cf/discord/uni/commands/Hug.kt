package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.utils.CFApi
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Load
@Argument("user", "user", true)
class Hug : Command(){
    override val desc = "hug a user"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val smolHug = CFApi.getCFApi("hug")

        ctx.send(EmbedBuilder().apply {
            val mem = ctx.args["user"] as Member
            setTitle("${if (ctx.args["user"] == null || ctx.author.id == mem.user.id) "trying to hug yourself " else "${mem.user.name}, you got a hug from ${ctx.member!!.user.name}"}", smolHug)
            setImage(smolHug)
            setColor(6684876)
            setFooter("powered by: https://api.computerfreaker.cf", "${ctx.jda.getUserById(138302166619258880).avatarUrl}")
        }.build())
    }
}