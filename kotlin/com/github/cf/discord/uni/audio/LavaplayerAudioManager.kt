/*
 *   Copyright (C) 2017-2018 computerfreaker
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

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User

/**
 * Per-guild audio manager
 */
class LavaplayerAudioManager(private val playerManager: AudioPlayerManager) : AudioPlayerJDA {

    companion object {
        // Embed settings
        private const val MAX_QUERY_SIZE = 9
    }

    // Create an audio player for this guild
    val player: AudioPlayer = playerManager.createPlayer()
    // Create a track scheduler for the audio player
    private val scheduler: TrackScheduler = TrackScheduler(player)

    init {
        player.addListener(scheduler)
    }

    fun sendHandler(): AudioPlayerSendHandler = AudioPlayerSendHandler(player)

    override fun loadAndPlay(requester: User, channel: TextChannel, trackUrl: String, playFirstOnly: Boolean) {
        playerManager.loadItemOrdered(this, trackUrl, object : DefaultAudioLoadResultHandler(this, channel, trackUrl) {
            override fun playlistLoaded(playlist: AudioPlaylist) {
                val tracks = playlist.tracks
                if (tracks.isEmpty()) {
                    // Send an errorEmbed message embed if no tracks could be found
                    channel.sendMessage(AudioEmbed.errorEmbed("Oops!", "No playlist could be found with that query.")).queue()
                } else {
                    if (playFirstOnly) {
                        // Play only the first search result
                        val track = tracks[0]
                        track.userData = requester
                        this.trackLoaded(track)
                    } else {
                        // Play all the tracks found in the playlist
                        tracks.forEach { it.userData = requester }
                        // Send embed message to notify users that a playlist was added to the audio player queue
                        channel.sendMessage(AudioEmbed.addedPlaylist(
                                tracks,
                                playlist.selectedTrack ?: tracks[0],
                                audioManager.remaining() + tracks.size,
                                playlist.name,
                                trackUrl,
                                requester)
                        ).queue()
                        // Enqueue audio tracks to scheduler
                        audioManager.scheduler.queue(tracks)
                    }
                }
            }
        })
    }

    override fun searchAndPlay(requester: User, channel: TextChannel, trackUrl: String) {
        playerManager.loadItemOrdered(this, trackUrl, object : DefaultAudioLoadResultHandler(this, channel, trackUrl) {
            override fun playlistLoaded(playlist: AudioPlaylist) {
                // Get a max of 9 tracks
                val tracks = if (playlist.tracks.size > MAX_QUERY_SIZE) playlist.tracks.subList(0, MAX_QUERY_SIZE) else playlist.tracks

                // Send an embed message containing choices and reaction listeners
                AudioEmbed.searchEmbed(audioManager, channel, requester, tracks).queue()
            }
        })
    }

    override fun skip(requester: User, channel: TextChannel) {
        channel.sendMessage(AudioEmbed.skipEmbed(scheduler.peek())).queue()
        // Skip to next track
        scheduler.nextTrack()
    }

    override fun shuffle(requester: User, channel: TextChannel) {
        // Shuffles all upcoming tracks
        scheduler.shuffle()
        channel.sendMessage(AudioEmbed.shuffleEmbed(scheduler.peek(), scheduler.remaining(), requester)).queue()
    }

    override fun stop(requester: User, channel: TextChannel) {
        channel.sendMessage(AudioEmbed.stopEmbed(scheduler.remaining(), requester)).queue()
        // Stops audio player
        scheduler.stopTrack()
    }

    override fun nowPlaying(channel: TextChannel, track: AudioTrack) {
        channel.sendMessage(AudioEmbed.nowPlayingEmbed(track, scheduler.remaining())).queue()
    }

    override fun clear(requester: User, channel: TextChannel) {
        scheduler.clearAll()
    }

    override fun addTrack(track: AudioTrack) {
        scheduler.queue(track)
    }

    override fun remaining(): Int {
        return scheduler.remaining()
    }

    override fun tracks(): List<AudioTrack> {
        return scheduler.getTracks()
    }

    override fun currentTrack(): AudioTrack? {
        return scheduler.getCurrentTrack()
    }

    /**
     * A default audio loading handler for when:
     * - audio track failed to load
     * - audio track has been loaded
     * - no audio tracks could be found with given trackUrl
     */
    private abstract class DefaultAudioLoadResultHandler(
            protected val audioManager: LavaplayerAudioManager,
            protected val channel: TextChannel,
            protected val trackUrl: String
    ) : AudioLoadResultHandler {

        override fun loadFailed(exception: FriendlyException) {
            // Send a failure embed message to notify users the reason the track could not be loaded
            channel.sendMessage(AudioEmbed.errorEmbed("Oops!", "Failed to play the track: ${exception.message}")).queue()
        }

        override fun trackLoaded(track: AudioTrack) {
            // Send embed message to notify users that this track was added to the audio player queue
            channel.sendMessage(AudioEmbed.addedTrack(track, audioManager.remaining() + 1)).complete()
            // Enqueue the audio track to scheduler
            audioManager.scheduler.queue(track)
        }

        override fun noMatches() {
            // Send embed message to notify users that no matches to the trackUrl could be found
            channel.sendMessage(AudioEmbed.errorEmbed("Oops!", "No audio sources found for `$trackUrl`.")).queue()
        }
    }
}
