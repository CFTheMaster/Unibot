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

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.MINIMUM_FOR_LEVEL_1
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.database.schema.Users
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.listeners.EventListener
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.jetbrains.exposed.sql.select
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Load
@Argument("user", "user", true)
@Alias("profile")
class ViewXP : Command(){
    override val guildOnly = true
    override val desc = "View someone's xp!"

    override fun run(ctx: Context) {
        val member = ctx.args.getOrDefault("user", ctx.member!!) as Member

        asyncTransaction(Uni.pool){
            val contract = Users.select{ Users.id.eq(member.user.idLong)}.firstOrNull()
                    ?: return@asyncTransaction ctx.send(
                            if (!member.user.isBot) "user has no xp: ${member.user.name+"#"+member.user.discriminator+" (${member.user.idLong})"}" else "bots don't have exp"
                    )
            ctx.send(EmbedBuilder().apply {
                setAuthor("Profile Info for: ${member.user.name}#${member.user.discriminator}", null, member.user.avatarUrl ?: null)
                val xp = contract[Users.expPoints]
                val level = contract[Users.level]

                val xpNeeded = level.toDouble() * (500).toDouble() + (level.toDouble() * MINIMUM_FOR_LEVEL_1.toDouble())
                val progress = xp.toDouble() / xpNeeded * (10).toDouble()
                setColor(member.colorRaw ?: 6684876)
                addField(
                        "Stats",
                        """**Rank:** ${contract[Users.level]}
                            |**Experience Points:** [${contract[Users.expPoints]}/${xpNeeded.toLong()}]
                            |**Progress:** [${"#".repeat(progress.toInt())}${"-".repeat(10 - progress.toInt())}] ${progress.toInt() * 10}%
                            |**Last Level Up:** [${contract[Users.lastLevelUp]}]
                            |**User Creation Date:** [${contract[Users.accountCreationDate]}]
                         """.trimMargin(),
                        true
                )
                setFooter("${contract[Users.lastLevelUp]} user last level up", null)
            }.build())
        }.execute()
    }
}
