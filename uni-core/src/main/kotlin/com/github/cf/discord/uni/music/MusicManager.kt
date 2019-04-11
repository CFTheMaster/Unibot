package com.github.cf.discord.uni.music

import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.entities.Context
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.core.entities.VoiceChannel
import java.util.*
import java.util.logging.Logger
import kotlin.reflect.jvm.jvmName


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
        val manager = GuildMusicManager(playerManager, ctx.event.textChannel, ctx.member!!.voiceState.channel as VoiceChannel)
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