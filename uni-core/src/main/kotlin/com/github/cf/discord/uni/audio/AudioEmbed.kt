/*
 *   Copyright (C) 2017-2019 computerfreaker
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.cf.discord.uni.audio

import com.github.cf.discord.uni.Lib
import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import com.github.cf.discord.uni.bold
import com.github.cf.discord.uni.fastCeil
import com.github.cf.discord.uni.markdownUrl
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDetection
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Color

object AudioEmbed {
    @JvmStatic
    val EMBED_COLOUR = Color(125, 165, 222)
    private const val MAX_PER_PAGE: Int = 5
    const val ICON_URL = "https://i.imgur.com/OnSCGuL.png"

    private fun embed(title: String): EmbedBuilder = EmbedBuilder().audioDefaults(title)

    fun addedTrack(track: AudioTrack, remaining: Int): MessageEmbed {
        if (track.userData != null) {
            val requester = track.userData!! as User
            return embed("Added Track")
                    .setDescription("Adding track to audio player queue:$LINE_SEPARATOR**${track.info.titleLink()}${
                    if (track.info.isStream)
                        ""
                    else " (${track.duration.toDurationString()})"}**")
                    .footerMessage(remaining, requester)
                    .build()
        } else {
            return embed("Added Track")
                    .setDescription("Adding track to audio player queue:$LINE_SEPARATOR**${track.info.titleLink()}${
                    if (track.info.isStream)
                        ""
                    else " (${track.duration.toDurationString()})"}**")
                    .setFooter("$remaining tracks left in queue.", "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                    .build()
        }
    }

    fun addedPlaylist(tracks: List<AudioTrack>, selectedTrack: AudioTrack, remaining: Int, playlistName: String, playlistUrl: String, requester: User): MessageEmbed =
            embed("Added Playlist")
                    .setDescription("Adding playlist to audio player:$LINE_SEPARATOR${playlistName.markdownUrl(playlistUrl).bold()}")
                    .addField("First Up", "${selectedTrack.info.titleLink()}${
                    if (selectedTrack.info.isStream)
                        ""
                    else " (${selectedTrack.duration.toDurationString()})"}".bold(), false)
                    .setFooter("$remaining tracks left in queue (${tracks.size} tracks queued from playlist). ${requester.footer()}", requester.effectiveAvatarUrl)
                    .build()

    fun errorEmbed(title: String, msg: String): MessageEmbed =
            embed(title)
                    .setDescription(msg)
                    .build()

    fun nowPlayingEmbed(track: AudioTrack, remaining: Int): MessageEmbed {
        if (track.userData != null) {
            val requester = track.userData!! as User
            return embed("Now Playing")
                    .setDescription("${track.info.titleLink()}${if (track.info.isStream) "" else " (${track.duration.toDurationString()})"}".bold())
                    .footerMessage(remaining, requester)
                    .build()
        } else {
            return embed("Now Playing")
                    .setDescription("${track.info.titleLink()}${if (track.info.isStream) "" else " (${track.duration.toDurationString()})"}".bold())
                    .setFooter("$remaining tracks left in queue.", "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                    .build()
        }
    }

    fun nowPlayingPaginatedEmbed(audioManager: LavaplayerAudioManager, channel: TextChannel, requester: User, page: Int): AudioCurrentlyPlayingEmbed {
        val paginatedEmbed = AudioCurrentlyPlayingEmbed(channel, requester, audioManager, MAX_PER_PAGE, page, fastCeil(audioManager.remaining(), MAX_PER_PAGE))
        paginatedEmbed.embedBuilder.audioDefaults("Currently Playing").setFooter(requester.footer(), requester.effectiveAvatarUrl)
        paginatedEmbed.updateEmbedBuilder()
        return paginatedEmbed
    }

    fun searchEmbed(audioManager: LavaplayerAudioManager, channel: TextChannel, requester: User, tracks: List<AudioTrack>): AudioSearchEmbed {
        val searchEmbed = AudioSearchEmbed(audioManager, channel, requester, tracks)
        searchEmbed.embedBuilder.audioDefaults("Audio Search").setFooter(requester.footer(), requester.effectiveAvatarUrl)
                .appendDescription("**")
        tracks.forEachIndexed { i, track ->
            searchEmbed.embedBuilder.appendDescription("${(i + 1) % 10}. ${track.info.titleLink()}${Lib.LINE_SEPARATOR}")
        }
        searchEmbed.embedBuilder.appendDescription("**")
        return searchEmbed
    }

    fun shuffleEmbed(track: AudioTrack?, remaining: Int, requester: User): MessageEmbed =
            embed("Shuffling Tracks")
                    .setDescription(
                            if (track != null) {
                                "Shuffled all upcoming tracks.${Lib.LINE_SEPARATOR}**Up next: ${track.info.titleLink()}**"
                            } else {
                                "There are no tracks to shuffle."
                            }
                    ).footerMessage(remaining, requester)
                    .build()

    fun skipEmbed(track: AudioTrack?): MessageEmbed =
            embed("Next Track")
                    .setDescription(
                            if (track != null)
                                "Skipping to next track:$LINE_SEPARATOR${track.info.titleLink().bold()}"
                            else "There are no more tracks left to play.")
                    .build()

    fun stopEmbed(remaining: Int, requester: User): MessageEmbed =
            embed("Stopped Playing")
                    .setDescription("Stopping the audio player...")
                    .footerMessage(remaining, requester)
                    .build()

    fun clearPlaying(): MessageEmbed =
            embed("cleared playlist")
                    .setDescription("cleared the current playlist")
                    .build()
}

fun EmbedBuilder.audioDefaults(title: String): EmbedBuilder = this.setColor(AudioEmbed.EMBED_COLOUR).setAuthor(title, null, AudioEmbed.ICON_URL)

/**
 * Returns a markdown formatted song title with a clickable URL source link.
 */
fun AudioTrackInfo.titleLink(): String = "[${if (this.title == MediaContainerDetection.UNKNOWN_TITLE) this.uri else this.title.replace("**", "\\*\\*")}](${this.uri})"

/**
 * Returns the duration timestamp in HH:mm:ss of a given long number.
 */
fun Long.toDurationString(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours >= 1) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
    } else {
        String.format("%d:%02d", minutes % 60, seconds % 60)
    }
}

/**
 * Returns the footer message containing information about tracks left in queue and the next song.
 */
fun EmbedBuilder.footerMessage(remaining: Int, requester: User): EmbedBuilder {
    return this.setFooter("$remaining tracks left in queue. ${requester.footer()}", requester.effectiveAvatarUrl)
}

fun User.footer(): String = "➤ Requested by ${this.name}#${this.discriminator}"
