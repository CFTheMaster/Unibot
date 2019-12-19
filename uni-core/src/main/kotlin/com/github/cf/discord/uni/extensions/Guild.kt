/*
 *   Copyright (C) 2017-2020 computerfreaker
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
package com.github.cf.discord.uni.extensions

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.database.DatabaseWrapper
import com.github.cf.discord.uni.database.schema.Starboard
import com.github.cf.discord.uni.database.schema.Starboard.stargazers
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.awt.Color

fun Guild.searchMembers(query: String): List<Member> = members.filter {
    "<@!${it.user.idLong}> ${it.asMention} ${it.effectiveName.toLowerCase()} ${it.user.name.toLowerCase()}#${it.user.discriminator} ${it.user.id}".indexOf(query.toLowerCase()) > -1
}

fun Guild.searchTextChannels(query: String): List<TextChannel> = textChannels.filter {
    "${it.asMention} ${it.name.toLowerCase()} ${it.id}".indexOf(query.toLowerCase()) > -1
}

fun Guild.searchRoles(query: String): List<Role> = roles.filter {
    "${it.asMention} ${it.name.toLowerCase()} ${it.id}".indexOf(query.toLowerCase()) > -1
}

fun getStarColor(stars: Int): Color {
    var c = stars / 13

    if (c > 1) {
        c = 1
    }

    return Color(255, (194 * c) + (253 * (1 - c)), (12 * c) + (247 * (1 - c)))
}

fun Guild.addStar(msg: Message, user: User) {
    if (msg.author.id == user.id) {
        return
    }

    DatabaseWrapper.getGuildSafe(this).thenAccept { guild ->
        val channel = getTextChannelById(guild.starboardChannel ?: return@thenAccept) ?: return@thenAccept

        asyncTransaction(Uni.pool) {
            val stars = Starboard.select {
                Starboard.guildId.eq(idLong)
            }

            val star = stars.firstOrNull {
                it[Starboard.messageId] == msg.idLong
            }

            val embed = EmbedBuilder()

            if (star != null) {
                if (star[Starboard.stargazers].contains(user.idLong)) {
                    return@asyncTransaction
                }

                embed.apply {
                    setAuthor(msg.author.name, null, msg.author.avatarUrl)
                    setColor(getStarColor(star[Starboard.stargazers].size + 1))

                    if (msg.attachments.isNotEmpty()) {
                        setImage(msg.attachments.first().url)
                    }

                    descriptionBuilder.append(msg.contentRaw)
                }

                channel
                        .retrieveMessageById(star[Starboard.starId])
                        .queue({
                            it
                                    .editMessage(embed.build())
                                    .content("\u2b50 **${star[Starboard.stargazers].size + 1}** <#${msg.channel.id}> ID: ${msg.id}\nUrl: ${msg.jumpUrl}")
                                    .queue()
                        })


                Starboard.update({
                    Starboard.messageId.eq(msg.idLong)
                }) {
                    it[stargazers] = star[Starboard.stargazers] + user.idLong
                }
            } else {
                embed.apply {
                    setAuthor(msg.author.name, null, msg.author.avatarUrl)
                    setColor(getStarColor(1))

                    if (msg.attachments.isNotEmpty()) {
                        setImage(msg.attachments.first().url)
                    }

                    descriptionBuilder.append(msg.contentRaw)
                }

                channel
                        .sendMessage(embed.build())
                        .content("\u2b50 <#${msg.channel.id}> ID: ${msg.id}\nUrl: ${msg.jumpUrl}")
                        .queue { starMsg ->
                            asyncTransaction(Uni.pool) {
                                Starboard.insert {
                                    it[messageId] = msg.idLong
                                    it[guildId] = idLong
                                    it[channelId] = msg.channel.idLong
                                    it[starId] = starMsg.idLong
                                    it[stargazers] = arrayOf(user.idLong)
                                    it[content] = msg.contentRaw
                                    it[attachments] = msg.attachments.map { it.url }.toTypedArray()
                                }
                            }.execute()
                        }
            }

            return@asyncTransaction
        }.execute()
    }
}

fun Guild.removeStar(msg: Message, user: User) {
    DatabaseWrapper.getGuildSafe(this).thenAccept { guild ->
        val channel = getTextChannelById(guild.starboardChannel ?: return@thenAccept) ?: return@thenAccept

        asyncTransaction(Uni.pool) {
            val stars = Starboard.select {
                Starboard.guildId.eq(idLong)
            }

            val star = stars.firstOrNull {
                it[Starboard.messageId] == msg.idLong
            }

            if (star != null && star[Starboard.stargazers].contains(user.idLong)) {
                val gazers = star[Starboard.stargazers].size - 1

                val embed = EmbedBuilder().apply {
                    setAuthor(msg.author.name, null, msg.author.avatarUrl)
                    setColor(getStarColor(gazers))
                    setDescription(descriptionBuilder.append(msg.contentRaw))
                }

                if (gazers == 0) {
                    channel
                            .retrieveMessageById(star[Starboard.starId])
                            .queue({
                                it.delete().queue()
                            })

                    Starboard.deleteWhere {
                        Starboard.messageId.eq(msg.idLong)
                    }
                } else {
                    channel
                            .retrieveMessageById(star[Starboard.starId])
                            .queue({
                                it
                                        .editMessage(embed.build())
                                        .content("\u2b50 ${if (gazers == 1) "" else "**$gazers**"} <#${msg.channel.id}> ID: ${msg.id}\nUrl: ${msg.jumpUrl}")
                                        .queue()
                            })

                    val gazerIds = star[Starboard.stargazers]

                    Starboard.update({
                        Starboard.messageId.eq(msg.idLong)
                    }) {
                        it[stargazers] = gazerIds.drop(gazerIds.indexOf(user.idLong)).toTypedArray()
                    }
                }
            }
        }.execute()
    }
}
