package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.entities.AsyncCommand
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.await
import net.dv8tion.jda.core.Permission
import kotlin.math.min

@Load
@Argument("messages", "number")
@Flags(
        Flag("bots", 'b', "Only clean messages sent by a bot")
)
@Perm(Permission.MESSAGE_MANAGE)
class Prune : AsyncCommand() {
    override val desc = "Prune messages."

    override suspend fun asyncRun(ctx: Context) {
        val history = ctx.channel.iterableHistory.await()
        val toClean = min(50, ctx.args["messages"] as Int)
        var messages = 0

        (0 until toClean)
                .map { history[it]}
                .filterNot{
                    (ctx.flags.argMap.containsKey("bots") || ctx.flags.argMap.containsKey("b")) && !it.author.isBot
                }
                .forEach {
                    it.delete().await()
                    messages++
                }
        ctx.send("$messages: messages have been pruned")
    }
}