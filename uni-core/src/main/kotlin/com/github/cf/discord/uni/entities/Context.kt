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
package com.github.cf.discord.uni.entities

import com.github.cf.discord.uni.Handler.ArgParser
import com.github.cf.discord.uni.database.DBGuild
import com.github.cf.discord.uni.database.DBUser
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.InputStream
import java.util.*
import java.util.concurrent.CompletableFuture

class Context(
        val event: MessageReceivedEvent,
        val cmd: Command,
        val args: MutableMap<String, Any>,
        val rawArgs: List<String>,
        val flags: ArgParser.ParsedResult,
        val perms: MutableMap<String, Boolean>,
        val storedUser: DBUser,
        val storedGuild: DBGuild?
) {

    val jda: JDA = event.jda
    val guild: Guild? = event.guild
    val author: User = event.author
    val channel: MessageChannel = event.channel
    val msg: Message = event.message
    val member: Member? = event.member
    val selfMember: Member? = event.guild?.selfMember

    fun send(arg: String) = event.channel.sendMessage(arg).queue()
    fun send(arg: MessageEmbed) = event.channel.sendMessage(arg).queue()

    fun sendCode(lang: String, arg: Any) = event.channel.sendMessage("```$lang\n$arg```").queue()

    fun sendError(e: Throwable) = event.channel.sendMessage(e.toString()).queue()

    fun getLastImage(): CompletableFuture<InputStream?> {
        val fut = CompletableFuture<InputStream?>()

        channel.history.retrievePast(25).queue({
            val history = it.filter { it.attachments.isNotEmpty() && it.attachments[0].isImage }

            if (history.isEmpty()) {
                fut.complete(null)
            } else {
                fut.complete(history[0].attachments[0].retrieveInputStream().get())
            }
        }) {
            fut.complete(null)
        }

        return fut
    }
}
