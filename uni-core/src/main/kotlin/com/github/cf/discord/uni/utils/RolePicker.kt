package com.github.cf.discord.uni.utils

import com.github.cf.discord.uni.stateful.EventWaiter
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import java.util.concurrent.CompletableFuture
import kotlin.math.min

class RolePicker(
        private val waiter: EventWaiter,
        private val user: Member,
        private var roles: List<Role>,
        private val guild: Guild,
        private val timeout: Long = 60000
) {
    private var index = 0
    private val text get() = "Please select a role:\n```asciidoc\n${roles.mapIndexed {
        i, role -> if (i == index) "*${i + 1}. ${role.name} *" else " ${i + 1}. ${role.name}"
    }.joinToString("\n")}```"
    private val inputText = "Please select a role by sending its number:\n```asciidoc\n${roles.mapIndexed {
        i, role -> " ${i + 1}. ${role.name}"
    }.joinToString("\n")}```"

    private val upEmote = "\u2B06"
    private val downEmote = "\u2B07"
    private val confirmEmote = "\u2705"
    private val cancelEmote = "\u23F9"

    init { roles = roles.subList(0, min(roles.size, 5)) }

    fun build(msg: Message) = build(msg.channel)

    fun build(channel: MessageChannel) = if (guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION) || guild.selfMember.hasPermission(Permission.ADMINISTRATOR)) {
        buildReactions(channel)
    } else {
        buildInput(channel)
    }

    private fun buildReactions(channel: MessageChannel): CompletableFuture<Role> {
        val fut = CompletableFuture<Role>()

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
                            if (index + 1 <= roles.size) {
                                index++
                                msg.editMessage(text).queue()
                            }
                        }

                        cancelEmote -> {
                            msg.delete().queue()
                        }

                        confirmEmote -> {
                            msg.delete().queue()
                            fut.complete(roles[index])
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

    private fun buildInput(channel: MessageChannel): CompletableFuture<Role> {
        val fut = CompletableFuture<Role>()
        channel.sendMessage(inputText).queue { msg ->
            waiter.await<MessageReceivedEvent>(1, timeout) {
                if (it.channel.id == msg.channel.id && it.author.id == user.user.id) {
                    if (it.message.contentRaw.toIntOrNull() == null) {
                        msg.channel.sendMessage("Invalid number").queue()
                    } else if (it.message.contentRaw.toInt() - 1 > roles.size || it.message.contentRaw.toInt() - 1 < 0) {
                        msg.channel.sendMessage("Number out of bounds!").queue()
                    } else {
                        index = it.message.contentRaw.toInt() - 1
                        msg.delete().queue()
                        fut.complete(roles[index])
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