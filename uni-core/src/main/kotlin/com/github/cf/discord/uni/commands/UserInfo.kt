package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import java.time.format.DateTimeFormatter

@Load
@Alias("user")
@Argument("user", "user", true)
class UserInfo : Command(){
    override val desc = "get info of a user"
    override val guildOnly = true

    override fun run(ctx: Context) {
        val member = ctx.args["user"] as? Member ?: ctx.member!!
        val embed = EmbedBuilder().apply {
            val statusEmote = if (member.game != null && member.game.type == Game.GameType.STREAMING) {
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

            // TODO add translations for these
            descriptionBuilder.append("**ID:** ${member.user.id}\n")
            descriptionBuilder.append("**Highest role:** ${member.roles.sortedBy { it.position }.last()?.name ?: "none"}\n")
            descriptionBuilder.append("**Playing:** ${member.game?.name ?: "nothing"}")

            setFooter(
                    "Joined Discord on ${
                    member.user.creationTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
                    }, ${ctx.guild!!.name} on ${
                    member.joinDate.format(DateTimeFormatter.RFC_1123_DATE_TIME)
                    }",
                    null
            )
        }

        ctx.send(embed.build())
    }
}