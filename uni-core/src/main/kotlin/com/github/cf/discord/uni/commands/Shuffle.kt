package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager
import net.dv8tion.jda.core.Permission

@Load
@Perm(Permission.MANAGE_SERVER)
class Shuffle : Command(){
    override val desc = "Shuffles the queue!"

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild!!.id]
                ?: return ctx.send("not connected")

        manager.scheduler.shuffle()

        ctx.send("queue shuffled")
    }
}