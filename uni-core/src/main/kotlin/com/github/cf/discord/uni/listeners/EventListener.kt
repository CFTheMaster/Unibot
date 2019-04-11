package com.github.cf.discord.uni.listeners

import com.github.cf.discord.uni.CommandHandler
import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.database.DatabaseWrapper
import com.github.cf.discord.uni.database.schema.*
import com.github.cf.discord.uni.extensions.addStar
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.extensions.log
import com.github.cf.discord.uni.extensions.removeStar
import com.github.cf.discord.uni.stateful.EventWaiter
import com.github.cf.discord.uni.utils.Http
import com.github.jasync.sql.db.util.length
import gnu.trove.map.hash.TLongLongHashMap
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.audit.ActionType
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.guild.GuildBanEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.MessageUpdateEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import org.json.JSONObject
import java.awt.Color
import java.lang.Exception
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Random
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

class EventListener : ListenerAdapter(){
    override fun onGenericEvent(event: Event) = waiter.emit(event)

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
            DatabaseWrapper.getGuildSafe(event.guild).thenAccept{ stored ->
                if(stored.logs){
                    event.message.log()
                }

                if(event.author.isBot){
                    return@thenAccept
                }

                DatabaseWrapper.getUserSafe(event.member).thenAccept {user ->
                    try {
                        cmdHandler.handleMessage(event, user, stored)
                    } catch (e: Exception){
                        LOGGER.error("Error while trying to handkle message: $e") // dit gaat alleen af als de commandhandler crasht
                    }
                }// wilde kijke als het fout gaat in DB, je moet alleen ff in de DB kijken of er een goeie date bij staat

                if(stored.antiInvite){
                    val regex = "(https?)?:?(//)?discord(app)?.?(gg|io|me|com)?/(\\w+:?\\w*@)?(\\S+)(:[0-9]+)?(/|/([\\w#!:.?+=&%@!-/]))?".toRegex()

                    if(event.member.roles.isEmpty() && regex.containsMatchIn(event.message.contentRaw)){
                        event.message.delete().queue ({
                            event.channel.sendMessage("please do not post any ads").queue()
                        })
                        {
                            event.channel.sendMessage("error while trying to delete message $it").queue()
                            LOGGER.error("error while trying to remove ad", it)
                        }
                    }
                }
                asyncTransaction(Uni.pool) {
                    val exists = Users.select{ Users.id.eq(event.author.idLong)}.firstOrNull() ?: return@asyncTransaction
                    val curLevel = exists[Users.level]
                    val xp = exists[Users.expPoints]
                    val lastMsg = exists[Users.lastMessage]

                    val niceMeme = DateTime.now().millis

                    if(DateTime.now().millis - niceMeme > TimeUnit.MINUTES.toMillis(10) && event.message.idLong > lastMsg){
                        val xpGet = xp+1
                        Users.update({Users.id.eq(event.author.idLong)}) {
                            it[expPoints] = xpGet
                            it[lastMessage] = event.message.idLong
                        }

                    }


                    val xpNeeded = curLevel.toFloat() * 500f * (curLevel.toFloat() / 3f) + (xp * (xp + curLevel + xp/3))

                    if (xp >= xpNeeded) {
                        Users.update({
                            Users.id.eq(event.author.idLong)
                        }) {
                            it[level] = curLevel + 1
                            it[expPoints] = xp
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

        Uni.jda!!.getGuildById(138303776170835969).getTextChannelById(440833941335703572).sendMessage(EmbedBuilder()
                .setAuthor("Joined guild", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setThumbnail("${if (event.guild.iconUrl != null) event.guild.iconUrl else "https://maxcdn.icons8.com/Share/icon/Logos/discord_logo1600.png"}")
                .setColor(java.lang.Integer.parseInt("#6600cc".replaceFirst("#", ""), 16))
                .addField("Joined Guild: ", "${event.guild.name}", true)
                .addField("Server/Guild ID: ", "${event.guild.idLong}", true)
                .addField("Server/Guild Owner: ", "${event.guild.owner.user.name}", true)
                .addField("Server/Guild Owner ID: ", "${event.guild.owner.user.idLong}", true)
                .addField("Creation Date: ", "${event.guild.creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                .addField("Guild Members: ", "${event.guild.members.size}", true)
                .addField("Bots: ", "${event.guild.members.filter { it.user.isBot }.size}", true)
                .addField("Highest role: ", "${event.guild.roles.get(0).name ?: "none"}\n", true)
                .addField("Text Channels: ", "${event.guild.textChannels.size}", true)
                .addField("Voice Channels: ", " ${event.guild.voiceChannels.size} ", true)
                .addField("Total amount of guilds: ", "${event.jda.guilds.size}", true)
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

        event.jda.getGuildById(138303776170835969).getTextChannelById(440833941335703572).sendMessage(EmbedBuilder()
                .setAuthor("Left guild", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                .setThumbnail("${if(event.guild.iconUrl != null) event.guild.iconUrl else "https://maxcdn.icons8.com/Share/icon/Logos/discord_logo1600.png"}")
                .setColor(java.lang.Integer.parseInt("#6600cc".replaceFirst("#", ""), 16))
                .addField("Left Guild: ", "${event.guild.name}", true)
                .addField("Server/Guild ID: ", "${event.guild.idLong}", true)
                .addField("Server/Guild Owner: ", "${event.guild.owner.user.name}", true)
                .addField("Server/Guild Owner ID: ", "${event.guild.owner.user.idLong}", true)
                .addField("Creation Date: ", "${event.guild.creationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}", true)
                .addField("Guild Members: ", "${event.guild.members.size}", true)
                .addField("Bots: ","${event.guild.members.filter { it.user.isBot }.size}", true)
                .addField("Highest role: ", "${event.guild.roles.get(0).name ?: "none"}\n", true)
                .addField("Text Channels: ", "${event.guild.textChannels.size}", true)
                .addField("Voice Channels: ", " ${event.guild.voiceChannels.size} ", true)
                .addField("Total amount of guilds: ", "${event.jda.guilds.size}", true)
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

                event.channel.getMessageById(event.messageId).queue { msg ->
                    event.guild.addStar(msg, event.user)
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

                event.channel.getMessageById(event.messageId).queue { msg ->
                    event.guild.removeStar(msg, event.user)
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
                    event.guild.controller.addSingleRoleToMember(event.member, r).queue()
                }
            }
        }.execute().thenApply {}.exceptionally {
            it.printStackTrace()
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
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        DatabaseWrapper.getGuildSafe(event.guild).thenAccept { storedGuild ->
            asyncTransaction(Uni.pool) {
                if (!storedGuild.modlogs) {
                    return@asyncTransaction
                }

                val modlogs = ModLogs.select { ModLogs.guildId.eq(event.guild.idLong) }
                val modlogChannel = event.guild.getTextChannelById(storedGuild.modlogChannel ?: return@asyncTransaction) ?: return@asyncTransaction
                val audit = event.guild.auditLogs.type(ActionType.KICK).limit(2).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction
                val case = modlogs.count() + 1

                val msg = modlogChannel.sendMessage("""
                **Kick** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

                ModLogs.insert {
                    it[messageId] = msg.idLong
                    it[modId] = audit.user.idLong
                    it[guildId] = event.guild.idLong
                    it[targetId] = audit.targetIdLong
                    it[caseId] = case
                    it[type] = "KICK"
                    it[reason] = audit.reason
                }
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
                val audit = event.guild.auditLogs.type(ActionType.UNBAN).first { it.targetId == event.user.id }
                val case = modlogs.count() + 1

                val msg = modlogChannel.sendMessage("""
                **Unban** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

                ModLogs.insert {
                    it[messageId] = msg.idLong
                    it[modId] = audit.user.idLong
                    it[guildId] = event.guild.idLong
                    it[targetId] = audit.targetIdLong
                    it[caseId] = case
                    it[type] = "UNBAN"
                    it[reason] = audit.reason
                }
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
                val audit = event.guild.auditLogs.type(ActionType.BAN).first { it.targetId == event.user.id }
                val case = modlogs.count() + 1

                val msg = modlogChannel.sendMessage("""
                **Ban** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

                ModLogs.insert {
                    it[messageId] = msg.idLong
                    it[modId] = audit.user.idLong
                    it[guildId] = event.guild.idLong
                    it[targetId] = audit.targetIdLong
                    it[caseId] = case
                    it[type] = "BAN"
                    it[reason] = audit.reason
                }
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
                val audit = event.guild.auditLogs.type(ActionType.MEMBER_ROLE_UPDATE).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction

                Roles.insert {
                    it[userId] = event.user.idLong
                    it[guildId] = event.guild.idLong
                    it[roleId] = event.roles.first().idLong
                }

                val case = modlogs.count() + 1

                val msg = modlogChannel.sendMessage("""
                **Mute** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

                ModLogs.insert {
                    it[messageId] = msg.idLong
                    it[modId] = audit.user.idLong
                    it[guildId] = event.guild.idLong
                    it[targetId] = audit.targetIdLong
                    it[caseId] = case
                    it[type] = "MUTE"
                    it[reason] = audit.reason
                }
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
                val audit = event.guild.auditLogs.type(ActionType.MEMBER_ROLE_UPDATE).firstOrNull { it.targetId == event.user.id } ?: return@asyncTransaction

                Roles.deleteWhere {
                    Roles.guildId.eq(event.guild.idLong) and Roles.userId.eq(event.user.idLong) and Roles.roleId.eq(event.roles.first().idLong)
                }

                val case = modlogs.count() + 1

                val msg = modlogChannel.sendMessage("""
                **Unmute** | Case $case
                **User**: ${event.user.name}#${event.user.discriminator} (${event.user.id})
                **Reason**: ${audit.reason ?: "`Responsible moderator, please use the reason command to set this reason`"}
                **Responsible moderator**: ${audit.user.name}#${audit.user.discriminator} (${audit.user.id})
            """.trimIndent()).complete()

                ModLogs.insert {
                    it[messageId] = msg.idLong
                    it[modId] = audit.user.idLong
                    it[guildId] = event.guild.idLong
                    it[targetId] = audit.targetIdLong
                    it[caseId] = case
                    it[type] = "UNMUTE"
                    it[reason] = audit.reason
                }
            }.execute()
        }
    }

    private fun startPresenceTimer() {
        fixedRateTimer("change status", false, 0L, TimeUnit.MINUTES.toMillis(10)) {
            val text = arrayOf(
                    "with computerfreaker \uD83C\uDF38",
                    "with guns \uD83C\uDF38",
                    "is this thing on? \uD83D\uDC40",
                    "doing nothing...")
            val idx = Random().nextInt(text.size)
            val random = text[idx]
            val prefix = Uni.prefixes.firstOrNull()
            Uni.jda!!.presence.setPresence(OnlineStatus.ONLINE, Game.of(Game.GameType.STREAMING, "$random | ${prefix}help", "https://www.twitch.tv/computerfreaker"))
        }
    }

    companion object {
        val waiter = EventWaiter()

        fun updateStats(){
            val jsonType = MediaType.parse("application/json")
            if (Uni.jda != null){
                val json = mapOf("server_count" to Uni.jda!!.guilds.size)
                val body = RequestBody.create(jsonType, JSONObject(json).toString())

                if(EnvVars.DBL_TOKEN!!.isNotEmpty()){
                    Http.post("https://discordbots.org/api/bots/${Uni.jda!!.selfUser.id}/stats", body){
                        addHeader("Authorization", EnvVars.DBL_TOKEN)
                    }.thenAccept{
                        LOGGER.info("updated stats for DBL")
                        it.close()
                    }.thenApply {}.exceptionally {
                        LOGGER.error("Error while updating stats", it)
                    }
                }
            }
        }

        val snipes = TLongLongHashMap()
        val cmdHandler = CommandHandler()
    }
}