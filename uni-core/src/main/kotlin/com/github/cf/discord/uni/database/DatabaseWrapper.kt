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
package com.github.cf.discord.uni.database

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.database.schema.*
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.extensions.log
import com.github.cf.discord.uni.listeners.EventListener
import net.dv8tion.jda.api.entities.Guild
import org.joda.time.DateTime
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.lang.Exception
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService


data class DBGuild(
        val id: Long,
        val name: String,
        val prefix: String?,
        val starboard: Boolean,
        val starboardChannel: Long?,
        val logs: Boolean,
        val modlogs: Boolean,
        val modlogChannel: Long?,
        val welcome: Boolean,
        val welcomeChannel: Long?,
        val welcomeMessage: String,
        val leaveMessage: String,
        val levelMessages: Boolean,
        val mutedRole: Long?,
        val antiInvite: Boolean,
        val userRole: Boolean,
        val autoRole: Long?,
        val localLeveling: Boolean,
        val autoKick: Boolean,
        val accountAge: Long
)

data class DBStar(
        val messageId: Long,
        val channelId: Long,
        val guildId: Long,
        val starId: Long,
        val stars: List<Long>,
        val content: String,
        val attachments: List<String>
)

data class DBUser(
        val id: Long,
        val expPoints: Long,
        val level: Long,
        val lastLevelUp: DateTime,
        val accountCreationDate: DateTime,
        val lastMessage: Long,
        val customPrefix: String?,
        val localExp: Long,
        val localLevel: Long
)

data class DBWewCounter(
        val amount: Long
)

data class DBModLogs(
        val messageId: Long,
        val modId: Long,
        val guildId: Long,
        val targetId: Long,
        val caseId: Int,
        val type: String,
        val reason: String
)

data class DBCore(
        val discordToken: String,
        val nicoNicoEmail: String,
        val nicoNicoPassword: String,
        val googleApiKey: String,
        val googleSearchEngineID: String,
        val dblToken: String?,
        val sauceNaoToken: String,
        val osuToken: String,
        val discordBoatsToken: String?,
        val discordServicesToken: String?
)

object DatabaseWrapper {
    private val pool: ExecutorService = Uni.pool

    fun getCore(): CompletableFuture<DBCore> = asyncTransaction(pool){
        val core = Core.select { Core.discordToken.isNotNull() }.firstOrNull()

        if(core == null){
            throw Exception("nothing found, put stuff in the database")
        } else {
            return@asyncTransaction DBCore(
                    core[Core.discordToken],
                    core[Core.nicoNicoEmail],
                    core[Core.nicoNicoPassword],
                    core[Core.googleApiKey],
                    core[Core.googleSearchEngineID],
                    core[Core.dblToken]!!,
                    core[Core.sauceNaoToken],
                    core[Core.osuToken],
                    core[Core.discordBoatsToken]!!,
                    core[Core.discordServicesToken]!!
            )
        }
    }.execute()

    fun getGuild(guild: Guild) = getGuild(guild.idLong)

    fun getGuild(id: Long) = asyncTransaction(pool){
        val guild = Guilds.select{ Guilds.id.eq(id)}.firstOrNull()

        if(guild == null){
            throw Exception("Guild not found")
        } else {
            return@asyncTransaction DBGuild(
                    guild[Guilds.id],
                    guild[Guilds.name],
                    guild[Guilds.prefix],
                    guild[Guilds.starboard],
                    guild[Guilds.starboardChannel],
                    guild[Guilds.logs],
                    guild[Guilds.modlogs],
                    guild[Guilds.modLogChannel],
                    guild[Guilds.welcome],
                    guild[Guilds.welcomeChannel],
                    guild[Guilds.welcomeMessage],
                    guild[Guilds.leaveMessage],
                    guild[Guilds.levelMessages],
                    guild[Guilds.mutedRole],
                    guild[Guilds.antiInvite],
                    guild[Guilds.userRole],
                    guild[Guilds.autoRole],
                    guild[Guilds.localLeveling],
                    guild[Guilds.autoKick],
                    guild[Guilds.accountAge]
            )
        }
    }.execute()

    fun newGuild(guild: Guild) = asyncTransaction(pool){
        val selection = Guilds.select{
            Guilds.id.eq(guild.idLong)
        }

        if(selection.empty()){
            Guilds.insert {
                it[id] = guild.idLong
                it[name] = guild.name
                it[prefix] = ""
                it[starboard] = false
                it[starboardChannel] = guild.textChannels.firstOrNull {it.name.toLowerCase() == "starboard"}?.idLong
                it[logs] = false
                it[modlogs] = false
                it[modLogChannel] = guild.textChannels.firstOrNull{it.name.toLowerCase() == "modlogs"}?.idLong
                it[welcome] = false
                it[welcomeChannel] = guild.textChannels.firstOrNull{it.name.toLowerCase() == "welcome"}?.idLong
                it[welcomeMessage] = "Welcome %USER% to %SERVER%!"
                it[leaveMessage] = "%USER% \uD83D\uDC4B"
                it[levelMessages] = false
                it[mutedRole] = guild.roles.firstOrNull { it.name.toLowerCase() == "muted" }?.idLong
                it[antiInvite] = false
                it[userRole] = false
                it[autoRole] = 0L
                it[localLeveling] = false
                it[autoKick] = false
                it[accountAge] = 1L

            }
        }
    }.execute().exceptionally {
        LOGGER.error("Error while trying to insert guild with ID ${guild.id}", it)
    }

    fun remGuild(guild: Guild) = remGuild(guild.idLong)

    fun remGuild(id: Long) = asyncTransaction(pool) { Guilds.deleteWhere { Guilds.id.eq(id) } }.execute().thenApply {}.exceptionally {
        LOGGER.error("Error while trying to delete guild with ID $id", it)
    }

    fun getUser(user: User) = getUser(user.idLong)

    fun getUser(member: Member) = getUser(member.user.idLong)


    fun getUser(id: Long) = asyncTransaction(pool) {
        val user = Users.select { Users.id.eq(id) }.firstOrNull()

        if (user == null) {
            throw Exception("User not found")
        } else {
            return@asyncTransaction DBUser(
                    user[Users.id],
                    user[Users.expPoints],
                    user[Users.level],
                    user[Users.lastLevelUp],
                    user[Users.accountCreationDate],
                    user[Users.lastMessage],
                    user[Users.customPrefix],
                    user[Users.localExp],
                    user[Users.localLevel]
            )
        }
    }.execute()

    fun newUser(member: Member) = newUser(member.user)

    fun newUser(user: User) = asyncTransaction(pool) {
        val selection = Users.select{
            Users.id.eq(user.idLong)
        }

        if(selection.empty()){
            Users.insert {
                it[id] = user.idLong
                it[expPoints] = 0
                it[level] = 0
                it[lastLevelUp] = DateTime.now()
                it[accountCreationDate] = DateTime(user.timeCreated.toInstant())
                it[lastMessage] = 1L
                it[customPrefix] = ""
                it[localExp] = 0L
                it[localLevel] = 0L
            }
        }
    }.execute().exceptionally {
        LOGGER.error("Error while trying to insert user with ID ${user.id}", it)
    }

    fun getGuildSafe(guild: Guild): CompletableFuture<DBGuild> = asyncTransaction(pool) {
        val stored = Guilds.select { Guilds.id.eq(guild.idLong) }.firstOrNull()

        if (stored == null) {
            Guilds.insert {
                it[id] = guild.idLong
                it[name] = guild.name
                it[prefix] = ""
                it[starboard] = false
                it[starboardChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "starboard" }?.idLong
                it[logs] = false
                it[modlogs] = false
                it[modLogChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "modlogs" }?.idLong
                it[welcome] = false
                it[welcomeChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "welcome" }?.idLong
                it[welcomeMessage] = "Welcome %USER% to %SERVER%!"
                it[leaveMessage] = "%USER% \uD83D\uDC4B"
                it[levelMessages] = false
                it[mutedRole] = guild.roles.firstOrNull { it.name.toLowerCase() == "muted" }?.idLong
                it[antiInvite] = false
                it[userRole] = false
                it[autoRole] = 0L
                it[localLeveling] = false
                it[autoKick] = false
                it[accountAge] = 1L
            }

            DBGuild(
                    guild.idLong,
                    guild.name,
                    "",
                    false,
                    null,
                    false,
                    false,
                    null,
                    false,
                    null,
                    "Welcome %USER% to %SERVER%!",
                    "%USER% \uD83D\uDC4B",
                    false,
                    null,
                    false,
                    false,
                    0L,
                    false,
                    false,
                    1L
            )
        } else {
            DBGuild(
                    stored[Guilds.id],
                    stored[Guilds.name],
                    stored[Guilds.prefix],
                    stored[Guilds.starboard],
                    stored[Guilds.starboardChannel],
                    stored[Guilds.logs],
                    stored[Guilds.modlogs],
                    stored[Guilds.modLogChannel],
                    stored[Guilds.welcome],
                    stored[Guilds.welcomeChannel],
                    stored[Guilds.welcomeMessage],
                    stored[Guilds.leaveMessage],
                    stored[Guilds.levelMessages],
                    stored[Guilds.mutedRole],
                    stored[Guilds.antiInvite],
                    stored[Guilds.userRole],
                    stored[Guilds.autoRole],
                    stored[Guilds.localLeveling],
                    stored[Guilds.autoKick],
                    stored[Guilds.accountAge]
            )
        }
    }.execute()

    fun getUserSafe(member: Member) = getUserSafe(member.user)

    fun getUserSafe(user: User): CompletableFuture<DBUser> = asyncTransaction(pool) {
        val stored = Users.select { Users.id.eq(user.idLong) }.firstOrNull()

        if (stored == null) {
            Users.insert {
                it[id] = user.idLong
                it[expPoints] = 0
                it[level] = 0
                it[lastLevelUp] = DateTime.now()
                it[accountCreationDate] = DateTime.parse(user.timeCreated.toInstant().toString())
                it[lastMessage] = 1L
                it[customPrefix] = ""
                it[localExp] = 0L
                it[localLevel] = 0L

            }

            DBUser(
                    user.idLong,
                    0,
                    0,
                    DateTime.now(),
                    DateTime.parse(user.timeCreated.toInstant().toString()),
                    0L,
                    "",
                    0L,
                    0L
            )
        } else {
            DBUser(
                    stored[Users.id],
                    stored[Users.expPoints],
                    stored[Users.level],
                    stored[Users.lastLevelUp],
                    stored[Users.accountCreationDate],
                    stored[Users.lastMessage],
                    stored[Users.customPrefix],
                    stored[Users.localExp],
                    stored[Users.localLevel]
            )
        }
    }.execute()

    fun getWewAmountSafe(): CompletableFuture<DBWewCounter> = asyncTransaction(pool){
        val stored = WewCounter.select{ WewCounter.amount.eq(WewCounter.amount) }.firstOrNull()

        if(stored == null){
            WewCounter.insert {
                it[amount] = 0L
            }

            DBWewCounter(
                    0L
            )
        } else{
            DBWewCounter(
                    stored[WewCounter.amount]
            )
        }
    }.execute()

    fun setModLogCase(messageIdL: Long, modIdL: Long, guildIdL: Long, targetIdL: Long, caseIdL: Int, typeL: String, reasonL: String) = asyncTransaction(pool){

        ModLogs.insert {
            it[messageId] = messageIdL
            it[modId] = modIdL
            it[guildId] = guildIdL
            it[targetId] = targetIdL
            it[caseId] = caseIdL
            it[type] = typeL
            it[reason] = reasonL
            }

        DBModLogs(
                messageIdL,
                modIdL,
                guildIdL,
                targetIdL,
                caseIdL,
                typeL,
                reasonL)

    }.execute()

    fun logEvent(event: Event) = asyncTransaction(pool) {
        when (event) {
            is MessageDeleteEvent -> {
                val log = Logs.select {
                    Logs.messageId.eq(event.messageIdLong)
                }.firstOrNull()

                if (log != null) {
                    EventListener.snipes.put(event.channel.idLong, log[Logs.messageId])

                    Logs.insert {
                        it[Logs.event] = "DELETE"
                        it[messageId] = log[Logs.messageId]
                        it[content] = log[Logs.content]
                        it[attachments] = log[Logs.attachments]
                        it[embeds] = log[Logs.embeds]
                        it[timestamp] = log[Logs.timestamp]
                        it[authorId] = log[Logs.authorId]
                        it[authorName] = log[Logs.authorName]
                        it[authorDiscrim] = log[Logs.authorDiscrim]
                        it[authorAvatar] = log[Logs.authorAvatar]
                        it[authorNick] = log[Logs.authorNick]
                        it[guildId] = log[Logs.guildId]
                        it[guildName] = log[Logs.guildName]
                        it[channelId] = log[Logs.channelId]
                        it[channelName] = log[Logs.channelName]
                    }
                }
            }
            is MessageReceivedEvent -> event.message.log()
            is MessageUpdateEvent -> event.message.log("UPDATE")
            else -> throw Exception("Not a valid event to log")
        }
    }.execute().exceptionally {
        LOGGER.error("Error while trying to insert message in db with ID: $event", it)
    }
}
