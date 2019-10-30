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
package com.github.cf.discord.uni

import com.github.cf.discord.uni.Handler.ArgParser
import com.github.cf.discord.uni.Uni.Companion.LOGGER
import com.github.cf.discord.uni.annotations.*
import com.github.cf.discord.uni.data.botOwners
import com.github.cf.discord.uni.database.DBGuild
import com.github.cf.discord.uni.database.DBUser
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.findMembers
import com.github.cf.discord.uni.extensions.findRoles
import com.github.cf.discord.uni.extensions.findTextChannels
import com.github.cf.discord.uni.listeners.EventListener.Companion.waiter
import com.github.cf.discord.uni.utils.Logger
import com.github.cf.discord.uni.utils.RolePicker
import com.github.cf.discord.uni.utils.TextChannelPicker
import com.github.cf.discord.uni.utils.UserPicker
import com.github.jasync.sql.db.util.length
import gnu.trove.map.hash.TLongObjectHashMap
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.reflect.jvm.jvmName
import java.util.concurrent.CompletableFuture
import org.apache.commons.validator.UrlValidator
import org.reflections8.Reflections
import org.reflections8.util.ClasspathHelper
import org.reflections8.util.ConfigurationBuilder
import java.util.*

class CommandHandler{
    private val logger = Logger(this::class.jvmName)
    private val aliases = mutableMapOf<String, String>()
    private val cooldown = TLongObjectHashMap<OffsetDateTime>()

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
                            val name = (if (cmd.name.isEmpty()) cmd::class.simpleName
                                    ?: return else cmd.name).toLowerCase()
                            commands[name] = cmd

                            LOGGER.info("Loaded command $name")
                            if (aliases.isNotEmpty()) {
                                for (alias in aliases.first().aliases) {
                                    LOGGER.info("Added alias for command $name: $alias")
                                    this.aliases[alias] = name
                                }
                            }
                        }
                    }
                }
    }
    fun handleMessage(event: MessageReceivedEvent, user: DBUser, guild: DBGuild? = null){
        val guildPrefix = String(Base64.getDecoder().decode(guild?.prefix ?: "")).toLowerCase()

        val userPrefix = String(Base64.getDecoder().decode(user.customPrefix ?: "")).toLowerCase()

        val customPrefix = "<@${event.jda.selfUser.idLong}> "

        fun checkPrefix(prefix: String?, message: Message): String? {
            if(prefix !is String) return null
            if(prefix.isEmpty()) return null
            return if (message.contentRaw.toLowerCase().startsWith(prefix)) prefix else null
        }

        val usedPrefix = Uni.prefixes.firstOrNull {
            event.message.contentRaw.toLowerCase().startsWith(it.toLowerCase())
        } ?: checkPrefix(guildPrefix.toLowerCase(), event.message)
        ?: checkPrefix(userPrefix.toLowerCase(), event.message)
        ?: checkPrefix(customPrefix.toLowerCase(), event.message)

        val allPrefixes = usedPrefix!!.length

        var cmd = event.message.contentRaw.substring(allPrefixes).split(" ")[0]
        var args = event.message.contentRaw.substring(allPrefixes).split(" ")

        if(args.isNotEmpty()){
            args = args.drop(1)
        }

        val newPerms: MutableMap<String, Boolean>

        if(!commands.contains(cmd)){
            if(aliases.contains(cmd)){
                cmd = aliases[cmd] as String
            } else {
                return
            }
        }
        logger.command(event)

        var command = commands[cmd] as Command

        if(command.ownerOnly && event.author.id !in botOwners.authors) {
            val embed = EmbedBuilder()
                    .setAuthor("Uni", null, "https://cdn.discordapp.com/avatars/396801832711880715/1d51997b035d1fa5d8441b73de87c748.png")
                    .setTitle("Please don't do this command")
                    .setDescription("doing this command makes me angry please don't do it again <a:Goodbye:565638414968422420>")
                    .build()
            return event.message.channel.sendMessage(embed).queue()
        }

        if(command.guildOnly && event.guild == null){
            return event.message.channel.sendMessage("this command can be only used in a guild").queue()
        }

        if (command.nsfw && !event.textChannel.isNSFW){
            return event.message.channel.sendMessage("this command can only be used in a nsfw channel").queue()
        }

        if(event.author.id !in botOwners.authors){
            val lastMsg = cooldown[event.author.idLong]

            if (lastMsg != null && lastMsg.until(event.message.timeCreated, ChronoUnit.SECONDS) < command.cooldown){
                return
            }

            cooldown.put(event.author.idLong, event.message.timeCreated)
        }

        if(args.isNotEmpty() && commands[cmd]?.subcommands?.get(args[0]) is Command){
            val subcmd = args[0]
            command = commands[cmd]?.subcommands?.get(subcmd) as Command
            args = args.drop(0)

            val raw = args

            val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

            args = flags.unmatched

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help"))){
                return event.channel.sendMessage(help(command)).queue()
            }

            try{
                newPerms = checkPermissions(event, command)
                checkArguments(event, command, args).thenAccept { arg ->
                    try {
                        command.run(Context(event, command, arg, raw, flags, newPerms, user, guild))
                    } catch (e: Exception) {
                        event.channel.sendMessage("error: $e").queue()

                        val name = "${event.author.name}#${event.author.discriminator}"
                        val where = if (event.guild != null) {
                            "guild ${event.guild.name} (${event.guild.id}) with channel ${event.channel.name} (${event.channel.id})"
                        } else {
                            "DMs"
                        }

                        logger.error("Error while handling command $cmd, executed by user $name (${event.author.id} in $where", e)
                    }
                }.thenApply {  }.exceptionally {
                    event.channel.sendMessage(it.message!!).queue({}) { err ->
                        logger.error("Error while trying to send error", err)
                    }
                }
            } catch (err: Exception) {
                return event.channel.sendMessage(err.message!!).queue()
            }
        } else {
            val raw = args

            val flags = ArgParser.untypedParseSplit(ArgParser.tokenize(args.joinToString(" ")))

            args = flags.unmatched

            if (!command.noHelp && (flags.argMap.contains("h") || flags.argMap.contains("help"))) {
                return event.channel.sendMessage(help(cmd)).queue()
            }

            try {
                newPerms = checkPermissions(event, commands[cmd] as Command)
                checkArguments(event, commands[cmd] as Command, args).thenAccept { arg ->
                    try {
                        command.run(Context(event, command, arg, raw, flags, newPerms, user, guild))
                    } catch (e: Exception) {
                        event.channel.sendMessage("error $e").queue()

                        val name = "${event.author.name}#${event.author.discriminator}"
                        val where = if (event.guild != null) {
                            "guild ${event.guild.name} (${event.guild.id}) with channel ${event.channel.name} (${event.channel.id}"
                        } else {
                            "DMs"
                        }

                        logger.error("Error while handling command $cmd, executed by user $name (${event.author.id} in $where", e)
                    }
                }
            } catch (err: Exception) {
                return event.channel.sendMessage(err.message!!).queue()
            }
        }
    }
    private fun checkPermissions(event: MessageReceivedEvent, cmd: Command): MutableMap<String, Boolean> {
        val perms = cmd::class.annotations.filterIsInstance(Perm::class.java).toMutableList()
        val otherPerms = cmd::class.annotations.filterIsInstance(Perms::class.java)

        if (otherPerms.isNotEmpty())
            perms.addAll(otherPerms.first().perms)

        val newPerms = mutableMapOf<String, Boolean>()

        for (perm in perms) {
            newPerms[perm.name.name] = event.member?.hasPermission(event.channel as TextChannel, perm.name)
                    ?: event.member?.hasPermission(Permission.ADMINISTRATOR) ?: false
            if (!perm.optional && !newPerms[perm.name.name]!!
                    && !event.member?.hasPermission(Permission.ADMINISTRATOR)!!
                    && event.member!!.user.id !in botOwners.authors)
                throw Exception("does not have proper permissions for this command")
        }

        return newPerms
    }

    private fun checkArguments(
            event: MessageReceivedEvent,
            cmd: Command,
            args: List<String>
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
                    throw Exception("argument not specified")
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
                        val channels = event.guild.findTextChannels(arg2)

                        if (channels.isEmpty())
                            throw Exception("channel not found")

                        if (channels.size > 1) {
                            val picker = TextChannelPicker(
                                    waiter,
                                    event.member!!,
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
                        val users = event.guild.findMembers(arg2)

                        if (users.isEmpty())
                            throw Exception("user not found")

                        if (users.size > 1) {
                            val picker = UserPicker(waiter, event.member!!, users, event.guild)

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
                        val roles = event.guild.findRoles(arg2)

                        if (roles.isEmpty())
                            throw Exception("role not found")

                        if (roles.size > 1) {
                            val picker = RolePicker(waiter, event.member!!, roles, event.guild)

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
                        throw Exception("invalid argument type")

                    newArgs[arg.name] = arg2
                    i++
                    next()
                }

                "number" -> {
                    newArgs[arg.name] = arg2.toIntOrNull()
                            ?: throw Exception("invalid argument type")
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
