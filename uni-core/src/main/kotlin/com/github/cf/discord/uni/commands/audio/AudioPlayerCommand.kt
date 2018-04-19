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

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.command.CommandContext
import com.github.kvnxiao.discord.meirei.jda.permission.PermissionLevel
import com.github.kvnxiao.discord.meirei.utility.GuildId
import com.github.cf.discord.uni.audio.AudioEmbed
import com.github.cf.discord.uni.audio.AudioEventAdapter
import com.github.cf.discord.uni.audio.LavaplayerAudioManager
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.data.authorOnly
import com.github.cf.discord.uni.database.nosql.Redis
import com.github.cf.discord.uni.stateful.GuildStateManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@CommandGroup("audio")
class AudioPlayerCommand : ListenerAdapter() {

    companion object {
        // Audio source search prefixes
        private const val PREFIX_YOUTUBE = "ytsearch:"
        private const val PREFIX_SOUNDCLOUD = "scsearch:"

        // Youtube regexes
        private const val YOUTUBE_PROTOCOL_REGEX = "(?:http://|https://|)"
        private const val YOUTUBE_VIDEO_ID_REGEX = "(?<v>[a-zA-Z0-9_-]{11})"
        private const val YOUTUBE_PLAYLIST_ID_REGEX = "(?<list>(PL|LL|FL|UU)[a-zA-Z0-9_-]+)"
        private const val YOUTUBE_MIX_ID_REGEX = "(?<mix>RD[a-zA-Z0-9_-]+)"
        private const val YOUTUBE_PLAYLIST_ID_OR_MIX_ID_REGEX = "(?:$YOUTUBE_PLAYLIST_ID_REGEX|$YOUTUBE_MIX_ID_REGEX)"
        private const val YOUTUBE_ANY_REGEX = "[\\w~.-]+=[\\w~.-]+"
        private const val YOUTUBE_PARAMETER_REGEX = "(?:v=$YOUTUBE_VIDEO_ID_REGEX|list=$YOUTUBE_PLAYLIST_ID_OR_MIX_ID_REGEX|$YOUTUBE_ANY_REGEX)&?"
        private const val YOUTUBE_PARAMETERS_REGEX = "(?:$YOUTUBE_PARAMETER_REGEX)+"
        @JvmStatic
        private val validYoutubePlaylistPatterns = arrayOf(Pattern.compile("^$YOUTUBE_PLAYLIST_ID_REGEX$"), Pattern.compile("^$YOUTUBE_PROTOCOL_REGEX(?:www\\.|m\\.|)youtube.com/playlist\\?$YOUTUBE_PARAMETERS_REGEX$"))
    }

    private val playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val guildAudioManager: GuildStateManager<LavaplayerAudioManager> = GuildStateManager()
    private val guildAudioPanelManager: GuildStateManager<AudioPlayerPanel> = GuildStateManager()

    private val nowPlayingChannelCache: Cache<GuildId, TextChannel> = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build()

    init {
        // Register niconico.jp source
        val nicoNicoEmail = EnvVars.LAVAPLAYER_NICONICO_EMAIL
        val nicoNicoPass = EnvVars.LAVAPLAYER_NICONICO_PASSWORD
        playerManager.registerSourceManager(NicoAudioSourceManager(nicoNicoEmail, nicoNicoPass))
        // Register default audio player manager for lavaplayer
        AudioSourceManagers.registerLocalSource(playerManager)
        AudioSourceManagers.registerRemoteSources(playerManager)
    }

    /**
     * Gets the text channel to output "Now Playing" messages.
     * Retrieves from cache or accesses Redis key-value store for the channel ID if the cache is invalid.
     */
    private fun Cache<GuildId, TextChannel>.getOrPut(guild: Guild): TextChannel {
        val channel = this.get(guild.idLong, {
            val bucket = Redis.client.getBucket<String?>("${guild.idLong}:setnpch")
            val id = bucket.get()

            val npChannel = if (id != null) {
                guild.getTextChannelById(id)
            } else {
                guild.getTextChannelsByName("${guild.defaultChannel.toString()}", true).firstOrNull()
                        ?: guild.textChannels.first()
            }
            npChannel
        })
        return channel!!
    }

    /**
     * Get or create a new Lavaplayer audio manager for the guild.
     * On each new audio manager instance, register an audio listener for track event
     * and set the guild's audio sending handler to the Lavaplayer audio manager's sending handler.
     */
    private fun GuildStateManager<LavaplayerAudioManager>.getOrPut(guild: Guild): LavaplayerAudioManager =
            this.getOrPut(guild.idLong, {
                // Create audio manager
                val audioManager = LavaplayerAudioManager(playerManager)

                // Add audio listener
                audioManager.player.addListener(object : AudioEventAdapter() {
                    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
                        // Send message to the designated 'now playing' text channel
                        val channel = nowPlayingChannelCache.getOrPut(guild)

                        // Create and queue embed message
                        audioManager.nowPlaying(channel, track)
                    }
                })

                // Set audio manager sending handler
                guild.audioManager.sendingHandler = audioManager.sendHandler()
                audioManager
            })

    /// @@@@@@@@@@@@@@@@@@@@@@@@@@
    /// Listener Adapter
    /// @@@@@@@@@@@@@@@@@@@@@@@@@@
    /// For creating Audio Player Panel on startup
    override fun onReady(event: ReadyEvent) {
        val guilds = event.jda.guilds
        val buckets = Redis.client.buckets.find<String>("*:setapch")
        val guildChannelMap = buckets.associate {
            it.name.substringBefore(':').toLong() to it.get().toLong()
        }
        guilds.forEach {
            if (guildChannelMap.containsKey(it.idLong)) {
                val channel = it.getTextChannelById(guildChannelMap[it.idLong]!!)
                val messages = channel.history.retrievePast(100).complete()
                messages.forEach {
                    it.delete().reason("Deleting messages in channel for Audio Player Panel").complete()
                }
                // Create a new audio player panel
                val audioManager = guildAudioManager.getOrPut(it)
                val audioPanel = AudioPlayerPanel(channel, audioManager)
                guildAudioPanelManager.put(it.idLong, audioPanel.start())
            }
        }
    }

    /// @@@@@@@@@@@@@@@@@@@@@@@@@@,
    /// Command declarations below
    /// @@@@@@@@@@@@@@@@@@@@@@@@@@

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "tracks.play",
            aliases = ["play"],
            description = "Plays audio in the current voice channel the bot is in."
    )
    fun play(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild && event.guild.selfMember.voiceState.inVoiceChannel()) {
                val args = context.args ?: return
                val audioManager = guildAudioManager.getOrPut(event.guild)
                audioManager.loadAndPlay(event.author, event.textChannel, args, false)
            } else{
                event.channel.sendMessage("i'm not in a voice channel >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "source.soundcloud",
            aliases = ["sc"],
            description = "Searches SoundCloud and plays audio from the first result in the current voice channel the bot is in.",
            usage = "<soundcloud search query>"
    )
    fun soundcloud(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild && event.guild.selfMember.voiceState.inVoiceChannel()) {
                val args = context.args ?: return
                val audioManager = guildAudioManager.getOrPut(event.guild)
                if (args.startsWith("https://www.soundcloud.com")) {
                    audioManager.loadAndPlay(event.author, event.textChannel, args, true)
                } else {
                    audioManager.loadAndPlay(event.author, event.textChannel, "$PREFIX_SOUNDCLOUD$args", true)
                }
            }else{
                event.channel.sendMessage("i'm not in a voice channel >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "source.soundcloud_search",
            aliases = ["scs"],
            description = "Queries SoundCloud for search results and uses reactions to select the song to play.",
            usage = "<soundcloud search query>"
    )
    fun soundcloudSearch(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild && event.guild.selfMember.voiceState.inVoiceChannel()) {
                val args = context.args ?: return
                val audioManager = guildAudioManager.getOrPut(event.guild)
                audioManager.searchAndPlay(event.author, event.textChannel, "$PREFIX_SOUNDCLOUD$args")
            }else{
                event.channel.sendMessage("i'm not in a voice channel >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "source.youtube",
            aliases = ["yt"],
            description = "Searches YouTube and plays audio from the first result in the current voice channel the bot is in.",
            usage = "<youtube search query>"
    )
    fun youtube(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild && event.guild.selfMember.voiceState.inVoiceChannel()) {
                val args = context.args ?: return
                val audioManager = guildAudioManager.getOrPut(event.guild)
                if (args.startsWith("https://")) {
                    // Playlist
                    if (validYoutubePlaylistPatterns.any { it.matcher(args).matches() }) {
                        audioManager.loadAndPlay(event.author, event.textChannel, args, false)
                    } else {
                        // Single track
                        audioManager.loadAndPlay(event.author, event.textChannel, args, true)
                    }
                } else {
                    // Single track search
                    audioManager.loadAndPlay(event.author, event.textChannel, "$PREFIX_YOUTUBE$args", true)
                }
            }else{
                event.channel.sendMessage("i'm not in a voice channel >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "source.youtube_query",
            aliases = ["yts"],
            description = "Queries YouTube for search results and uses reactions to select the song to play.",
            usage = "<url to valid source>"
    )
    fun youtubeSearch(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild && event.guild.selfMember.voiceState.inVoiceChannel()) {
                val args = context.args ?: return
                val audioManager = guildAudioManager.getOrPut(event.guild)
                audioManager.searchAndPlay(event.author, event.textChannel, "$PREFIX_YOUTUBE$args")
            }else{
                event.channel.sendMessage("i'm not in a voice channel >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "tracks.skip",
            aliases = ["skip", "next"],
            description = "Skips the current playing track to the next one."
    )
    fun skip(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if (author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild) {
                val audioManager = guildAudioManager.getOrPut(event.guild)
                audioManager.skip(event.author, event.textChannel)
            }else{
                event.channel.sendMessage("this is not a guild >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "tracks.stop",
            aliases = ["stop"],
            description = "Stops the current playing track."
    )
    fun stop(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild && event.guild.selfMember.voiceState.inVoiceChannel()) {
                val audioManager = guildAudioManager.getOrPut(event.guild)
                audioManager.stop(event.author, event.textChannel)
            }else{
                event.channel.sendMessage("i'm not in a voice channel >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "channel.set_now_playing",
            aliases = ["setnpch"],
            description = "Sets the channel for the bot to print the now playing information"
    )
    fun setNowPlaying(context: CommandContext, event: MessageReceivedEvent) {
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val embed = EmbedBuilder()
                .setColor(embedColor)
                .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setTitle("music")
                .setDescription("i have set the current playing channel to <#${event.message.channel.id}>")
                .build()
        val author = event.author
        if(author!!.isBot) {
            return
        } else if(event.member.hasPermission(Permission.ADMINISTRATOR) || event.message.author.id in authorOnly.authors){
            if (event.channelType.isGuild) {
                event.channel.sendMessage(embed).queue()
                val channelId = event.textChannel.idLong

                // Set channel ID in Redis
                Redis.client.getBucket<String>("${event.guild.idLong}:setnpch").set(channelId.toString())
                // Invalidate cache
                nowPlayingChannelCache.invalidate(event.guild.idLong)
            }
        }else{
            event.channel.sendMessage(
                    EmbedBuilder()
                            .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                            .setTitle("not sufficient perms to do this command")
                            .setDescription("do you have the permission Admin <:OhISee:397902772865073154>")
                            .build()
            ).queue()
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "channel.set_audio_panel",
            aliases = ["setapch"],
            description = "Sets the channel for the bot to print and updateEmbedBuilder the audio player panel"
    )
    fun setAudioPlayerPanel(context: CommandContext, event: MessageReceivedEvent) {
        val randomColor = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor1 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val randomColor2 = (Math.floor(Math.random() * (255)) + 1).toInt()
        val embedColor = Color(randomColor, randomColor1, randomColor2)

        val embed = EmbedBuilder()
                .setColor(embedColor)
                .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setTitle("music")
                .setDescription("i have set the current update embed for the audio player panel set to channel ${event.message.channel.name.toString()}")
                .build()
        val author = event.author
        if(author!!.isBot) return
        else if(event.member.hasPermission(Permission.ADMINISTRATOR) || event.message.author.id in authorOnly.authors) {
            // Set channel only if audio panel doesn't exit
            if (event.channelType.isGuild && !guildAudioPanelManager.contains(event.guild.idLong)) {
                // Set channel ID in Redis
                val channelId = event.textChannel.idLong
                try {
                    event.channel.sendMessage(embed).queue()
                    // Create the SessionFactory from hibernate.cfg.xml
                    Redis.client.getBucket<String>("${event.guild.idLong}:setapch").set(channelId.toString())
                } catch (ex: Throwable) {
                    System.err.println("Initial SessionFactory creation failed. $ex")
                    throw ExceptionInInitializerError(ex)
                }

                // Create a new audio player panel
                val audioManager = guildAudioManager.getOrPut(event.guild)
                val audioPanel = AudioPlayerPanel(event.textChannel, audioManager)
                guildAudioPanelManager.put(event.guild.idLong, audioPanel.start())
            }
        }
        else{
            event.channel.sendMessage(
                    EmbedBuilder()
                            .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                            .setTitle("not sufficient perms to do this command")
                            .setDescription("do you have the permission Admin<:OhISee:397902772865073154>")
                            .build()
            ).queue()
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "channel.delete_audio_panel",
            aliases = ["delapch"],
            description = "Deletes the audio player panel"
    )
    fun deleteAudioPlayerPanel(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if (author!!.isBot) {
            return
        }  else if(event.member.hasPermission(Permission.ADMINISTRATOR) || event.message.author.id in authorOnly.authors) {
        // Delete channel only if audio panel exists
            if (event.channelType.isGuild && guildAudioPanelManager.contains(event.guild.idLong)) {
                // Delete channel ID in Redis
                Redis.client.getBucket<String>("${event.guild.idLong}:setapch").delete()
                val audioPlayer = guildAudioPanelManager.delete(event.guild.idLong)!!
                // Dispose the periodic update scheduler
                audioPlayer.dispose()
            }
        } else{
            event.channel.sendMessage(
                    EmbedBuilder()
                            .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                            .setTitle("not sufficient perms to do this command")
                            .setDescription("do you have the permission Admin<:OhISee:397902772865073154>")
                            .build()
            ).queue()
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "tracks.shuffle",
            aliases = ["shuffle"],
            description = "Shuffles all queued tracks in the audio player."
    )
    fun shuffle(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild && event.guild.selfMember.voiceState.inVoiceChannel()) {
                val audioManager = guildAudioManager.getOrPut(event.guild)
                // Shuffle tracks in current guild
                audioManager.shuffle(event.author, event.textChannel)
            }else{
                event.channel.sendMessage("i'm not in a voice channel >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "tracks.playing",
            aliases = ["playing", "np"],
            description = "Shows the current playing track and the rest of the queue."
    )
    fun nowPlaying(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            if (event.channelType.isGuild) {
                val audioManager = guildAudioManager.getOrPut(event.guild)
                val page = context.args?.toIntOrNull() ?: 1
                AudioEmbed.nowPlayingPaginatedEmbed(audioManager, event.textChannel, event.author, page).queue()
            }else{
                event.channel.sendMessage("this is a not a guild >~<").queue()
            }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "tracks.clear",
            aliases = ["clear"],
            description = "Clears the audio queue for this guild."
    )
    fun clear(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            event.message.channel.sendMessage("cleared the current queue!")
            if (event.channelType.isGuild) {
                val audioManager = guildAudioManager.getOrPut(event.guild)
                audioManager.clear(event.author, event.textChannel)
            }else{
                event.channel.sendMessage("this is not a guild >~<").queue()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioPlayerCommand) return false

        if (playerManager != other.playerManager) return false

        return true
    }

    override fun hashCode(): Int {
        return playerManager.hashCode()
    }
}
