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
