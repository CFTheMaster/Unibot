package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import java.time.Duration

@Load
class Ping : Command(){
    override val desc = "check the current ping of the bot"
    override val guildOnly = false

    override fun run(ctx: Context) {
        val receivedTime = ctx.msg.creationTime.toInstant()
        ctx.channel.sendMessage("Pong!").queue {
            val sentTime = it.creationTime.toInstant().plusMillis(ctx.jda.ping)
            it.editMessage("${it.contentRaw} ${Duration.between(receivedTime, sentTime).toMillis()}ms").queue()
        }
    }
}