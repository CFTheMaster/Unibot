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
package com.github.cf.discord.uni.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel

class GuildMusicManager(manager: AudioPlayerManager, val textChannel: TextChannel, val voiceChannel: VoiceChannel) {
    val player: AudioPlayer = manager.createPlayer()
    val scheduler = TrackScheduler(player, this)
    val sendingHandler = AudioPlayerSendHandler(player)
    val voteSkip = mutableListOf<String>()

    var autoplay = false

    init {
        player.addListener(scheduler)
        player.volume = 50
    }
}
