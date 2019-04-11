package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager

@Load
class Autoplay : Command(){
    override val desc = "Toggles autoplay"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild?.id]
                ?: return ctx.send("Not connected to a voice channel")
        manager.autoplay = !manager.autoplay

        ctx.send("autoplay has been turned ${if (manager.autoplay) "on" else "off"}")
    }
}