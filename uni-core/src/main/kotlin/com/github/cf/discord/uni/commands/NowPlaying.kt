package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.ThreadedCommand
import com.github.cf.discord.uni.music.MusicManager
import com.github.cf.discord.uni.utils.Http
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONObject

@Load
@Alias("np")
class NowPlaying : ThreadedCommand(){
    override val desc = "Get the current song"
    override val guildOnly = true

    override fun threadedRun(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id]
                ?: return ctx.send("Not connected to a voice channel")

        val embed = EmbedBuilder().apply {
            setAuthor("now playing", null, null)
            setTitle(manager.player.playingTrack.info.title)

            val durMillis = manager.player.playingTrack.duration
            val durSecs = (durMillis / 1000) % 60
            val durMins = (durMillis / 60000) % 60
            val durHours = (durMillis / 3600000) % 24

            val duration = "%02d:%02d:%02d".format(durHours, durMins, durSecs)

            val posMillis = manager.player.playingTrack.position
            val posSecs = (posMillis / 1000) % 60
            val posMins = (posMillis / 60000) % 60
            val posHours = (posMillis / 3600000) % 24

            val position = "%02d:%02d:%02d".format(posHours, posMins, posSecs)

            val emote = if (manager.player.isPaused) "\u23F8" else "\u25B6"

            descriptionBuilder.append("$position/$duration $emote")
            setColor(6684876)
        }

        if(manager.scheduler.queue.isNotEmpty()) {
            embed.setFooter("next song: ${manager.scheduler.queue.peek().info.title}", null)
        } else if (manager.autoplay && manager.player.playingTrack.info.uri.indexOf("youtube") > -1){
            val res = Http.okhttp.newCall(Request.Builder().apply {
                url(HttpUrl.Builder().apply {
                    scheme("https")
                    host("www.googleapis.com")
                    addPathSegment("youtube")
                    addPathSegment("v3")
                    addPathSegment("search")
                    addQueryParameter("key", EnvVars.GOOGLE_API_KEY)
                    addQueryParameter("part", "snippet")
                    addQueryParameter("maxResults", "10")
                    addQueryParameter("type", "video")
                    addQueryParameter("relatedToVideoId", manager.player.playingTrack.info.identifier)
                }.build())
            }.build()).execute()

            val title = JSONObject(res.body()!!.string())
                    .getJSONArray("items")
                    .getJSONObject(0)
                    .getJSONObject("snippet")
                    .getString("title")

            embed.setFooter("next song to $title", null)
            res.close()
        }

        ctx.send(embed.build())
    }
}