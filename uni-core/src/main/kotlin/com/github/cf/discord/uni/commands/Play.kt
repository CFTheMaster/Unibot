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
package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.PickerItem
import com.github.cf.discord.uni.listeners.EventListener
import com.github.cf.discord.uni.music.GuildMusicManager
import com.github.cf.discord.uni.music.MusicManager
import com.github.cf.discord.uni.utils.Http
import com.github.cf.discord.uni.utils.ItemPicker
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.audio.hooks.ConnectionListener
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import okhttp3.HttpUrl
import org.json.JSONObject
import java.awt.Color

@Load
@Argument("url|query", "string")
class Play : Command(){
    override val desc = "Plays audio in the current voice channel the bot is in."
    override val guildOnly = true

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState!!.inVoiceChannel()) {
            return ctx.send("failed to join the voice channel")
        }

        if (MusicManager.musicManagers[ctx.guild!!.id] == null) {
            val manager = MusicManager.join(ctx)

            ctx.guild.audioManager.connectionListener = object : ConnectionListener {
                override fun onStatusChange(status: ConnectionStatus) {
                    if (status == ConnectionStatus.CONNECTED)
                        play(ctx, manager)
                }

                override fun onUserSpeaking(user: User, speaking: Boolean) { return }
                override fun onPing(ping: Long) { return }
            }
        } else {
            play(ctx, MusicManager.musicManagers[ctx.guild.id]!!)
        }
    }
    fun play(ctx: Context, manager: GuildMusicManager) {
        val search = ctx.rawArgs.joinToString(" ")

        MusicManager.playerManager.loadItemOrdered(manager, search, object : AudioLoadResultHandler {
            override fun loadFailed(exception: FriendlyException) = ctx.sendError(exception)

            override fun noMatches() {
                val picker = ItemPicker(EventListener.waiter, ctx.member as Member, ctx.guild as Guild, true)

                Http.get(HttpUrl.Builder().apply {
                    scheme("https")
                    host("www.googleapis.com")
                    addPathSegment("youtube")
                    addPathSegment("v3")
                    addPathSegment("search")
                    addQueryParameter("key", EnvVars.GOOGLE_API_KEY)
                    addQueryParameter("part", "snippet")
                    addQueryParameter("maxResults", "10")
                    addQueryParameter("type", "video")
                    addQueryParameter("q", search)
                }.build()).thenAccept { res ->
                    val items = JSONObject(res.body()!!.string()).getJSONArray("items")

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)

                        val id = item
                                .getJSONObject("id")
                                .getString("videoId")

                        val snippet = item.getJSONObject("snippet")

                        val title = snippet.getString("title")
                        val thumb = snippet
                                .getJSONObject("thumbnails")
                                .getJSONObject("medium")
                                .getString("url")

                        val channel = snippet.getString("channelTitle")

                        picker.addItem(PickerItem(id, title, "", channel, thumb, url = "https://youtu.be/$id"))
                    }

                    picker.color = Color(255, 0, 0)

                    val item = picker.build(ctx.channel).get()
                    res.close()

                    MusicManager.playerManager.loadItemOrdered(manager, item.url, object : AudioLoadResultHandler {
                        override fun loadFailed(exception: FriendlyException) = ctx.sendError(exception)

                        override fun noMatches() = ctx.send("no matching songs have been found")

                        override fun trackLoaded(track: AudioTrack) {
                            manager.scheduler.add(track)

                            ctx.send("${track.info.title} has been loaded")
                        }

                        override fun playlistLoaded(playlist: AudioPlaylist) = trackLoaded(playlist.tracks.first())
                    })
                }
            }

            override fun trackLoaded(track: AudioTrack) {
                manager.scheduler.add(track)

                ctx.send("${track.info.title} has been added to the queue"

                )
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val id = when {
                    search.indexOf("youtu") > -1 -> search.split("v=")[1].split("&")[0]

                    else -> ""
                }

                val tracks = if (id.isNotBlank()) {
                    val index = playlist.tracks.indexOfFirst { it.identifier == id }

                    playlist.tracks.subList(index, playlist.tracks.size)
                } else {
                    playlist.tracks
                }

                for (track in tracks) {
                    manager.scheduler.add(track)
                }

                ctx.send("added to queue playlist tracks: ${tracks.size} to playlist: ${playlist.name}")
            }
        })
    }

}
