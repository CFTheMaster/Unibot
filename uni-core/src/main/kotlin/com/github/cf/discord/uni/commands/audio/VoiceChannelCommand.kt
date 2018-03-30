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

import com.github.cf.discord.uni.core.EnvVars
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.managers.AudioManager
import java.awt.Color

@CommandGroup("voicechannel")
class VoiceChannelCommand {

    /**
     * Joins or moves to a new voice channel
     */
    @Synchronized
    private fun connectToVoiceChannel(audioManager: AudioManager, voiceChannel: VoiceChannel) {
        if (!audioManager.isAttemptingToConnect) {
            audioManager.openAudioConnection(voiceChannel)
        }
    }

    @Synchronized
    private fun leaveVoiceChannel(audioManager: AudioManager) = audioManager.closeAudioConnection()

    /// @@@@@@@@@@@@@@@@@@@@@@@@@@
    /// Command declarations below
    /// @@@@@@@@@@@@@@@@@@@@@@@@@@

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "join",
            aliases = ["join"],
            description = "Makes the bot join the current voice channel you are in"
    )
    fun join(context: CommandContext, event: MessageReceivedEvent) {
        val randomColor = (Math.floor(Math.random()*(255))+1).toInt()
        val randomColor1 = (Math.floor(Math.random()*(255))+1).toInt()
        val randomColor2 = (Math.floor(Math.random()*(255))+1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)
        val embed = EmbedBuilder()
                .setColor(embedColor)
                .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setTitle("joined the voice channel")
                .setDescription("don't forget to do ${EnvVars.PREFIX}setapch to set the music channel \uD83D\uDC9C")
                .build()
        if (event.channelType.isGuild && event.member.voiceState.inVoiceChannel()) {
            connectToVoiceChannel(event.guild.audioManager, event.member.voiceState.channel)
            event.message.channel.sendMessage(embed).queue()
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "leave",
            aliases = ["leave"],
            description = "Makes the bot leave the current voice channel it is in."
    )
    fun leave(context: CommandContext, event: MessageReceivedEvent) {
        val randomColor = (Math.floor(Math.random()*(255))+1).toInt()
        val randomColor1 = (Math.floor(Math.random()*(255))+1).toInt()
        val randomColor2 = (Math.floor(Math.random()*(255))+1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val embed = EmbedBuilder()
                .setColor(embedColor)
                .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setTitle("left the current voice channel")
                .setDescription("i left the current voice channel <:TheSaddest:370615171464429569>")
                .build()
        if (event.channelType.isGuild) {
            leaveVoiceChannel(event.guild.audioManager)
            event.message.channel.sendMessage(embed).queue()
        }
    }
}
