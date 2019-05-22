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

import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import com.github.cf.discord.uni.bold
import com.github.cf.discord.uni.embed.PaginatedEmbed
import com.github.cf.discord.uni.fastCeil
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import kotlin.math.min

class AudioCurrentlyPlayingEmbed(
    channel: TextChannel,
    requester: User,
    audioManager: LavaplayerAudioManager,
    maxPerPage: Int,
    page: Int,
    maxPages: Int
) : PaginatedEmbed<LavaplayerAudioManager>(channel, requester, audioManager, maxPerPage, page, maxPages) {

    override fun updateMessage() {
        // Update max number of pages
        maxPages = fastCeil(obj.remaining(), maxPerPage)
        message.editMessage(this.updateEmbedBuilder().build()).queue {
            lastPage = page
        }
    }

    fun updateEmbedBuilder(): EmbedBuilder {
        val track = obj.currentTrack()
        this.embedBuilder
                .setDescription(
                        if (track != null) {
                            "${track.info.titleLink()}${if (!track.info.isStream) "$LINE_SEPARATOR${track.position.toDurationString()}/${track.info.length.toDurationString()}" else ""}".bold()
                        } else {
                            "No tracks are currently playing."
                        }
                )
                .clearFields()
                .addField("Next up", nextTracksInfo(), false)
                .addField("Page", "$page/$maxPages".bold(), false)
        return this.embedBuilder
    }

    private fun nextTracksInfo(): String {
        val tracks = obj.tracks()
        if (tracks.isEmpty()) {
            return "No tracks are left in the queue."
        }
        val min = (page - 1) * maxPerPage
        val max = min(page * maxPerPage, obj.remaining())
        return tracks.subList(min, max).mapIndexed { i, track ->
            "${min + i + 1}. ${track.info.titleLink()}"
        }.joinToString(separator = LINE_SEPARATOR, prefix = "**", postfix = "**")
    }
}
