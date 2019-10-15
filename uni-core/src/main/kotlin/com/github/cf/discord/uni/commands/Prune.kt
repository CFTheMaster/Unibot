/*
 *   Copyright (C) 2017-2019 computerfreaker
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.entities.AsyncCommand
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.PermissionException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.math.min

@Load
@Argument("messages", "number")
@Flags(
        Flag("bots", 'b', "Only clean messages sent by a bot")
)
@Perm(Permission.MESSAGE_MANAGE)
class Prune : AsyncCommand() {
    override val desc = "Prune messages."
    override val cate = Category.MODERATION.name

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
        ctx.channel.sendMessage("$messages: messages have been pruned").queue({
            Timer().schedule(
                    timerTask {
                        it.delete().queue()
                    },TimeUnit.SECONDS.toMillis(10))


        }) {
            err ->
            if (err is PermissionException) {
                ctx.send("permissions missing can't ban the user")
            } else {
                ctx.sendError(err)
            }
        }
    }
}
