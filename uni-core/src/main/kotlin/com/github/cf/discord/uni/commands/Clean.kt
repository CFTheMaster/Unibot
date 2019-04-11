package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.AsyncCommand
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.await
import kotlin.math.min

@Load
@Argument("messages", "number", true)
@Alias("clear", "cls")
class Clean : AsyncCommand() {
    override val desc = "Clean the last 10 messages sent by me"
    override val guildOnly = true
    override val cooldown = 15

    override suspend fun asyncRun(ctx: Context) {
        val msgs = ctx.channel.getHistoryAround(ctx.msg, 100).await()

        val botmsgs = msgs.retrievedHistory.filter { it.author.id == ctx.selfMember!!.user.id }
        val sub = botmsgs.subList(0, min(botmsgs.size, min(ctx.args.getOrDefault("messages", 10) as Int, 10)))
        sub.forEach { it.delete().await() }


        ctx.send("I have cleaned ${sub.size} messages from myself")
    }
}