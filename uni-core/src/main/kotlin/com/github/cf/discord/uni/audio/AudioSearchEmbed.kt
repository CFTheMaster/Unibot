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

import com.github.cf.discord.uni.Reactions
import com.github.cf.discord.uni.embed.NumericOptionEmbed
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent

class AudioSearchEmbed(
        private val audioManager: LavaplayerAudioManager,
        channel: TextChannel,
        requester: User,
        tracks: List<AudioTrack>
) : NumericOptionEmbed<List<AudioTrack>>(channel, requester, tracks, tracks.size, true) {

    override fun onReactionAdd(event: GuildMessageReactionAddEvent) {
        // Only accept the original user's search
        if (event.member.user.idLong == requester.idLong) {
            // Convert reaction to choice's index value
            val choice = Reactions.EMOJI_TO_INT.getOrDefault(event.reactionEmote.name, -1)
            val track = when (choice) {
                in 1..obj.size -> obj[choice - 1]
                else -> null
            }
            // Requester chose the crossmark reaction -> remove the event listener and all reactions
            if (track == null) {
                if (event.reactionEmote.name == Reactions.REGIONAL_CROSSMARK) {
                    this.removeListener()
                }
            } else {
                // Set track's requester
                track.userData = requester

                // Send embed message to notify users that this track was added to the audio player queue
                message.channel.sendMessage(AudioEmbed.addedTrack(track, audioManager.remaining() + 1)).complete()

                // Enqueue the audio track to scheduler
                audioManager.addTrack(track)

                // Remove reaction listener from client
                this.removeListener()
            }
        }
    }
}
