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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

abstract class AudioEventAdapter : AudioEventListener {

    /**
     * @param player Audio player
     */
    protected open fun onPlayerPause(player: AudioPlayer) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     */
    protected open fun onPlayerResume(player: AudioPlayer) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track that started
     */
    protected open fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track that ended
     * @param endReason The reason why the track stopped playing
     */
    protected open fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track where the exception occurred
     * @param exception The exception that occurred
     */
    protected open fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track where the exception occurred
     * @param thresholdMs The wait threshold that was exceeded for this event to trigger
     */
    protected open fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        // Adapter dummy method
    }

    override fun onEvent(event: AudioEvent) {
        when (event) {
            is PlayerPauseEvent -> onPlayerPause(event.player)
            is PlayerResumeEvent -> onPlayerResume(event.player)
            is TrackStartEvent -> onTrackStart(event.player, event.track)
            is TrackEndEvent -> onTrackEnd(event.player, event.track, event.endReason)
            is TrackExceptionEvent -> onTrackException(event.player, event.track, event.exception)
            is TrackStuckEvent -> onTrackStuck(event.player, event.track, event.thresholdMs)
        }
    }
}
