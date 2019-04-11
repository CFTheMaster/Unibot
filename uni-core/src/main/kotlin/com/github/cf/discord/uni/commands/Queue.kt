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
                setColor(6684876)
                setTitle("queue")
                descriptionBuilder.append(formatted)
            }

            ctx.send(embed.build())
        }
    }
}