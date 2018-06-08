package com.github.cf.discord.uni.db

import io.sentry.Sentry
import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.async.asyncTransaction
import com.github.cf.discord.uni.db.schema.*
import mu.KotlinLogging
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.GenericMessageEvent
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import kotlin.reflect.jvm.jvmName

data class DBGuild(
        val id: Long,
        val name: String,
        val lang: String,
        val prefixes: List<String>,
        val forceLang: Boolean,
        val starboard: Boolean,
        val starboardChannel: Long?,
        val logs: Boolean,
        val modlogs: Boolean,
        val modlogChannel: Long?,
        val rolemeRoles: Map<String, Long>,
        val welcome: Boolean,
        val welcomeChannel: Long?,
        val welcomeMessage: String,
        val leaveMessage: String,
        val ignoredChannels: List<Long>,
        val levelMessages: Boolean,
        val mutedRole: Long?,
        val antiInvite: Boolean
)

data class DBUser(
        val id: Long,
        val username: String,
        val discriminator: String,
        val lang: String,
        val marriedUserId: Long?
)

data class DBStar(
        val messageId: Long,
        val channelId: Long,
        val guildId: Long,
        val starId: Long,
        val stargazers: List<Long>,
        val content: String,
        val attachments: List<String>
)

data class DBContract(
        val userId: Long,
        val wish: String,
        val date: DateTime
)

object Database {
    private val logger = KotlinLogging.logger(this::class.jvmName)
    private val pool: ExecutorService = Uni.pool

    fun getGuild(guild: Guild) = getGuild(guild.idLong)

    fun getGuild(id: Long) = asyncTransaction(pool) {
        val guild = Guilds.select { Guilds.id.eq(id) }.firstOrNull()

        if (guild == null) {
            throw Exception("Guild not found")
        } else {
            return@asyncTransaction DBGuild(
                    guild[Guilds.id],
                    guild[Guilds.name],
                    guild[Guilds.lang],
                    guild[Guilds.prefixes].toList(),
                    guild[Guilds.forceLang],
                    guild[Guilds.starboard],
                    guild[Guilds.starboardChannel],
                    guild[Guilds.logs],
                    guild[Guilds.modlogs],
                    guild[Guilds.modlogChannel],
                    guild[Guilds.rolemeRoles],
                    guild[Guilds.welcome],
                    guild[Guilds.welcomeChannel],
                    guild[Guilds.welcomeMessage],
                    guild[Guilds.leaveMessage],
                    guild[Guilds.ignoredChannels].toList(),
                    guild[Guilds.levelMessages],
                    guild[Guilds.mutedRole],
                    guild[Guilds.antiInvite]
            )
        }
    }.execute()

    fun newGuild(guild: Guild) = asyncTransaction(pool) {
        val selection = Guilds.select {
            Guilds.id.eq(guild.idLong)
        }

        if (selection.empty()) {
            Guilds.insert {
                it[id] = guild.idLong
                it[lang] = "en_US"
                it[forceLang] = false
                it[prefixes] = arrayOf()
                it[name] = guild.name
                it[starboard] = false
                it[starboardChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "starboard" }?.idLong
                it[logs] = false
                it[modlogs] = false
                it[modlogChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "modlogs" }?.idLong
                it[rolemeRoles] = mapOf()
                it[welcome] = false
                it[welcomeChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "welcome" }?.idLong
                it[welcomeMessage] = "Welcome %USER% to %SERVER%!"
                it[leaveMessage] = "%USER% \uD83D\uDC4B"
                it[ignoredChannels] = arrayOf()
                it[levelMessages] = false
                it[mutedRole] = guild.roles.firstOrNull { it.name.toLowerCase() == "muted" }?.idLong
                it[antiInvite] = false
            }
        }
    }.execute().exceptionally {
        logger.error("Error while trying to insert guild with ID ${guild.id}", it)
        Sentry.capture(it)
    }

    fun remGuild(guild: Guild) = remGuild(guild.idLong)

    fun remGuild(id: Long) = asyncTransaction(pool) { Guilds.deleteWhere { Guilds.id.eq(id) } }.execute().thenApply {}.exceptionally {
        logger.error("Error while trying to delete guild with ID $id", it)
        Sentry.capture(it)
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
                    user[Users.username],
                    user[Users.discriminator],
                    user[Users.lang],
                    user[Users.marriedUserId]
            )
        }
    }.execute()

    fun newUser(member: Member) = newUser(member.user)

    fun newUser(user: User) = asyncTransaction(pool) {
        val selection = Users.select {
            Users.id.eq(user.idLong)
        }

        if (selection.empty()) {
            Users.insert {
                it[id] = user.idLong
                it[username] = user.name
                it[discriminator] = user.discriminator
                it[lang] = "en_US"
                it[marriedUserId] = null
            }
        }
    }.execute().exceptionally {
        logger.error("Error while trying to insert user with ID ${user.id}", it)
        Sentry.capture(it)
    }

    fun getGuildSafe(guild: Guild): CompletableFuture<DBGuild> = asyncTransaction(pool) {
        val stored = Guilds.select { Guilds.id.eq(guild.idLong) }.firstOrNull()

        if (stored == null) {
            Guilds.insert {
                it[id] = guild.idLong
                it[lang] = "en_US"
                it[forceLang] = false
                it[prefixes] = arrayOf()
                it[name] = guild.name
                it[starboard] = false
                it[starboardChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "starboard" }?.idLong
                it[logs] = false
                it[modlogs] = false
                it[modlogChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "modlogs" }?.idLong
                it[rolemeRoles] = mapOf()
                it[welcome] = false
                it[welcomeChannel] = guild.textChannels.firstOrNull { it.name.toLowerCase() == "welcome" }?.idLong
                it[welcomeMessage] = "Welcome %USER% to %SERVER%!"
                it[leaveMessage] = "%USER% \uD83D\uDC4B"
                it[ignoredChannels] = arrayOf()
                it[levelMessages] = false
                it[mutedRole] = guild.roles.firstOrNull { it.name.toLowerCase() == "muted" }?.idLong
                it[antiInvite] = false
            }

            DBGuild(
                    guild.idLong,
                    guild.name,
                    "en_US",
                    listOf(),
                    false,
                    false,
                    null,
                    false,
                    false,
                    null,
                    mapOf(),
                    false,
                    null,
                    "Welcome %USER% to %SERVER%!",
                    "%USER% \uD83D\uDC4B",
                    listOf(),
                    false,
                    null,
                    false
            )
        } else {
            DBGuild(
                    stored[Guilds.id],
                    stored[Guilds.name],
                    stored[Guilds.lang],
                    stored[Guilds.prefixes].toList(),
                    stored[Guilds.forceLang],
                    stored[Guilds.starboard],
                    stored[Guilds.starboardChannel],
                    stored[Guilds.logs],
                    stored[Guilds.modlogs],
                    stored[Guilds.modlogChannel],
                    stored[Guilds.rolemeRoles],
                    stored[Guilds.welcome],
                    stored[Guilds.welcomeChannel],
                    stored[Guilds.welcomeMessage],
                    stored[Guilds.leaveMessage],
                    stored[Guilds.ignoredChannels].toList(),
                    stored[Guilds.levelMessages],
                    stored[Guilds.mutedRole],
                    stored[Guilds.antiInvite]
            )
        }
    }.execute()

    fun getUserSafe(member: Member) = getUserSafe(member.user)

    fun getUserSafe(user: User): CompletableFuture<DBUser> = asyncTransaction(pool) {
        val stored = Users.select { Users.id.eq(user.idLong) }.firstOrNull()

        if (stored == null) {
            Users.insert {
                it[id] = user.idLong
                it[username] = user.name
                it[discriminator] = user.discriminator
                it[lang] = "en_US"
                it[marriedUserId] = null
            }

            DBUser(
                    user.idLong,
                    user.name,
                    user.discriminator,
                    "en_US",
                    null
            )
        } else {
            DBUser(
                    stored[Users.id],
                    stored[Users.username],
                    stored[Users.discriminator],
                    stored[Users.lang],
                    stored[Users.marriedUserId]
            )
        }
    }.execute()

    fun logEvent(event: GenericMessageEvent) = asyncTransaction(pool) {
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
            is MessageReceivedEvent -> Uni.LOGGER.debug { }
            is MessageUpdateEvent -> Uni.LOGGER.debug { "UPDATE" }
            else -> throw Exception("Not a valid event to log")
        }
    }.execute().exceptionally {
        logger.error("Error while trying to insert message in db with ID ${event.messageId}", it)
        Sentry.capture(it)
    }
}