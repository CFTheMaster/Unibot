package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager

@Alias("unpause", "resume")
class Pause : Command() {
    override val desc = "Pause the current song!"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id]
                ?: return ctx.send("not connected to a voice channel"
                )
        val state = manager.player.isPaused

        manager.player.isPaused = !state

        if (!state) {
            ctx.send("paused")
        } else {
            ctx.send("resumed")
        }
    }
}