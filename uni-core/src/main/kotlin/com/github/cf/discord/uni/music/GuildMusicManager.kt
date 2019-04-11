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