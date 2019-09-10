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

import com.github.cf.discord.uni.entities.PickerItem
import com.github.cf.discord.uni.stateful.EventWaiter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import java.awt.Color
import java.util.concurrent.CompletableFuture

class ItemPicker(
        private val waiter: EventWaiter,
        private val user: Member,
        private val guild: Guild,
        private val confirm: Boolean = false,
        private val timeout: Long = 60000
) {
    private var index = 0
    private val embeds = mutableListOf<MessageEmbed>()
    private val items = mutableListOf<PickerItem>()

    private val rightEmote = "\u27A1"
    private val leftEmote = "\u2B05"
    private val confirmEmote = "\u2705"
    private val cancelEmote = "\u23F9"

    var color: Color = Color.CYAN

    fun addItem(item: PickerItem): ItemPicker {
        items.add(item)
        return this
    }

    fun build(msg: Message) = build(msg.channel)

    fun build(channel: MessageChannel) = if (guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION) || guild.selfMember.hasPermission(Permission.ADMINISTRATOR)) {
        buildReactions(channel)
    } else {
        buildInput(channel)
    }

    private fun buildReactions(channel: MessageChannel): CompletableFuture<PickerItem> {
        val fut = CompletableFuture<PickerItem>()

        for (item in items) {
            val embed = EmbedBuilder().apply {
                setColor(item.color ?: color)
                setFooter("${if (item.footer.isNotBlank()) "${item.footer} | " else ""}Page ${items.indexOf(item) + 1}/${items.size}", null)

                if (item.author.isNotBlank()) {
                    setAuthor(item.author, null, null)
                }

                if (item.title.isNotBlank()) {
                    setTitle(item.title, if (item.url.isNotBlank()) item.url else null)
                }

                if (item.description.isNotBlank()) {
                    descriptionBuilder.append(item.description)
                }

                if (item.thumbnail.isNotBlank()) {
                    setThumbnail(item.thumbnail)
                }

                if (item.image.isNotBlank()) {
                    setImage(item.image)
                }
            }

            embeds.add(embed.build())
        }

        channel.sendMessage(embeds[index]).queue { msg ->
            msg.addReaction(leftEmote).queue()
            if (confirm) {
                msg.addReaction(confirmEmote).queue()
            }
            msg.addReaction(cancelEmote).queue()
            msg.addReaction(rightEmote).queue()

            waiter.await<MessageReactionAddEvent>(30, timeout) {
                if (it.messageId == msg.id && it.user.id == user.user.id) {
                    when (it.reactionEmote.name) {
                        leftEmote -> {
                            it.reaction.removeReaction(it.user).queue()
                            if (index - 1 >= 0) {
                                msg.editMessage(embeds[--index]).queue()
                            }
                        }

                        rightEmote -> {
                            it.reaction.removeReaction(it.user).queue()
                            if (index + 1 <= items.size - 1) {
                                msg.editMessage(embeds[++index]).queue()
                            }
                        }

                        confirmEmote -> {
                            if (confirm) {
                                msg.delete().queue()
                                fut.complete(items[index])
                            }
                        }

                        cancelEmote -> {
                            msg.delete().queue()
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

    private fun buildInput(channel: MessageChannel): CompletableFuture<PickerItem> {
        val fut = CompletableFuture<PickerItem>()
        val formatted = items.mapIndexed { i, item -> " ${i + 1}. ${item.title}" }.joinToString("\n")

        channel.sendMessage("Please choose an item from the list by sending its number:\n```\n$formatted```").queue { msg ->
            waiter.await<MessageReceivedEvent>(1, timeout) {
                if (it.channel.id == msg.channel.id && it.author.id == user.user.id) {
                    if (it.message.contentRaw.toIntOrNull() == null) {
                        msg.channel.sendMessage("Invalid number").queue()
                    } else if (it.message.contentRaw.toInt() - 1 > items.size || it.message.contentRaw.toInt() - 1 < 0) {
                        msg.channel.sendMessage("Number out of bounds!")
                    } else {
                        index = it.message.contentRaw.toInt() - 1
                        msg.delete().queue()
                        fut.complete(items[index])
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
