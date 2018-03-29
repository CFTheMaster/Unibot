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
package com.github.cf.discord.uni.commands.audio

import com.github.cf.discord.uni.Lib
import com.github.cf.discord.uni.audio.LavaplayerAudioManager
import com.github.cf.discord.uni.audio.audioDefaults
import com.github.cf.discord.uni.audio.titleLink
import com.github.cf.discord.uni.audio.toDurationString
import com.github.cf.discord.uni.bold
import mu.KotlinLogging
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import reactor.core.Disposable
import reactor.core.scheduler.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.min

private val LOGGER = KotlinLogging.logger { }
class AudioPlayerPanel(
        private val channel: TextChannel,
        private val audioManager: LavaplayerAudioManager
) {

    private val embedBuilder = EmbedBuilder()

    companion object {
        private val schedulers = Schedulers.newElastic(AudioPlayerPanel::class.java.simpleName)
    }

    // The message id after the embed is sent
    lateinit var message: Message
    lateinit var scheduler: Disposable

    init {
        this.embedBuilder.audioDefaults("Audio Player Panel")
    }

    fun start(): AudioPlayerPanel {
        channel.sendMessage(this.updateEmbedBuilder().build()).queue {
            LOGGER.debug { "Creating a scheduler to update the AudioPlayerPanel every 5 seconds in channel ${channel.idLong} of guild ${channel.guild.idLong}." }
            message = it
            scheduler = schedulers.schedulePeriodically(
                    {
                        message.editMessage(this.updateEmbedBuilder().build()).queue()
                    },
                    0,
                    5000,
                    TimeUnit.MILLISECONDS
            )
        }
        return this
    }

    fun dispose() {
        LOGGER.debug { "Disposing the periodic update scheduler for the AudioPlayerPanel in channel ${channel.idLong} of guild ${channel.guild.idLong}." }
        scheduler.dispose()
    }

    private fun updateEmbedBuilder(): EmbedBuilder {
        val track = audioManager.currentTrack()
        embedBuilder.clearFields()
        if (track != null) {
            embedBuilder.setDescription("${track.info.titleLink()}${if (!track.info.isStream) "${Lib.LINE_SEPARATOR}${track.position.toDurationString()}/${track.info.length.toDurationString()}" else ""}".bold())
                    .addField("Next up", nextTracksInfo(), false)
        } else {
            embedBuilder.setDescription("No tracks are currently playing.")
        }
        return embedBuilder
    }

    private fun nextTracksInfo(): String {
        val tracks = audioManager.tracks()
        return if (tracks.isEmpty()) {
            "No tracks are left in the queue."
        } else tracks.subList(0, min(3, audioManager.remaining()))
                .joinToString(separator = Lib.LINE_SEPARATOR, prefix = "**", postfix = "**") { it.info.titleLink() }
    }
}
