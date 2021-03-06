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

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Member
import org.joda.time.DateTime
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Load
@Alias("user")
@Argument("user", "user", true)
class UserInfo : Command(){
    override val desc = "get info of a user"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val member = ctx.args["user"] as? Member ?: ctx.member!!
        val embed = EmbedBuilder().apply {
            val statusEmote = if (member.activities.isNotEmpty() && member.onlineStatus.key == "streaming") {
                "<:StreamingDOT:565631757509066752> "
            } else {
                when (member.onlineStatus.name) {
                    "ONLINE" -> "<:OnlineDOT:565631777025032223> "
                    "OFFLINE" -> "<:OfflineDOT:565631801935265792>"
                    "IDLE" -> "<:IdleDOT:565631819337433098>"
                    "DO_NOT_DISTURB" -> "<:DoNotDisturbDOT:565631840879247360>"
                    else -> "<:OfflineDOT:565631801935265792>"
                }
            }

            setTitle("$statusEmote ${member.user.name}#${member.user.discriminator}${if (!member.nickname.isNullOrEmpty()) " (${member.nickname})" else ""}")

            setThumbnail(member.user.avatarUrl)

            val totalDays = ChronoUnit.DAYS.between(member.user.timeCreated.toLocalDate(), OffsetDateTime.now().toLocalDate())

            // TODO add translations for these
            descriptionBuilder.append("**ID:** ${member.user.id}\n")
            descriptionBuilder.append("**Highest role:** ${member.roles.sortedBy { it.position }.last()?.name ?: "none"}\n")
            descriptionBuilder.append("**Playing:** ${member.activities.firstOrNull()?.name ?: "nothing"}\n")
            descriptionBuilder.append("**Joined Discord:** ${
            member.user.timeCreated.format(DateTimeFormatter.RFC_1123_DATE_TIME)
            }\n")
            descriptionBuilder.append("**${ctx.guild!!.name}: ** ${
            member.timeJoined.format(DateTimeFormatter.RFC_1123_DATE_TIME)
            }\n")
            setFooter(
                    "Total amount of days since creation: $totalDays",
                    member.user.effectiveAvatarUrl
            )
        }

        ctx.send(embed.build())
    }
}
