package com.github.cf.discord.uni.Handler

import com.github.cf.discord.uni.Uni
import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.utils.*
import com.github.cf.discord.uni.entities.*
import com.github.cf.discord.uni.extensions.*
import com.github.cf.discord.uni.async.asyncTransaction
import com.github.cf.discord.uni.core.EnvVars
import gnu.trove.map.hash.TLongObjectHashMap
import io.sentry.Sentry
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.UserBuilder
import com.github.cf.discord.uni.data.botOwners
import com.github.cf.discord.uni.db.*
import com.github.cf.discord.uni.db.schema.*
import com.github.cf.discord.uni.extensions.UTF8Control
import com.github.cf.discord.uni.listeners.CommandListener
import com.github.cf.discord.uni.utils.ArgParser
import com.github.cf.discord.uni.utils.I18n
import mu.KotlinLogging
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Channel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.apache.commons.validator.routines.UrlValidator
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.reflect.jvm.jvmName

class CommandHandler {
    private val logger = KotlinLogging.logger(this::class.jvmName)
    private val aliases = mutableMapOf<String, String>()
    private val cooldowns = TLongObjectHashMap<OffsetDateTime>()

    val commands = mutableMapOf<String, Command>()

    init { loadAll() }

    private fun loadAll() {
        Reflections(ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com.github.cf.discord.uni.commands")))
                .getSubTypesOf(Command::class.java)
                .forEach {
                    if (!it.isInterface) {
                        val ann = it.annotations.filterIsInstance<Load>()
                        val aliases = it.annotations.filterIsInstance<Alias>()
                        if (ann.isNotEmpty() && ann.first().bool) {
                            val cmd = it.newInstance() as Command
                            val name = (if (cmd.name.isEmpty()) cmd::class.simpleName ?: return else cmd.name).toLowerCase()
                            commands[name] = cmd

                            logger.info("Loaded command $name")
                            if (aliases.isNotEmpty()) {
                                for (alias in aliases.first().aliases) {
                                    logger.info("Added alias for command $name: $alias")
                                    this.aliases[alias] = name
                                }
                            }
                        }
                    }
                }
    }

    fun handleMessage(event: MessageReceivedEvent, user: DBUser, guild: DBGuild? = null) {
        val locale = if (guild != null && guild.forceLang) {
            Locale(guild.lang.split("_")[0], guild.lang.split("_")[1])
        } else {
            Locale(user.lang.split("_")[0], user.lang.split("_")[1])
        }

        val guildPrefixes = guild?.prefixes ?: listOf()

        val lang = ResourceBundle.getBundle("i18n.Kyubey", locale, UTF8Control())

        val usedPrefix = EnvVars.PREFIXES.firstOrNull {
            event.message.contentRaw.startsWith(it.toLowerCase())
        } ?: guildPrefixes.lastOrNull {
            event.message.contentRaw.startsWith(it.toLowerCase())
        } ?: return

        var cmd = event.message.contentRaw.substring(usedPrefix.length).split(" ")[0]
        var args = event.message.contentRaw.substring(usedPrefix.length).split(" ")

        if (args.isNotEmpty()) {
            args = args.drop(1)
        }

        val newPerms: MutableMap<String, Boolean>

        if (!commands.contains(cmd)) {
            if (aliases.contains(cmd)) {
                cmd = aliases[cmd] as String
            } else {
                return
            }
        }

        if (event.guild != null) {
            if (guild!!.ignoredChannels.contains(event.channel.idLong) && cmd != "unignore") {
                return
            }

            val restricted = asyncTransaction(Uni.pool) {
                val restrictions = Restrictions.select {
                    Restrictions.guildId.eq(event.guild!!.idLong) and Restrictions.userId.eq(event.author.idLong)
                }

                restrictions.any {
                    it[Restrictions.command] == "all" || it[Restrictions.command] == cmd
                } || Restrictions.select {
                    Restrictions.guildId.eq(event.guild.idLong) and Restrictions.everyone.eq(true)
                }.toList().isNotEmpty()
            }.execute().get()

            if (restricted && cmd != "unrestrict") {
                return
            }
        }

        val restricted = asyncTransaction(Uni.pool) {
            val restrictions = Restrictions.select {
                Restrictions.userId.eq(event.author.idLong) and Restrictions.global.eq(true)
            }

            restrictions.any {
                it[Restrictions.command] == "all" || it[Restrictions.command] == cmd
            }
        }.execute().get()

        if (restricted && cmd != "unrestrict") {
            return
        }

        Uni.LOGGER.debug { event }

        Sentry.getContext().apply {
            recordBreadcrumb(
                    BreadcrumbBuilder().apply {
                        setMessage("Command executed")
                        setData(mapOf(
                                "command" to cmd
                        ))
                    }.build()
            )
            setUser(
                    UserBuilder().apply {
                        setUsername(event.author.name)
                        setData(mapOf(
                                Pair("guildId", event.guild?.id ?: ""),
                                Pair("userId", event.author.id),
                                Pair("channelId", event.channel.id)
                        ))
                    }.build()
            )
        }

        var command = commands[cmd] as Command

        if (command.ownerOnly && event.author.id !in botOwners.authors)
            return

        if (command.guildOnly && event.guild == null) {
            return event.channel.sendMessage(
                    I18n.parse(
                            lang.getString("server_only_command"),
                            mapOf("username" to event.author.name)
                    )
            ).queue()
        }

        if (command.nsfw && !event.textChannel.isNSFW) {
            return event.channel.sendMessage(
                    I18n.parse(
                            lang.getString("nsfw_only_command"),
                            mapOf("username" to event.author.name)
                    )
            ).queue()
        }

        if (event.author.id in botOwners.authors) {
            val lastMsg = cooldowns[event.author.idLong]

            if (lastMsg != null && lastMsg.until(event.message.creationTime, ChronoUnit.SECONDS) < command.cooldown) {
                return
            }

            cooldowns.put(event.author.idLong, event.message.creationTime)
        }

        if (args.isNotEmpty() && commands[cmd]?.subcommands?.get(args[0]) is Command) {
            val subcmd = args[0]
            command = commands[cmd]?.subcommands?.get(subcmd) as Command
            args = args.drop(1)

            val raw = args

            val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

            args = flags.unmatched

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help"))) {
                return event.channel.sendMessage(help(command)).queue()
            }

            try {
                newPerms = checkPermissions(event, command, lang)
                checkArguments(event, command, args, lang).thenAccept { arg ->
                    try {
                        command.run(Context(event, command, arg, raw, flags, newPerms, lang, user, guild))
                    } catch (e: Exception) {
                        event.channel.sendMessage(
                                I18n.parse(
                                        lang.getString("error"),
                                        mapOf("error" to "$e")
                                )
                        ).queue()

                        val name = "${event.author.name}#${event.author.discriminator}"
                        val where = if (event.guild != null) {
                            "guild ${event.guild.name} (${event.guild.id}) with channel ${event.channel.name} (${event.channel.id}"
                        } else {
                            "DMs"
                        }

                        logger.error("Error while handling command $cmd, executed by user $name (${event.author.id} in $where", e)

                        Sentry.capture(e)
                    }
                }.thenApply {}.exceptionally {
                    event.channel.sendMessage(it.message).queue({}) { err ->
                        logger.error("Error while trying to send error", err)
                        Sentry.capture(err)
                    }
                }
            } catch (err: Exception) {
                return event.channel.sendMessage(err.message).queue()
            }
        } else {
            val raw = args

            val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

            args = flags.unmatched

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help"))) {
                return event.channel.sendMessage(help(cmd)).queue()
            }

            try {
                newPerms = checkPermissions(event, commands[cmd] as Command, lang)
                checkArguments(event, commands[cmd] as Command, args, lang).thenAccept { arg ->
                    try {
                        command.run(Context(event, command, arg, raw, flags, newPerms, lang, user, guild))
                    } catch (e: Exception) {
                        event.channel.sendMessage(
                                I18n.parse(
                                        lang.getString("error"),
                                        mapOf("error" to "$e")
                                )
                        ).queue()

                        val name = "${event.author.name}#${event.author.discriminator}"
                        val where = if (event.guild != null) {
                            "guild ${event.guild.name} (${event.guild.id}) with channel ${event.channel.name} (${event.channel.id}"
                        } else {
                            "DMs"
                        }

                        logger.error("Error while handling command $cmd, executed by user $name (${event.author.id} in $where", e)

                        Sentry.capture(e)
                    }
                }
            } catch (err: Exception) {
                return event.channel.sendMessage(err.message).queue()
            }
        }
    }

    private fun checkPermissions(event: MessageReceivedEvent, cmd: Command, lang: ResourceBundle): MutableMap<String, Boolean> {
        val perms = cmd::class.annotations.filterIsInstance(Perm::class.java).toMutableList()
        val otherPerms = cmd::class.annotations.filterIsInstance(Perms::class.java)

        if (otherPerms.isNotEmpty())
            perms.addAll(otherPerms.first().perms)

        val newPerms = mutableMapOf<String, Boolean>()

        for (perm in perms) {
            newPerms[perm.name.name] = event.member?.hasPermission(event.channel as Channel, perm.name)
                    ?: event.member?.hasPermission(Permission.ADMINISTRATOR) ?: false
            if (!perm.optional && !newPerms[perm.name.name]!!
                    && !event.member?.hasPermission(Permission.ADMINISTRATOR)!!
                    && event.member.user.id !in botOwners.authors)
                throw Exception(
                        I18n.parse(
                                lang.getString("user_lack_perms"),
                                mapOf(
                                        "username" to event.author.name,
                                        "permission" to I18n.permission(lang, perm.name.name)
                                )
                        )
                )
        }

        return newPerms
    }

    private fun checkArguments(
            event: MessageReceivedEvent,
            cmd: Command,
            args: List<String>,
            lang: ResourceBundle
    ): CompletableFuture<MutableMap<String, Any>> {
        val newArgs = mutableMapOf<String, Any>()
        val fut = CompletableFuture<MutableMap<String, Any>>()

        val cmdArgs = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val other = cmd::class.annotations.filterIsInstance(Arguments::class.java)

        if (other.isNotEmpty())
            cmdArgs += other.first().args

        var i = 0

        fun next() {
            if (i == cmdArgs.size)  {
                fut.complete(newArgs)
                return
            }

            val arg = cmdArgs[i]
            var arg2: String?
            try {
                arg2 = args[i]
            } catch(e: Exception) {
                if (!arg.optional)
                    throw Exception(
                            I18n.parse(
                                    lang.getString("argument_not_specified"),
                                    mapOf(
                                            "argument" to arg.name,
                                            "username" to event.author.name
                                    )
                            )
                    )
                else {
                    fut.complete(newArgs)
                    return
                }
            }

            if (cmdArgs.last() == arg)
                arg2 = args.slice(cmdArgs.indexOf(arg) until args.size).joinToString(" ")

            when (arg.type) {
                "textchannel" -> {
                    if (event.guild != null) {
                        val channels = event.guild.searchTextChannels(arg2)

                        if (channels.isEmpty())
                            throw Exception(
                                    I18n.parse(
                                            lang.getString("channel_not_found"),
                                            mapOf(
                                                    "username" to event.author.name
                                            )
                                    )
                            )

                        if (channels.size > 1) {
                                val picker = TextChannelPicker(
                                    CommandListener.waiter,
                                    event.member,
                                    channels,
                                    event.guild
                            )

                            picker.build(event.message).thenAccept {
                                newArgs[arg.name] = it
                                i++
                                next()
                            }
                        } else {
                            newArgs[arg.name] = channels[0]
                            i++
                            next()
                        }
                    }
                }

                "user" -> {
                    if (event.guild != null) {
                        val users = event.guild.searchMembers(arg2)

                        if (users.isEmpty())
                            throw Exception(
                                    I18n.parse(
                                            lang.getString("user_not_found"),
                                            mapOf(
                                                    "username" to event.author.name
                                            )
                                    )
                            )

                        if (users.size > 1) {
                            val picker = UserPicker(CommandListener.waiter, event.member, users, event.guild)

                            picker.build(event.message).thenAccept {
                                newArgs[arg.name] = it
                                i++
                                next()
                            }
                        } else {
                            newArgs[arg.name] = users[0]
                            i++
                            next()
                        }
                    }
                }

                "role" -> {
                    if (event.guild != null) {
                        val roles = event.guild.searchRoles(arg2)

                        if (roles.isEmpty())
                            throw Exception(
                                    I18n.parse(
                                            lang.getString("role_not_found"),
                                            mapOf(
                                                    "username" to event.author.name
                                            )
                                    )
                            )

                        if (roles.size > 1) {
                            val picker = RolePicker(CommandListener.waiter, event.member, roles, event.guild)

                            picker.build(event.message).thenAccept {
                                newArgs[arg.name] = it
                                i++
                                next()
                            }
                        } else {
                            newArgs[arg.name] = roles[0]
                            i++
                            next()
                        }
                    }
                }

                "url" -> {
                    val validator = UrlValidator()

                    if (!validator.isValid(arg2))
                        throw Exception(
                                I18n.parse(
                                        lang.getString("invalid_argument_type"),
                                        mapOf(
                                                "username" to event.author.name,
                                                "type" to "url",
                                                "given_type" to "string"
                                        )
                                )
                        )

                    newArgs[arg.name] = arg2
                    i++
                    next()
                }

                "number" -> {
                    newArgs[arg.name] = arg2.toIntOrNull()
                            ?: throw Exception(
                            I18n.parse(
                                    lang.getString("invalid_argument_type"),
                                    mapOf(
                                            "username" to event.author.name,
                                            "type" to "number",
                                            "given_type" to "string"
                                    )
                            )
                    )
                    i++
                    next()
                }

                else -> {
                    newArgs[arg.name] = arg2
                    i++
                    next()
                }
            }
        }

        next()

        return fut
    }

    fun help(cmdd: String): String {
        if (!commands.contains(cmdd))
            throw Exception("No such command: $cmdd")

        val cmd = commands[cmdd] as Command
        val args = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val otherArgs = cmd::class.annotations.filterIsInstance(Arguments::class.java)

        if (otherArgs.isNotEmpty())
            args += otherArgs.first().args

        val flags = cmd::class.annotations.filterIsInstance(Flag::class.java).toMutableList()
        val otherFlags = cmd::class.annotations.filterIsInstance(Flags::class.java)

        if (otherFlags.isNotEmpty())
            flags += otherFlags.first().flags

        val sub = cmd.subcommands.map {
            "\t${it.key}" + " ".repeat(20 - it.key.length) + it.value.desc.split("\n")[0]
        }
        val flag = flags.map {
            "\t-${it.abbr}, --${it.flag}${" ".repeat(20 - "-${it.abbr}, --${it.flag}".length)}${it.desc}\n"
        }
        val usage = args.map {
            if (it.optional)
                "[${it.name}: ${it.type}]"
            else
                "<${it.name}: ${it.type}>"
        }
        val formattedSubs = if (sub.isNotEmpty()) "\nSubcommands:\n${sub.joinToString("\n")}" else ""
        val formattedFlags = if (flag.isNotEmpty()) flag.joinToString("\n") else ""
        return "```\n" +
                "$cmdd ${usage.joinToString(" ")}\n" +
                "\n" +
                "${cmd.desc}\n" +
                "$formattedSubs\n" +
                "Flags:\n" +
                "\t-h, --help\n" +
                "$formattedFlags```"
    }
    fun help(cmd: Command): String {
        val args = cmd::class.annotations.filterIsInstance(Argument::class.java).toMutableList()
        val otherArgs = cmd::class.annotations.filterIsInstance(Arguments::class.java)
        if (otherArgs.isNotEmpty())
            args += otherArgs.first().args
        val flags = cmd::class.annotations.filterIsInstance(Flag::class.java).toMutableList()
        val otherFlags = cmd::class.annotations.filterIsInstance(Flags::class.java)
        if (otherFlags.isNotEmpty())
            flags += otherFlags.first().flags
        val sub = cmd.subcommands.map {
            "\t${it.key}" + " ".repeat(20 - it.key.length) + it.value.desc.split("\n")[0]
        }
        val flag = flags.map {
            "\t-${it.abbr}, --${it.flag}${" ".repeat(20 - "-${it.abbr}, --${it.flag}".length)}${it.desc}\n"
        }
        val usage = args.map {
            if (it.optional)
                "[${it.name}: ${it.type}]"
            else
                "<${it.name}: ${it.type}>"
        }
        val formattedSubs = if (sub.isNotEmpty()) "\nSubcommands:\n${sub.joinToString("\n")}\n" else ""
        val formattedFlags = if (flag.isNotEmpty()) flag.joinToString("\n") else ""
        val name = (if (cmd.name.isEmpty()) cmd::class.simpleName!! else cmd.name).toLowerCase()
        return "```\n" +
                "$name ${usage.joinToString(" ")}\n" +
                "\n" +
                "${cmd.desc}\n" +
                "$formattedSubs\n" +
                "Flags:\n" +
                "\t-h, --help\n" +
                "$formattedFlags```"
    }
}