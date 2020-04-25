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
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.commands.system.Category
import com.github.cf.discord.uni.database.schema.Logs
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.listeners.EventListener
import net.dv8tion.jda.api.EmbedBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select


@Load
class Snipe : Command() {
    override val desc = "Snipe the latest deleted message."
    override val guildOnly = true
    override val cate = Category.MODERATION.name

    override fun run(ctx: Context) {
        if (!ctx.storedGuild!!.logs) {
            return ctx.send("logs not enabled: ${ctx.author.name}")
        }

        asyncTransaction(Uni.pool) {
            val snipe: Long? = EventListener.snipes.remove(ctx.channel.idLong)

            if (snipe != null) {
                val log = Logs.select {
                    Logs.guildId.eq(ctx.guild!!.idLong) and Logs.messageId.eq(snipe)
                }.first()

                val embed = EmbedBuilder().apply {
                    setAuthor("${log[Logs.authorName]}#${log[Logs.authorDiscrim]}", null, log[Logs.authorAvatar])
                    val regex = "(https?)?:?(//)?discord(app)?.?(gg|io|me|com|net)?/(\\w+:?\\w*@)?(\\S+)(:[0-9]+)?(/|/([\\w#!:.?+=&%@!-/]))?".toRegex()
                    descriptionBuilder.append(log[Logs.content].replace(regex, "[INVITE REDACTED]"))
                    setFooter("sniped by ${ctx.author.name}#${ctx.author.discriminator}", null)
                }

                ctx.send(embed.build())
            } else {
                ctx.send("no text has been found ;(")
            }
        }.execute()
    }
}
