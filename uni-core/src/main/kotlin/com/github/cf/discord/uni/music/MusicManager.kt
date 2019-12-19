/*
 *   Copyright (C) 2017-2020 computerfreaker
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

import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.entities.Context
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.VoiceChannel
import java.util.*

object MusicManager {
    val playerManager = DefaultAudioPlayerManager()
    val musicManagers = mutableMapOf<String, GuildMusicManager>()
    val inactivityScheduler = Timer(true)

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)
    }

    fun join(ctx: Context): GuildMusicManager {
        LOGGER.info("New voice connection in guild ${ctx.guild!!.name}!")
        val manager = GuildMusicManager(playerManager, ctx.event.textChannel, ctx.member!!.voiceState!!.channel as VoiceChannel)
        musicManagers[ctx.guild.id] = manager
        ctx.guild.audioManager.openAudioConnection(ctx.member.voiceState?.channel)
        ctx.guild.audioManager.sendingHandler = manager.sendingHandler
        return manager
    }

    fun leave(guild: String): Boolean {
        LOGGER.info("Voice connection ended in guild with id $guild!")
        val manager = musicManagers[guild] ?: return false
        manager.player.stopTrack()
        manager.scheduler.queue.clear()
        manager.voiceChannel.guild.audioManager.closeAudioConnection()
        musicManagers.remove(guild)
        return true
    }
}
