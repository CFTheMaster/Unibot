package com.github.cf.discord.uni.music

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.utils.Http
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.core.EmbedBuilder
import org.json.JSONObject
import java.awt.Color
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.timerTask

class TrackScheduler(private val player: AudioPlayer, private val manager: GuildMusicManager) : AudioEventAdapter() {
    val queue = LinkedBlockingQueue<AudioTrack>()

    fun add(track: AudioTrack) {
        if (!player.startTrack(track, true)) queue.offer(track)
    }

    fun next() = player.startTrack(queue.poll(), false)

    fun shuffle() {
        val tracks = queue.shuffled()

        queue.clear()

        queue += tracks
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val nextTrack = queue.peek()
        val embed = EmbedBuilder()

        embed.setTitle("Now playing: ${track.info.title}")
        embed.setColor(Color.CYAN)

        if (nextTrack != null) {
            embed.setFooter("Next: ${nextTrack.info.title}", null)
        }

        manager.textChannel.sendMessage(embed.build()).queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            val nextTrack = queue.peek()
            val embed = EmbedBuilder()

            embed.setTitle("Track finished: ${track.info.title}")
            embed.setColor(Color.CYAN)

            if (nextTrack != null) {
                embed.setFooter("Next: ${nextTrack.info.title}", null)
            }

            if (manager.autoplay && track.info.uri.indexOf("youtube") > -1) {
                val qs = "?key=${EnvVars.GOOGLE_API_KEY}&part=snippet&maxResults=10&type=video&relatedToVideoId=${track.info.identifier}"

                Http.get("https://www.googleapis.com/youtube/v3/search$qs").thenAccept { res ->
                    val id = JSONObject(res.body()!!.string())
                            .getJSONArray("items")
                            .getJSONObject(0)
                            .getJSONObject("id")
                            .getString("videoId")

                    MusicManager.playerManager.loadItem("https://youtube.com/watch?v=$id", object : AudioLoadResultHandler {
                        override fun loadFailed(exception: FriendlyException)
                                = manager.textChannel.sendMessage("[autoplay] Failed to add song to queue: ${exception.message}").queue()
                        override fun noMatches() = manager.textChannel.sendMessage("[autoplay] YouTube url is (probably) invalid!").queue()
                        override fun trackLoaded(track: AudioTrack) = manager.scheduler.add(track)
                        override fun playlistLoaded(playlist: AudioPlaylist) = trackLoaded(playlist.tracks.first())
                    })
                }
            } else {
                MusicManager.inactivityScheduler.schedule(timerTask {
                    if (player.playingTrack != null || !manager.textChannel.guild.audioManager.isConnected) {
                        return@timerTask
                    }

                    manager.textChannel.sendMessage("Left voicechannel because of inactivity").queue()
                    MusicManager.leave(manager.textChannel.guild.id)
                }, 300000L)
            }
            manager.textChannel.sendMessage(embed.build()).queue()
            next()
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException)
            = manager.textChannel.sendMessage("Error occurred while playing music: ${exception.message}").queue()
}