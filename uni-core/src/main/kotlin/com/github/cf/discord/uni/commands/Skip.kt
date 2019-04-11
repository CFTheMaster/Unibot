package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.music.MusicManager
import net.dv8tion.jda.core.Permission

@Load
@Perm(Permission.MANAGE_SERVER, true)
class Skip : Command() {
    override val desc = "Skips the current song"
    override val guildOnly = true

    override fun run(ctx: Context) {
        if (!ctx.member!!.voiceState.inVoiceChannel()) {
            return ctx.send("you aren't in the voice channel")
        }

        val manager = MusicManager.musicManagers[ctx.guild!!.id]
                ?: return ctx.send("Not connected to a voice channel")

        if (manager.scheduler.queue.isEmpty()) {
            return ctx.send("There's nothing in the queue!")
        }

        if (ctx.perms["MANAGE_SERVER"] == true) {
            manager.scheduler.next()
            ctx.send("force_skip")
        } else {
            val members = manager.voiceChannel.members.filter { !it.user.isBot }

            if (members.size - 1 <= manager.voteSkip.size) {
                manager.scheduler.next()

                return ctx.send("voteskip success")
            }

            if (manager.voteSkip.contains(ctx.author.id)) {
                return ctx.send("you already voted")
            }

            if (members.size - 1 <= manager.voteSkip.size + 1) {
                manager.scheduler.next()

                return ctx.send("voteskip success")
            }

            manager.voteSkip.add(ctx.author.id)
            ctx.send("voteskip add votes: ${manager.voteSkip.size}, votes needed for skip ${members.size - 1}")

        }
    }
}