package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.time.Duration
import java.time.Instant

@Load
class Uptime : Command(){
    override val desc = "check the uptime of the bot"
    override val guildOnly = false

    private val startTime = Instant.now()

    override fun run(ctx: Context) {
        ctx.send("System uptime: ${Duration.between(startTime, Instant.now()).formatDuration()}")
    }

    private fun Duration.formatDuration(): String {
        val seconds = Math.abs(this.seconds)
        return "**${seconds / 86400}**d **${(seconds % 86400) / 3600}**h **${(seconds % 3600) / 60}**min **${seconds % 60}**s"
    }
}