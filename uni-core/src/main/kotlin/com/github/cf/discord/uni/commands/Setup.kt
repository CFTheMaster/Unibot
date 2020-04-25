/*
 *   Copyright (C) 2017-2021 computerfreaker
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

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.Uni.Companion.prefixes
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.annotations.Perm
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.database.schema.Guilds
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.extensions.searchTextChannels
import com.github.cf.discord.uni.listeners.EventListener
import com.github.cf.discord.uni.utils.TextChannelPicker
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.jetbrains.exposed.sql.update
import java.util.concurrent.CompletableFuture

@Load
@Perm(Permission.MANAGE_SERVER)
@Argument("topic", "string", true)
class Setup : Command(){
    override val desc = "Easy setup of Uni."
    override val guildOnly = true
    override val cate = Category.MODERATION.name

    private val topics = listOf(
            "starboard",
            "modlogs",
            "logs"
    )

    override fun run(ctx: Context) {
        val topic = ctx.args.getOrDefault("topic", "choose") as String

        if (topic == "choose") {
            ctx.channel.sendMessage("What would you like to set up? (`cancel` to cancel, `list` for a list of topics)").queue {
                EventListener.waiter.await<MessageReceivedEvent>(1, 60000L) { event ->
                    if (event.author.id == ctx.author.id && event.channel.id == ctx.channel.id) {
                        if (event.message.contentRaw == "list") {
                            ctx.sendCode("asciidoc", topics.joinToString("\n"))
                            return@await true
                        }

                        if (event.message.contentRaw != "cancel") {
                            setupTopic(ctx, event.message.contentRaw)
                        }

                        true
                    } else {
                        false
                    }
                }
            }
        } else {
            setupTopic(ctx, topic)
        }
    }

    private fun setupTopic(ctx: Context, topic: String) {
        when (topic) {
            "starboard" -> setupStarboard(ctx)
            "modlogs" -> setupModlogs(ctx)
            "modlog" -> setupModlogs(ctx)
            "logs" -> setupLogs(ctx)
            else -> ctx.send("Setup topic not found!")
        }
    }

    private fun askChannel(ctx: Context): CompletableFuture<TextChannel> {
        val fut = CompletableFuture<TextChannel>()

        EventListener.waiter.await<MessageReceivedEvent>(1, 60000L) { event ->
            if (event.author.id == ctx.author.id && event.channel.id == ctx.channel.id) {
                if (event.message.contentRaw == "cancel") {
                    return@await true
                }

                val channels = ctx.guild!!.searchTextChannels(event.message.contentRaw)

                if (channels.isEmpty()) {
                    ctx.channel.sendMessage("Couldn't find that channel, mind trying again?").queue {
                        askChannel(ctx).thenAccept { fut.complete(it) }
                    }
                    return@await true
                }

                if (channels.size == 1) {
                    fut.complete(channels[0])
                    return@await true
                }

                val picker = TextChannelPicker(EventListener.waiter, ctx.member!!, channels, ctx.guild)

                picker.build(ctx.msg).thenAccept { fut.complete(it) }

                true
            } else {
                false
            }
        }

        return fut
    }

    private fun setupStarboard(ctx: Context) {
        ctx.channel.sendMessage("What channel would you like to have the starboard in? (`cancel` to cancel)").queue {
            askChannel(ctx).thenAccept { channel ->
                asyncTransaction(Uni.pool) {
                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[starboard] = true
                        it[starboardChannel] = channel.idLong
                    }
                }.execute().thenAccept {
                    ctx.send("Successfully set up starboard! (You can disable starboard using `${prefixes.firstOrNull()}config disable starboard`)")
                }.thenApply {}.exceptionally {
                    ctx.sendError(it)
                    LOGGER.error("Error while trying to set up starboard", it)
                }
            }
        }
    }

    private fun setupModlogs(ctx: Context) {
        ctx.channel.sendMessage("What channel would you like to have the modlog in? (`cancel` to cancel)").queue {
            askChannel(ctx).thenAccept { channel ->
                asyncTransaction(Uni.pool) {
                    Guilds.update({
                        Guilds.id.eq(ctx.guild!!.idLong)
                    }) {
                        it[modlogs] = true
                        it[modLogChannel] = channel.idLong
                    }
                }.execute().thenAccept {
                    ctx.send("Successfully set up modlogs! (You can disable modlogs using `${prefixes.firstOrNull()}config disable modlogs`)")
                }.thenApply {}.exceptionally {
                    ctx.sendError(it)
                    LOGGER.error("Error while trying to set up modlogs", it)
                }
            }
        }
    }

    private fun setupLogs(ctx: Context) {
        ctx.channel.sendMessage("Are you sure you want to enable message logs for **all** channels? [y/N]").queue {
            EventListener.waiter.await<MessageReceivedEvent>(1, 60000L) { event ->
                if (event.author.id == ctx.author.id && event.channel.id == ctx.channel.id) {
                    if (event.message.contentRaw.toLowerCase().startsWith("y")) {
                        asyncTransaction(Uni.pool) {
                            Guilds.update({
                                Guilds.id.eq(ctx.guild!!.idLong)
                            }) {
                                it[logs] = true
                            }
                        }.execute().thenAccept {
                            ctx.send("Successfully set up message logs! (You can disable logs using `${prefixes.firstOrNull()}config disable logs`)")
                        }.thenApply {}.exceptionally {
                            ctx.sendError(it)
                            LOGGER.error("Error while trying to set up logs", it)
                        }
                    } else {
                        ctx.send("Logs will **not** be enabled.")
                    }

                    true
                } else {
                    false
                }
            }
        }
    }
}
