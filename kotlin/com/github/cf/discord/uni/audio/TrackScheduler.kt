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
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadLocalRandom

class TrackScheduler(private val player: AudioPlayer) : AudioEventAdapter() {
    private val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    fun queue(track: AudioTrack) {
        if (player.playingTrack == null) {
            player.startTrack(track, true)
        } else {
            queue.offer(track)
        }
    }

    fun queue(tracks: List<AudioTrack>) {
        tracks.forEach {
            queue.offer(it)
        }
        if (player.playingTrack == null) {
            nextTrack(true)
        }
    }

    fun nextTrack(noInterrupt: Boolean = false) {
        player.startTrack(queue.poll(), noInterrupt)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    fun stopTrack() {
        player.stopTrack()
    }

    fun getTracks(): List<AudioTrack> {
        return queue.toList()
    }

    fun getCurrentTrack(): AudioTrack? {
        return player.playingTrack
    }

    fun getVolume(): Int {
        return player.volume
    }

    fun shuffle() {
        val tracks = queue.toMutableList()
        tracks.shuffle(ThreadLocalRandom.current())
        queue.clear()
        queue.addAll(tracks)
    }

    fun clearAll() {
        queue.clear()
    }

    fun peek(): AudioTrack? = queue.peek()

    fun remaining(): Int = queue.size
}
