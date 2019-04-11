package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.ReturnCodes
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

@Load
class Restart : Command(){
    override val desc = "restart the bot"
    override val ownerOnly = true

    override fun run(ctx: Context) {
        ctx.channel.sendMessage("**Shutting down and restarting after 3 seconds.**").queue({
            val timer = Timer()
            timer.schedule(timerTask { exitProcess(ReturnCodes.RESTART) }, 3000)
        })
    }
}