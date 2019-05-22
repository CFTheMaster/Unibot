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
package com.github.cf.discord.uni.utils

import com.github.cf.discord.uni.stateful.EventWaiter
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.util.concurrent.CompletableFuture
import kotlin.math.min

class TextChannelPicker(
        private val waiter: EventWaiter,
        private val user: Member,
        private var channels: List<TextChannel>,
        private val guild: Guild,
        private val timeout: Long = 60000
) {
    private var index = 0
    private val text get() = "Please select a channel:\n```asciidoc\n${channels.mapIndexed {
        i, channel -> if (i == index) "*${i + 1}. ${channel.name} *" else " ${i + 1}. ${channel.name}"
    }.joinToString("\n")}```"
    private val inputText = "Please select a channel by sending its number:\n```asciidoc\n${channels.mapIndexed {
        i, channel -> " ${i + 1}. ${channel.name}"
    }.joinToString("\n")}```"

    private val upEmote = "\u2B06"
    private val downEmote = "\u2B07"
    private val confirmEmote = "\u2705"
    private val cancelEmote = "\u23F9"

    init { channels = channels.subList(0, min(channels.size, 5)) }

    fun build(msg: Message) = build(msg.channel)

    fun build(channel: MessageChannel) = if (guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION) || guild.selfMember.hasPermission(Permission.ADMINISTRATOR)) {
        buildReactions(channel)
    } else {
        buildInput(channel)
    }

    private fun buildReactions(channel: MessageChannel): CompletableFuture<TextChannel> {
        val fut = CompletableFuture<TextChannel>()

        channel.sendMessage(text).queue { msg ->
            msg.addReaction(upEmote).queue()
            msg.addReaction(confirmEmote).queue()
            msg.addReaction(cancelEmote).queue()
            msg.addReaction(downEmote).queue()

            waiter.await<MessageReactionAddEvent>(20, timeout) {
                if (it.messageId == msg.id && it.user.id == user.user.id) {
                    when (it.reactionEmote.name) {
                        upEmote -> {
                            it.reaction.removeReaction(it.user).queue()
                            if (index - 1 >= 0) {
                                index--
                                msg.editMessage(text).queue()
                            }
                        }

                        downEmote -> {
                            it.reaction.removeReaction(it.user).queue()
                            if (index + 1 <= channels.size) {
                                index++
                                msg.editMessage(text).queue()
                            }
                        }

                        cancelEmote -> {
                            msg.delete().queue()
                        }

                        confirmEmote -> {
                            msg.delete().queue()
                            fut.complete(channels[index])
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }

        return fut
    }

    private fun buildInput(channel: MessageChannel): CompletableFuture<TextChannel> {
        val fut = CompletableFuture<TextChannel>()
        channel.sendMessage(inputText).queue { msg ->
            waiter.await<MessageReceivedEvent>(1, timeout) {
                if (it.channel.id == msg.channel.id && it.author.id == user.user.id) {
                    if (it.message.contentRaw.toIntOrNull() == null) {
                        msg.channel.sendMessage("Invalid number").queue()
                    } else if (it.message.contentRaw.toInt() - 1 > channels.size || it.message.contentRaw.toInt() - 1 < 0) {
                        msg.channel.sendMessage("Number out of bounds!").queue()
                    } else {
                        index = it.message.contentRaw.toInt() - 1
                        msg.delete().queue()
                        fut.complete(channels[index])
                    }
                    true
                } else {
                    false
                }
            }
        }

        return fut
    }
}
