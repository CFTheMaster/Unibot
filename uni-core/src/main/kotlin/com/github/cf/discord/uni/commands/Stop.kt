package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager
import net.dv8tion.jda.core.Permission

@Load
@Perm(Permission.MANAGE_SERVER)
class Stop : Command() {
    override val desc = "Stop the music!"

    override fun run(ctx: Context) {
        if (MusicManager.musicManagers[ctx.guild!!.id] == null) {
            return ctx.send("not connected to a voice channel")
        }

        MusicManager.leave(ctx.guild.id)

        ctx.send("music has stopped playing")
    }
}