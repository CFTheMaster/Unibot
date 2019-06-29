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

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.entities.PickerItem
import com.github.cf.discord.uni.listeners.EventListener
import com.github.cf.discord.uni.music.MusicManager
import com.github.cf.discord.uni.utils.ItemPicker
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission

@Perm(Permission.MANAGE_SERVER)
class Clear : Command() {
    override val desc = "Clear the queue!"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id] ?: return ctx.send("Not connected!")

        manager.scheduler.queue.clear()
        ctx.send("queue has been cleared successfully")
    }
}

@Load
class Queue : Command(){
    override val desc = "View the queue!"
    override val guildOnly = true

    init {
        addSubcommand(Clear())
    }

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild!!.id] ?: return ctx.send("Not connecteed")

        val formatted = manager.scheduler.queue.mapIndexed { i: Int, audioTrack: AudioTrack ->
            "${i + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})"
        }.joinToString("\n")

        if(formatted.length > 2048) {
            val parts = mutableListOf<String>()
            val picker = ItemPicker(EventListener.waiter, ctx.member!!, ctx.guild)
            var part = ""

            val items = manager.scheduler.queue.mapIndexed{
                i: Int, audioTrack: AudioTrack -> "${i + 1}. [${audioTrack.info.title}](${audioTrack.info.uri})"
            }
            for (item in items) {
                if (part.split("\n").size >= 10) {
                    parts += part
                    part = ""
                }

                part += "$item\n"
            }

            if (part.isNotBlank() && part.split("\n").size <= 10)
                parts += part

            for (pt in parts) {
                picker.addItem(
                        PickerItem(
                                "",
                                "queue",
                                pt
                        )
                )
            }

            picker.build(ctx.channel)
        } else {
            val embed = EmbedBuilder().apply {
                setColor(ctx.member?.colorRaw ?: 6684876)
                setTitle("queue")
                descriptionBuilder.append(formatted)
            }

            ctx.send(embed.build())
        }
    }
}
