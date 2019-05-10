package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Flag
import com.github.cf.discord.uni.annotations.Flags
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.ThreadedCommand
import com.github.cf.discord.uni.utils.Http
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Color
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

@Load
@Argument("username", "string")
@Flags(
        Flag("taiko", 't', "View taiko stats"),
        Flag("mania", 'm', "View osu!mania stats"),
        Flag("catch", 'c', "View catch the beat stats")
)
class Osu : ThreadedCommand() {
    override fun threadedRun(ctx: Context) {
        val username = ctx.args["username"] as String
        val mode = when {
            ctx.flags.argMap.containsKey("taiko") || ctx.flags.argMap.containsKey("t") -> "1"
            ctx.flags.argMap.containsKey("catch") || ctx.flags.argMap.containsKey("c") -> "2"
            ctx.flags.argMap.containsKey("mania") || ctx.flags.argMap.containsKey("m") -> "3"
            else -> "0"
        }

        ctx.send(EmbedBuilder().apply {
            setTitle("User Score For User: $username", null)
            setImage(getOsuImage(username, mode))
            setColor(ctx.member?.color)
            setFooter("Image Provided By https://www.lemmmy.pw/osusig", null)
        }.build())
    }

    private fun getOsuImage(username: String, mode: String): String?{
        val response = OkHttpClient().newCall(Request.Builder()
                .url("http://lemmmy.pw/osusig/sig.php?colour=hex8866ee&uname=$username&mode=$mode&removeavmargin&flagshadow&flagstroke&darkheader&darktriangles&opaqueavatar&avatarrounding=4&rankedscore&onlineindicator=undefined&xpbar&xpbarhex")
                .build()).execute()

        return if (response.isSuccessful) {
            response.toString()
        } else {
            response.body()?.close()
            null
        }
    }
}