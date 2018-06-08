/*
 *   Copyright (C) 2017-2018 computerfreaker
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
package com.github.cf.discord.uni.listeners

import com.github.cf.discord.uni.Handler.CommandHandler
import com.github.cf.discord.uni.Uni
import io.sentry.Sentry
import com.github.cf.discord.uni.async.asyncTransaction
import com.github.cf.discord.uni.db.Database
import com.github.cf.discord.uni.db.schema.*
import com.github.cf.discord.uni.entities.EventWaiter
import com.github.cf.discord.uni.extensions.UTF8Control
import com.github.cf.discord.uni.utils.I18n
import gnu.trove.map.hash.TLongLongHashMap
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.*
import net.dv8tion.jda.core.events.message.guild.*
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.utils.PermissionUtil
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.Event
import org.jetbrains.exposed.sql.*
import java.awt.Color
import java.util.*

class CommandListener : ListenerAdapter() {

    companion object {
        val snipes = TLongLongHashMap()
        val cmdHandler = CommandHandler()
        val waiter = EventWaiter()
    }
    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val channel = event.channel
        val guild = event.guild
        val author = event.author
        if(!PermissionUtil.checkPermission(channel, guild?.selfMember, Permission.MESSAGE_WRITE)
                || !PermissionUtil.checkPermission(channel, guild?.selfMember, Permission.MESSAGE_EMBED_LINKS)
                || author!!.isBot) {
            return
        }
    }
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.guild != null) {
            Database.getGuildSafe(event.guild).thenAccept { stored ->
                if (stored.logs) {
                    Uni.LOGGER.debug { "AAAAAAAAAAAAAAAAAABBBBBBBBB" }
                }

                if (event.author.isBot) {
                    return@thenAccept
                }

                Database.getUserSafe(event.member).thenAccept { user ->
                    try {
                        cmdHandler.handleMessage(event, user, stored)
                    } catch (e: Exception) {
                        Uni.LOGGER.error("Error while trying to handle message", e)
                        Sentry.capture(e)
                    }

                    val locale = Locale(user.lang.split("_")[0], user.lang.split("_")[1])
                    val bundle = ResourceBundle.getBundle("i18n.Kyubey", locale, UTF8Control())

                    if (stored.antiInvite) {
                        val regex = "(https?)?:?(//)?discord(app)?.?(gg|io|me|com)?/(\\w+:?\\w*@)?(\\S+)(:[0-9]+)?(/|/([\\w#!:.?+=&%@!-/]))?".toRegex()

                        if (event.member.roles.isEmpty() && regex.containsMatchIn(event.message.contentRaw)) {
                            event.message.delete().queue({
                                event.channel.sendMessage(
                                        I18n.parse(
                                                bundle.getString("no_ads"),
                                                mapOf("user" to event.author.asMention)
                                        )
                                ).queue()
                            }) {
                                event.channel.sendMessage(
                                        I18n.parse(
                                                bundle.getString("error"),
                                                mapOf("error" to it)
                                        )
                                ).queue()
                                Uni.LOGGER.error("Error while trying to delete ad", it)
                                Sentry.capture(it)
                            }
                        }
                    }

                    asyncTransaction(Uni.pool) {
                        val contract = Contracts.select { Contracts.userId.eq(event.author.idLong) }.firstOrNull() ?: return@asyncTransaction
                        val curLevel = contract[Contracts.level]
                        val xp = contract[Contracts.experience]

                        val xpNeeded = curLevel.toFloat() * 500f * (curLevel.toFloat() / 3f)

                        if (xp >= xpNeeded) {
                            Contracts.update({
                                Contracts.userId.eq(event.author.idLong)
                            }) {
                                it[level] = curLevel + 1
                                it[experience] = 0
                                it[balance] = contract[Contracts.balance] + 2500
                            }

                            if (stored.levelMessages) {
                                event.channel.sendMessage(EmbedBuilder().apply {
                                    setTitle("${event.author.name}, you are now rank ${curLevel + 1}!") // TODO translation
                                    setColor(Color.CYAN)
                                    descriptionBuilder.append("+2500$\n")

                                    // TODO add random items on levelup
                                }.build()).queue()
                            }
                        }
                    }.execute().exceptionally {
                        Uni.LOGGER.error("Error while trying to levelup user ${event.author.name}#${event.author.discriminator} (${event.author.id}", it)
                        Sentry.capture(it)
                    }
                }
            }
        } else {
            if (event.author.isBot) {
                return
            }

            Database.getUserSafe(event.author).thenAccept { user ->
                try {
                    cmdHandler.handleMessage(event, user)
                } catch (e: Exception) {
                    Uni.LOGGER.error("Error while trying to handle message", e)
                    Sentry.capture(e)
                }
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.guild != null) {
            Database.getGuildSafe(event.guild).thenAccept { guild ->
                if (guild.logs) {
                    Database.logEvent(event)
                }
            }
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (event.guild != null) {
            Database.getGuildSafe(event.guild).thenAccept { guild ->
                if (guild.logs) {
                    Uni.LOGGER.debug { "UPDATE" }
                }
            }
        }
    }
}
