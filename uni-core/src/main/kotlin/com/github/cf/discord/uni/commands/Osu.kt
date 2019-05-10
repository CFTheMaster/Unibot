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
import okhttp3.Request
import org.json.JSONArray
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

        Http.get(HttpUrl.Builder().apply {
            scheme("https")
            host("osu.ppy.sh")
            addPathSegment("api")
            addPathSegment("get_user")
            addQueryParameter("k", EnvVars.OSU_TOKEN)
            addQueryParameter("u", username)
            addQueryParameter("m", mode)
            addQueryParameter("type", "string")
        }.build()).thenAccept { res ->
            val body = res.body()!!.string()
            val json = JSONArray(body)

            if (json.length() == 0) {
                return@thenAccept ctx.send("user not found")
            }

            val user = json.getJSONObject(0)

            Http.get(HttpUrl.Builder().apply {
                scheme("https")
                host("osu.ppy.sh")
                addPathSegment("api")
                addPathSegment("get_user_best")
                addQueryParameter("k", EnvVars.OSU_TOKEN)
                addQueryParameter("u", user.getString("user_id"))
                addQueryParameter("m", mode)
                addQueryParameter("type", "id")
            }.build()).thenAccept { bestRes ->
                val bestBody = bestRes.body()!!.string()
                val bestJson = JSONArray(bestBody)

                val embed = EmbedBuilder().apply {
                    setTitle("${user.getString("username")} (${user.getString("country")})", "https://osu.ppy.sh/users/${user.getString("user_id")}")
                    setColor(Color(232, 102, 160))
                    descriptionBuilder.append("**Level:** ${floor(user.getString("level").toFloat()).roundToInt()}\n")
                    descriptionBuilder.append("**Plays:** ${user.getString("playcount")}\n")
                    descriptionBuilder.append("**Accuracy:** ${round(user.getString("accuracy").toFloat() * 100) / 100}%\n")
                    descriptionBuilder.append("**Score:** ${user.getString("ranked_score")}\n")
                    descriptionBuilder.append("**Rank:** ${user.getString("pp_rank")}\n")
                    descriptionBuilder.append("**PP:** ${user.getString("pp_raw").toFloat().roundToInt()}\n")
                    descriptionBuilder.append("\n\uD83C\uDFC6 **Best Plays** \uD83C\uDFC6")

                    for (i in 0 until min(bestJson.length(), 5)) {
                        val best = bestJson.getJSONObject(i)

                        val beatmapRes = Http.okhttp.newCall(Request.Builder().apply {
                            // TODO use Http.get here
                            url(HttpUrl.Builder().apply {
                                scheme("https")
                                host("osu.ppy.sh")
                                addPathSegment("api")
                                addPathSegment("get_beatmaps")
                                addQueryParameter("k", EnvVars.OSU_TOKEN)
                                addQueryParameter("b", best.getString("beatmap_id"))
                            }.build())
                        }.build()).execute()

                        val beatmapBody = beatmapRes.body()!!.string()
                        val beatmap = JSONArray(beatmapBody).getJSONObject(0)

                        val rank = best.getString("rank").replace("X", "SS")

                        addField(
                                "${beatmap.getString("artist")} - ${beatmap.getString("title")} [${beatmap.getString("version")}] ($rank)",
                                "**Score:** ${best.getString("score")}\n" +
                                        "**Combo:** ${best.getString("maxcombo")}\n" +
                                        "**PP:** ${best.getString("pp").toFloat().roundToInt()}",
                                true
                        )

                        beatmapRes.close()
                    }
                }.build()

                ctx.send(embed)
                res.close()
                bestRes.close()
            }.thenApply {}.exceptionally {
                LOGGER.error("Error while trying to get osu best plays", it)
                ctx.sendError(it)
            }
        }
    }
}