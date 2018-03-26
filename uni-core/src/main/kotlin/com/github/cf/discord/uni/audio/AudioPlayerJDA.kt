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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User

interface AudioPlayerJDA {
    fun addTrack(track: AudioTrack)
    fun loadAndPlay(requester: User, channel: TextChannel, trackUrl: String, playFirstOnly: Boolean)
    fun searchAndPlay(requester: User, channel: TextChannel, trackUrl: String)
    fun skip(requester: User, channel: TextChannel)
    fun shuffle(requester: User, channel: TextChannel)
    fun stop(requester: User, channel: TextChannel)
    fun nowPlaying(channel: TextChannel, track: AudioTrack)
    fun clear(requester: User, channel: TextChannel)
    fun remaining(): Int
    fun tracks(): List<AudioTrack>
    fun currentTrack(): AudioTrack?
}
