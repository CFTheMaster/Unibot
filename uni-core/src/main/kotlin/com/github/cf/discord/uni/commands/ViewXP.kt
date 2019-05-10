package com.github.cf.discord.uni.commands

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.MINIMUM_FOR_LEVEL_1
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.database.schema.Users
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.listeners.EventListener
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.jetbrains.exposed.sql.select
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Load
@Argument("user", "user", true)
class ViewXP : Command(){
    override val guildOnly = true
    override val desc = "View someone's xp!"

    override fun run(ctx: Context) {
        val member = ctx.args.getOrDefault("user", ctx.member!!) as Member

        asyncTransaction(Uni.pool){
            val contract = Users.select{ Users.id.eq(member.user.idLong)}.firstOrNull()
                    ?: return@asyncTransaction ctx.send(
                            "user has no xp" +
                                    "\n${member.user.name}"
                    )
            ctx.send(EmbedBuilder().apply {
                setTitle("XP Info for: ${member.user.name}")
                val xp = contract[Users.expPoints]
                val level = contract[Users.level]

                val xpNeeded = level.toDouble() * (500).toDouble() + (level.toDouble() * MINIMUM_FOR_LEVEL_1.toDouble())
                val progress = xp.toDouble() / xpNeeded * (10).toDouble()

                addField(
                        "Stats",
                        """**Rank:** ${contract[Users.level]}
                            |**Progress:** [${"#".repeat(progress.toInt())}${"-".repeat(10 - progress.toInt())}] ${progress.toInt() * 10}%
                            |**Last level up** [${contract[Users.lastLevelUp]}]
                            |**User creation date** [${contract[Users.accountCreationDate]}]
                            |**User Total Exp** [${contract[Users.expPoints]}] **Total EXP Needed to level up** [$xpNeeded]
                         """.trimMargin(),
                        true
                )
                setFooter("${contract[Users.lastLevelUp]} user last level up", null)
            }.build())
        }.execute()
    }
}