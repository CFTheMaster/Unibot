package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager
import net.dv8tion.jda.core.Permission

@Load
@Perm(Permission.MANAGE_SERVER)
@Alias("vol")
@Argument("volume", "number", true)
class Volume : Command(){
    override val desc = "Change the volume of the music"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val manager = MusicManager.musicManagers[ctx.guild!!.id]
                ?: return ctx.send("not connected to the channel")
        if ("volume" in ctx.args) {
            val vol = ctx.args["volume"] as Int

            if (vol > 100) {
                return ctx.send("Volume can't be put above 100")
            }

            if (vol < 0) {
                return ctx.send("can't put the volume below 0")
            }

            manager.player.volume = vol
        }

        ctx.send("volume has been changed to ${manager.player.volume / 10}")
    }
}