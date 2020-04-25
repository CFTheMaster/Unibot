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
package com.github.cf.discord.uni.listeners

import com.github.cf.discord.uni.CommandHandler
import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.Uni.Companion.MINIMUM_FOR_LEVEL_1
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.data.botOwners
import com.github.cf.discord.uni.database.DatabaseWrapper
import com.github.cf.discord.uni.database.schema.*
import com.github.cf.discord.uni.extensions.addStar
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.extensions.log
import com.github.cf.discord.uni.extensions.removeStar
import com.github.cf.discord.uni.stateful.EventWaiter
import com.github.cf.discord.uni.utils.Http
import gnu.trove.TLongLongHashMap
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.audit.AuditLogEntry
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import org.json.JSONObject
import java.awt.Color
import java.lang.Exception
import java.time.OffsetDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.Date
import java.util.Random
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.timerTask

class EventListener : ListenerAdapter(){
    private val timer = Timer(true)

    private fun cases(caseName: String, case: Int, audit: AuditLogEntry, user: User): CharSequence{
        val caseText = "```$caseName | Case $case\n" +
                "User: ${user.name}#${user.discriminator} (${user.id})\n" +
                "Reason: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}\n" +
                "Responsible moderator: ${audit.user!!.name}#${audit.user!!.discriminator} (${audit.user!!.id})\n" +
                "```"

        return caseText
    }

    fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onReady(event: ReadyEvent) {
        LOGGER.info("Bot is ready for action")

        if(Uni.jda == null){
            if(Uni.shardManager.shards.all { it.status == JDA.Status.CONNECTED || it.status == JDA.Status.LOADING_SUBSYSTEMS }){
                updateStats()
                startPresenceTimer()
            }
        } else {
            updateStats()
            startPresenceTimer()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if(event.guild != null){
            if(event.guild.idLong == 138303776170835969L){
                if(event.message.contentRaw.toLowerCase().contains("wew") && event.channel.idLong == 211956357841027072L){
                asyncTransaction(Uni.pool){
                    val ass = WewCounter.select {WewCounter.amount.eq(WewCounter.amount)}.firstOrNull() ?: return@asyncTransaction
                    val aNumberOrSomething = ass[WewCounter.amount]


                        val wewAmount = aNumberOrSomething+1
                        WewCounter.update({WewCounter.amount.eq((wewAmount-1))}) {
                            it[amount] = wewAmount
                            event.jda.getTextChannelById(568414725097127947L)!!.sendMessage("wew count: $wewAmount").queue()
                        }
                    }.execute().exceptionally {
                        LOGGER.error("Error while trying to add wew to the counter", it)
                    }
                }
            }

            DatabaseWrapper.getGuildSafe(event.guild).thenAccept{ stored ->
                if(stored.logs){
                    event.message.log()
                }

                if(event.author.isBot){
                    return@thenAccept
                }

                DatabaseWrapper.getUserSafe(event.member!!).thenAccept {user ->
                    try {
                        cmdHandler.handleMessage(event, user, stored)
                    } catch (e: Exception){
                        LOGGER.error("Error while trying to handle the message: $e")
                    }
                }

                if(stored.antiInvite){
                    val regex = "(https?)?:?(//)?discord(app)?.?(gg|io|me|com|net)?/(\\w+:?\\w*@)?(\\S+)(:[0-9]+)?(/|/([\\w#!:.?+=&%@!-/]))?".toRegex()

                    if(event.member!!.user.id !in botOwners.authors && !event.member!!.isOwner && regex.containsMatchIn(event.message.contentRaw) && !event.member!!.permissions.contains(Permission.KICK_MEMBERS)){
                        event.message.delete().queue ({
                            event.channel.sendMessage("${event.message.author.name} (${event.message.author.id}): please do not post any ads").queue()
                        })
                        {
                            event.channel.sendMessage("error while trying to delete message $it").queue()
                            LOGGER.error("error while trying to remove ad", it)
                        }
                    }
                }

                if(stored.localLeveling){
                    asyncTransaction(Uni.pool){
                        val userExists = Users.select { Users.id.eq(event.author.idLong) }.firstOrNull() ?: return@asyncTransaction
                        val currentLocalLevel = userExists[Users.localLevel]
                        val localExpGotten = userExists[Users.localExp]

                        val xpUpdate = localExpGotten+(1..8).random()
                        Users.update({
                            Users.id.eq(event.author.idLong)
                        }) {
                            it[localExp] = xpUpdate
                        }

                        val localExpNeeded = currentLocalLevel.toDouble() * (500).toDouble() + (currentLocalLevel.toDouble() * MINIMUM_FOR_LEVEL_1.toDouble())

                        if(localExpGotten >= localExpNeeded){
                            Users.update({
                                Users.id.eq(event.author.idLong)
                            }) {
                                it[localLevel] = currentLocalLevel + 1
                                it[localExp] = localExpGotten - 1
                                it[lastLevelUp] = DateTime.now()
                            }

                            if (stored.levelMessages) {
                                event.channel.sendMessage(EmbedBuilder().apply {
                                    setTitle("${event.author.name}, you are now local rank ${currentLocalLevel + 1}!") // TODO translation
                                    setColor(Color.ORANGE)

                                }.build()).queue()
                            }
                        }
                    }.execute().exceptionally {
                        LOGGER.error("Error while trying to levelup user ${event.author.name}#${event.author.discriminator} (${event.author.id}", it)
                    }
                }


                asyncTransaction(Uni.pool) {
                    val exists = Users.select{ Users.id.eq(event.author.idLong)}.firstOrNull() ?: return@asyncTransaction
                    val curLevel = exists[Users.level]
                    val xp = exists[Users.expPoints]
                    val lastMsg = exists[Users.lastMessage]

                    if(event.message.idLong > lastMsg){
                        val xpGet = xp+(1..5).random()
                        Users.update({Users.id.eq(event.author.idLong)}) {
                            it[expPoints] = xpGet
                            it[lastMessage] = event.message.idLong
                        }
                    }

                    val xpNeeded = (curLevel.toDouble() * (50).toDouble()) * (MINIMUM_FOR_LEVEL_1 * (curLevel.toDouble() / (3).toDouble()))

                    if (xp >= xpNeeded) {
                        Users.update({
                            Users.id.eq(event.author.idLong)
                        }) {
                            it[level] = curLevel + 1
                            it[expPoints] = xp - 1
                            it[lastLevelUp] = DateTime.now()
                        }

                        if (stored.levelMessages) {
                            event.channel.sendMessage(EmbedBuilder().apply {
                                setTitle("${event.author.name}, you are now rank ${curLevel + 1}!") // TODO translation
                                setColor(Color.ORANGE)

                            }.build()).queue()
                        }
                    }
                }.execute().exceptionally {
                    LOGGER.error("Error while trying to levelup user ${event.author.name}#${event.author.discriminator} (${event.author.id}", it)
                }
            }
        } else {
            if(event.author.isBot) {
                return
            }

            DatabaseWrapper.getUserSafe(event.author).thenAccept { user ->
                try {
                    cmdHandler.handleMessage(event, user)
                } catch (e: Exception){
                    LOGGER.error("error while trying to handle the message", e)
                }
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.guild != null) {
            DatabaseWrapper.getGuildSafe(event.guild).thenAccept { guild ->
                if (guild.logs) {
                    DatabaseWrapper.logEvent(event)
                }
            }
        }
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        LOGGER.info("New guild: ${event.guild.name} (${event.guild.id})")

        updateStats()

        Uni.shardManager.getGuildById(138303776170835969)!!.getTextChannelById(440833941335703572)!!.sendMessage(EmbedBuilder()
                .setAuthor("Joined guild", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setThumbnail(if (event.guild.iconUrl != null) event.guild.iconUrl else "https://maxcdn.icons8.com/Share/icon/Logos/discord_logo1600.png")
                .setColor(java.lang.Integer.parseInt("#6600cc".replaceFirst("#", ""), 16))
                .addField("Joined Guild: ", event.guild.name, true)
                .addField("Server/Guild ID: ", "${event.guild.idLong}", true)
                .addField("Server/Guild Owner: ", "${event.guild.owner!!.user.name}#${event.guild.owner!!.user.discriminator}", true)
                .addField("Server/Guild Owner ID: ", "${event.guild.owner!!.user.idLong}", true)
                .addField("Creation Date: ", event.guild.timeCreated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), true)
                .addField("Guild Members: ", "${event.guild.members.size}", true)
                .addField("Bots: ", "${event.guild.members.filter { it.user.isBot }.size}", true)
                .addField("Highest role: ", "${event.guild.roles.get(0).name ?: "none"}\n", true)
                .addField("Text Channels: ", "${event.guild.textChannels.size}", true)
                .addField("Voice Channels: ", " ${event.guild.voiceChannels.size} ", true)
                .addField("Total amount of guilds: ", "${Uni.shardManager.guilds.size}", true)
                .addBlankField(true)
                .build()).queue()
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        LOGGER.info("Left guild: ${event.guild.name} (${event.guild.id}")

        asyncTransaction(Uni.pool) {
            ModLogs.deleteWhere { ModLogs.guildId.eq(event.guild.idLong) }
            Starboard.deleteWhere { Starboard.guildId.eq(event.guild.idLong) }
            Guilds.deleteWhere { Guilds.id.eq(event.guild.idLong) }
        }.execute().thenApply {}.exceptionally {
            LOGGER.error("Error while trying to remove database entries for guild with id ${event.guild.id}", it)
        }

        updateStats()

        Uni.shardManager.getGuildById(138303776170835969)!!.getTextChannelById(440833941335703572)!!.sendMessage(EmbedBuilder()
                .setAuthor("Left guild", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setThumbnail(if(event.guild.iconUrl != null) event.guild.iconUrl else "https://maxcdn.icons8.com/Share/icon/Logos/discord_logo1600.png")
                .setColor(java.lang.Integer.parseInt("#6600cc".replaceFirst("#", ""), 16))
                .addField("Left Guild: ", event.guild.name, true)
                .addField("Server/Guild ID: ", "${event.guild.idLong}", true)
                .addField("Server/Guild Owner: ", "${event.guild.owner!!.user.name}#${event.guild.owner!!.user.discriminator}", true)
                .addField("Server/Guild Owner ID: ", "${event.guild.owner!!.user.idLong}", true)
                .addField("Creation Date: ", event.guild.timeCreated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), true)
                .addField("Guild Members: ", "${event.guild.members.size}", true)
                .addField("Bots: ","${event.guild.members.filter { it.user.isBot }.size}", true)
                .addField("Highest role: ", "${event.guild.roles.get(0).name ?: "none"}\n", true)
                .addField("Text Channels: ", "${event.guild.textChannels.size}", true)
                .addField("Voice Channels: ", " ${event.guild.voiceChannels.size} ", true)
                .addField("Total amount of guilds: ", "${Uni.shardManager.guilds.size}", true)
                .addBlankField(true)
                .build()).queue()
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (event.guild != null) {
            DatabaseWrapper.getGuildSafe(event.guild).thenAccept { guild ->
                if (guild.logs) {
                    event.message.log("UPDATE")
                }
            }
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.guild != null && event.reaction.reactionEmote.name == "\u2b50") {
            DatabaseWrapper.getGuildSafe(event.guild).thenAccept { guild ->
                if (!guild.starboard) {
                    return@thenAccept
                }

                event.channel.retrieveMessageById(event.messageId).queue { msg ->
                    event.guild.addStar(msg, event.user!!)
                }
            }
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (event.guild != null && event.reaction.reactionEmote.name == "\u2b50") {
            DatabaseWrapper.getGuildSafe(event.guild).thenAccept { guild ->
                if (!guild.starboard) {
                    return@thenAccept
                }

                event.channel.retrieveMessageById(event.messageId).queue { msg ->
                    event.guild.removeStar(msg, event.user!!)
                }
            }
        }
    }

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.member.user.id == event.jda.selfUser.id
                || !event.channelLeft.members.any { it.user.id == event.jda.selfUser.id }
                || event.channelLeft.members.size > 1) {
            return
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        asyncTransaction(Uni.pool) {
            val roles = Roles.select {
                Roles.userId.eq(event.user.idLong) and Roles.guildId.eq(event.guild.idLong)
            }

            for (role in roles) {
                val id = role[Roles.roleId]
                val r = event.guild.getRoleById(id)

                if (r != null) {
                    event.guild.addRoleToMember(event.member, r).queue()
                }
            }
        }.execute().thenApply {}.exceptionally {
            it.printStackTrace()
        }

        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { stored ->
            if(stored.autoKick){
                val totalDays = ChronoUnit.DAYS.between(event.member.user.timeCreated.toLocalDate(), OffsetDateTime.now().toLocalDate())
                if(totalDays <= stored.accountAge){
                    event.guild.kick(event.member)
                            .reason("[AutoKick] ${event.member.user.name}#${event.member.user.discriminator} (${event.member.user.idLong}) has been kicked because account was made too recent")
                            .queue()
                }
            }
        }

        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            val channel = event.guild.getTextChannelById(storedGuild.welcomeChannel ?: return@thenAccept) ?: return@thenAccept

            if (storedGuild.welcome && storedGuild.welcomeMessage.isNotBlank()) {
                channel.sendMessage(
                        storedGuild.welcomeMessage
                                .replace("%USER%", event.user.asMention)
                                .replace("%USERNAME%", event.user.name)
                                .replace("%SERVER%", event.guild.name)
                                .replace("%MEMBERNUM%", (event.guild.members.indexOf(event.member) + 1).toString())
                ).queue()
            }
        }

        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            val role = event.guild.getRoleById(storedGuild.autoRole ?: 0L) ?: return@thenAccept

            if (storedGuild.userRole && storedGuild.autoRole != 0L){
                event.guild
                        .addRoleToMember(event.member, role)
                        .reason("auto role for guild: ${event.guild.name}")
                        .queue()
            }
        }
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            asyncTransaction(Uni.pool) {
                if (!storedGuild.modlogs) {
                    return@asyncTransaction
                }

                val modlogs = ModLogs.select { ModLogs.guildId.eq(event.guild.idLong) }
                val modlogChannel = event.guild.getTextChannelById(storedGuild.modlogChannel ?: return@asyncTransaction) ?: return@asyncTransaction
                val audit = event.guild.retrieveAuditLogs().type(ActionType.KICK).limit(2).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction
                val case = modlogs.count() + 1

                modlogChannel.sendMessage(cases("KICK", case, audit, event.user)).complete()

                Timer().schedule(
                        timerTask {
                            DatabaseWrapper.setModLogCase(
                                    modlogChannel.latestMessageIdLong,
                                    audit.user!!.idLong,
                                    event.guild.idLong,
                                    audit.targetIdLong,
                                    case,
                                    "KICK",
                                    audit.reason ?: "none")
                        },500)
            }.execute()

            val channel = event.guild.getTextChannelById(storedGuild.welcomeChannel ?: return@thenAccept) ?: return@thenAccept

            if (storedGuild.welcome && storedGuild.leaveMessage.isNotBlank()) {
                channel.sendMessage(
                        storedGuild.leaveMessage
                                .replace("%USER%", event.user.asMention)
                                .replace("%USERNAME%", event.user.name)
                                .replace("%SERVER%", event.guild.name)
                ).queue()
            }
        }
    }

    override fun onGuildUnban(event: GuildUnbanEvent) {
        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            asyncTransaction(Uni.pool) {
                if (!storedGuild.modlogs) {
                    return@asyncTransaction
                }

                val modlogs = ModLogs.select { ModLogs.guildId.eq(event.guild.idLong) }
                val modlogChannel = event.guild.getTextChannelById(storedGuild.modlogChannel ?: return@asyncTransaction) ?: return@asyncTransaction
                val audit = event.guild.retrieveAuditLogs().type(ActionType.UNBAN).first { it.targetId == event.user.id }
                val case = modlogs.count() + 1

                modlogChannel.sendMessage(cases("UNBAN", case, audit, event.user)).complete()

                Timer().schedule(
                        timerTask {
                            DatabaseWrapper.setModLogCase(
                                    modlogChannel.latestMessageIdLong,
                                    audit.user!!.idLong,
                                    event.guild.idLong,
                                    audit.targetIdLong,
                                    case,
                                    "UNBAN",
                                    audit.reason ?: "none")
                        },500)
            }.execute()
        }
    }


    override fun onGuildBan(event: GuildBanEvent) {
        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            asyncTransaction(Uni.pool) {
                if (!storedGuild.modlogs) {
                    return@asyncTransaction
                }

                val modlogs = ModLogs.select { ModLogs.guildId.eq(event.guild.idLong) }
                val modlogChannel = event.guild.getTextChannelById(storedGuild.modlogChannel ?: return@asyncTransaction) ?: return@asyncTransaction
                val audit = event.guild.retrieveAuditLogs().type(ActionType.BAN).first { it.targetId == event.user.id }
                val case = modlogs.count() + 1

                modlogChannel.sendMessage(cases("BAN", case, audit, event.user)).complete()

                Timer().schedule(
                        timerTask {
                            DatabaseWrapper.setModLogCase(
                                    modlogChannel.latestMessageIdLong,
                                    audit.user!!.idLong,
                                    event.guild.idLong,
                                    audit.targetIdLong,
                                    case,
                                    "BAN",
                                    audit.reason ?: "none")
                        },500)
            }.execute()
        }
    }

    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent) {
        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            asyncTransaction(Uni.pool) {
                if (!storedGuild.modlogs
                        || !event.roles.contains(event.guild.getRoleById(storedGuild.mutedRole ?: return@asyncTransaction) ?: return@asyncTransaction))
                    return@asyncTransaction

                val modlogs = ModLogs.select { ModLogs.guildId.eq(event.guild.idLong) }
                val modlogChannel = event.guild.getTextChannelById(storedGuild.modlogChannel ?: return@asyncTransaction) ?: return@asyncTransaction
                val audit = event.guild.retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction

                Roles.insert {
                    it[userId] = event.user.idLong
                    it[guildId] = event.guild.idLong
                    it[roleId] = event.roles.first().idLong
                }

                val case = modlogs.count() + 1

                modlogChannel.sendMessage(cases("MUTE", case, audit, event.user)).complete()

                Timer().schedule(
                        timerTask {
                            DatabaseWrapper.setModLogCase(
                                    modlogChannel.latestMessageIdLong,
                                    audit.user!!.idLong,
                                    event.guild.idLong,
                                    audit.targetIdLong,
                                    case,
                                    "MUTE",
                                    audit.reason ?: "none")
                        },500)
            }.execute()
        }
    }

    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent) {
        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            asyncTransaction(Uni.pool) {
                if (!storedGuild.modlogs
                        || !event.roles.contains(event.guild.getRoleById(storedGuild.mutedRole ?: return@asyncTransaction) ?: return@asyncTransaction))
                    return@asyncTransaction

                val modlogs = ModLogs.select { ModLogs.guildId.eq(event.guild.idLong) }
                val modlogChannel = event.guild.getTextChannelById(storedGuild.modlogChannel ?: return@asyncTransaction) ?: return@asyncTransaction
                val audit = event.guild.retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction

                Roles.deleteWhere {
                    Roles.guildId.eq(event.guild.idLong) and Roles.userId.eq(event.user.idLong) and Roles.roleId.eq(event.roles.first().idLong)
                }

                val case = modlogs.count() + 1

                modlogChannel.sendMessage(cases("UNMUTE", case, audit, event.user)).complete()


                Timer().schedule(
                        timerTask {
                            DatabaseWrapper.setModLogCase(
                                    modlogChannel.latestMessageIdLong,
                                    audit.user!!.idLong,
                                    event.guild.idLong,
                                    audit.targetIdLong,
                                    case,
                                    "UNMUTE",
                                    audit.reason ?: "none")
                        },500)
            }.execute()
        }
    }

    private fun startPresenceTimer() {
        fixedRateTimer("change_status_timer",false, 0L, TimeUnit.MINUTES.toMillis(10L)) {
            updateYourNamePresence()
        }
    }

    private fun updateYourNamePresence() {
        val text = EnvVars.RANDOM_TEXT!!.split("::")
        val idx = (0 until text.size).random()
        val random = text[idx]
        val prefix = Uni.prefixes.firstOrNull()
        if (Uni.jda != null){
            Uni.jda!!.presence.setPresence(OnlineStatus.ONLINE, Activity.streaming("${random.replace("__xx__", "${Uni.shardManager.getUserById(138302166619258880L)?.name}#${Uni.shardManager.getUserById(138302166619258880L)?.discriminator}", false)} | ${prefix}help", "https://www.twitch.tv/computerfreaker"))
        } else {
            Uni.shardManager.setActivity(Activity.streaming("${random.replace("__xx__", "${Uni.shardManager.getUserById(138302166619258880L)?.name}#${Uni.shardManager.getUserById(138302166619258880L)?.discriminator}", false)} | ${prefix}help", "https://www.twitch.tv/computerfreaker"))
        }
    }

    companion object {
        val waiter = EventWaiter()

        fun updateStats(){
            val jsonType = MediaType.parse("application/json")
            if (Uni.jda != null){
                val json = mapOf("server_count" to Uni.jda!!.guilds.size)
                val body = RequestBody.create(jsonType, JSONObject(json).toString())

                if(DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).dblToken!!.isNotEmpty()){
                    Http.post("https://discordbots.org/api/bots/${Uni.jda!!.selfUser.id}/stats", body){
                        addHeader("Authorization", DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).dblToken!!)
                    }.thenAccept{
                        LOGGER.info("updated stats for DBL")
                        it.close()
                    }.thenApply {}.exceptionally {
                        LOGGER.error("Error while updating stats", it)
                    }
                }
            } else {
                for (shard in Uni.shardManager.shards){
                    val json = mapOf(
                            "server_count" to Uni.shardManager.guilds.size,
                            "shard_count" to Uni.shardManager.shardsTotal
                    )

                    val jsonBoats = mapOf(
                            "server_count" to Uni.shardManager.guilds.size
                    )

                    val body = RequestBody.create(jsonType, JSONObject(json).toString())

                    val bodyBoats = RequestBody.create(jsonType, JSONObject(jsonBoats).toString())

                    if(DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).dblToken!!.isNotEmpty()){
                        Http.post("https://discordbots.org/api/bots/${shard.selfUser.idLong}/stats", body){
                            addHeader("Authorization", DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).dblToken!!)
                        }.thenAccept{
                            LOGGER.info("updated stats for DBL")
                            it.close()
                        }.thenApply {}.exceptionally {
                            LOGGER.error("Error while updating stats", it)
                        }
                    }

                    if(DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).discordBoatsToken!!.isNotEmpty()){
                        Http.post("https://discord.boats/api/v2/bot/${shard.selfUser.idLong}", bodyBoats){
                            addHeader("Authorization", DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).discordBoatsToken!!)
                        }.thenAccept {
                            LOGGER.info("updated stats for Discord Boats")
                            it.close()
                        }.thenApply {}.exceptionally {
                            LOGGER.error("Error While Updating Stats", it)
                        }
                    }
                }
            }
        }

        val snipes = TLongLongHashMap()
        val cmdHandler = CommandHandler()
    }
}
