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
import com.github.cf.discord.uni.annotations.Alias
import com.github.cf.discord.uni.annotations.Argument
import com.github.cf.discord.uni.annotations.Arguments
import com.github.cf.discord.uni.annotations.Load
import com.github.cf.discord.uni.database.schema.Guilds
import com.github.cf.discord.uni.entities.Command
import com.github.cf.discord.uni.entities.Context
import com.github.cf.discord.uni.extensions.asyncTransaction
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.entities.TextChannel
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.update


@Argument("option", "string")
class EnableOption : Command() {
    override val desc = "Enable a setting."
    override val guildOnly = true

    private val options = listOf(
            "starboard",
            "logs",
            "modlogs",
            "welcome",
            "levelMessages",
            "antiInvite",
            "userRole"
    )

    override fun run(ctx: Context) {
        val opt = (ctx.args["option"] as String).toLowerCase().replace("enable ", "")

        val humanOptions = mutableMapOf<String, String>()

        for (option in options) {
            val regex = "([a-zA-Z])".toRegex()
            val letter = regex.find(option)?.groupValues?.get(0)

            if (letter != null)
                humanOptions[option.replace(regex, " ${letter.toLowerCase()}")] = option

            humanOptions[option.toLowerCase()] = option
        }

        if (opt !in humanOptions) {
            return ctx.send("Option not found! $opt") // TODO translation
        }

        asyncTransaction(Uni.pool) {
            Guilds.update({
                Guilds.id.eq(ctx.guild!!.idLong)
            }) {
                val col = columns.first { it.name == humanOptions[opt] } as Column<Boolean>

                it[col] = true
            }
        }.execute()

        ctx.send("Enabled $opt!") // TODO translation
    }
}

@Argument("option", "string")
class DisableOption : Command() {
    override val desc = "Disable a setting."
    override val guildOnly = true

    private val options = listOf(
            "starboard",
            "logs",
            "modlogs",
            "welcome",
            "levelMessages",
            "antiInvite",
            "userRole"
    )

    override fun run(ctx: Context) {
        val opt = (ctx.args["option"] as String).toLowerCase().replace("disable ", "")

        val humanOptions = mutableMapOf<String, String>()

        for (option in options) {
            val regex = "([a-zA-Z])".toRegex()
            val letter = regex.find(option)?.groupValues?.get(0)

            if (letter != null)
                humanOptions[option.replace(regex, " ${letter.toLowerCase()}")] = option

            humanOptions[option.toLowerCase()] = option
        }

        if (opt !in humanOptions) {
            return ctx.send("Option not found!") // TODO translation
        }

        asyncTransaction(Uni.pool) {
            Guilds.update({
                Guilds.id.eq(ctx.guild!!.idLong)
            }) {
                val col = columns.first { it.name == humanOptions[opt] }as Column<Boolean>

                it[col] = false
            }
        }.execute()

        ctx.send("Disabled $opt!") // TODO translation
    }
}

@Arguments(
        Argument("name", "string"),
        Argument("option", "string"),
        Argument("channel", "textchannel")
)
class SetChannelOption : Command() {
    override val desc = "Set an option's channel."
    override val guildOnly = true

    private val options = listOf(
            "starboardChannel",
            "modlogChannel",
            "welcomeChannel"
    )

    override fun run(ctx: Context) {
        val name = (ctx.args["name"] as String).toLowerCase().replace("setchannel ", "")
        val opt = (ctx.args["option"] as String).toLowerCase().replace("${options.isNotEmpty().toString().toLowerCase()} ", "")
        val channel = ctx.args["channel"] as TextChannel

        if (opt !in options.map(String::toLowerCase))
            return ctx.send("Option not found! $name, $opt, $channel") // TODO translation

        asyncTransaction(Uni.pool) {
            Guilds.update({
                Guilds.id.eq(ctx.guild!!.idLong)
            }) {
                val col = columns.first { it.name.toLowerCase() == opt } as Column<Long>

                it[col] = channel.idLong
            }
        }.execute()

        ctx.send("Channel for $opt is now ${channel.asMention}!") // TODO translation
    }
}

@Arguments(
        Argument("name", "string"),
        Argument("option", "string"),
        Argument("role", "role")
)
class SetRoleOption : Command() {
    override val desc = "Set an option's role."
    override val guildOnly = true

    private val options = listOf(
            "mutedRole",
            "autoRole"
    )

    override fun run(ctx: Context) {
        val name = (ctx.args["name"] as String).toLowerCase().replace("setrole ", "")
        val opt = (ctx.args["option"] as String).toLowerCase().replace("${options.isNotEmpty().toString().toLowerCase()} ", "")
        val role = ctx.args["role"] as Role

        if (opt !in options.map(String::toLowerCase)) {
            return ctx.send("Option not found! $name, $opt, $role") // TODO translation
        }

        asyncTransaction(Uni.pool) {
            Guilds.update({
                Guilds.id.eq(ctx.guild!!.idLong)
            }) {
                val col = columns.first { it.name.toLowerCase() == opt } as Column<Long>

                it[col] = role.idLong
            }
        }.execute()

        ctx.send("Role for $opt is now ${role.name}!") // TODO translation
    }
}

@Arguments(
        Argument("name", "string"),
        Argument("option", "string"),
        Argument("string", "string")
)
class SetStringOption : Command() {
    override val desc = "Set an option's text."

    private val options = listOf(
            "welcomeMessage",
            "leaveMessage"
    )

    override fun run(ctx: Context) {
        val name = (ctx.args["name"] as String).toLowerCase().replace("setstring ", "")
        val opt = (ctx.args["option"] as String).toLowerCase().replace("${options.isNotEmpty().toString().toLowerCase()} ", "")
        val string = ctx.args["string"] as String

        if (opt !in options.map(String::toLowerCase)) {
            return ctx.send("Option not found! $name, $opt, $string") // TODO translation
        }

        asyncTransaction(Uni.pool) {
            Guilds.update({
                Guilds.id.eq(ctx.guild!!.idLong)
            }) {
                val col = columns.first { it.name.toLowerCase() == opt } as Column<String>

                it[col] = string
            }
        }.execute()

        ctx.send("$opt is now $string!") // TODO translation
    }
}

@Load
@Alias("cfg", "conf", "settings")
class Config : Command() {
    override val guildOnly = true
    override val desc = "View the current config!"

    init {
        addSubcommand(EnableOption(), "enable")
        addSubcommand(DisableOption(), "disable")
        addSubcommand(SetChannelOption(), "setchannel")
        addSubcommand(SetRoleOption(), "setrole")
        addSubcommand(SetStringOption(), "setstring")
    }

    override fun run(ctx: Context) {
        val embed = EmbedBuilder().apply {
            setTitle("Settings")
            addField(
                    "General",
                    "**prefix:** ${if (ctx.storedGuild!!.prefix!!.isNotEmpty()) ctx.storedGuild.prefix.toString() else "none"}\n" +
                            "**logs:** ${if (ctx.storedGuild.logs) "enabled" else "disabled"}\n" +
                            "**mutedRole:** ${ctx.guild!!.getRoleById(ctx.storedGuild.mutedRole ?: 0L)?.asMention ?: "none"}\n" +
                            "**levelMessages:** ${if (ctx.storedGuild.levelMessages) "enabled" else "disabled"}\n" +
                            "**antiInvite:** ${if (ctx.storedGuild.antiInvite) "enabled" else "disabled"}\n",
                    true
            )
            addField(
                    "Modlogs",
                    "**modlogs:** ${if (ctx.storedGuild.modlogs) "enabled" else "disabled"}\n" +
                            "**modlogChannel:** ${ctx.guild.getTextChannelById(ctx.storedGuild.modlogChannel ?: 0L)?.asMention ?: "none"}",
                    true
            )
            addField(
                    "Starboard",
                    "**starboard:** ${if (ctx.storedGuild.starboard) "enabled" else "disabled"}\n" +
                            "**starboardChannel:** ${ctx.guild.getTextChannelById(ctx.storedGuild.starboardChannel ?: 0L)?.asMention ?: "none"}",
                    true
            )
            addField(
                    "Welcomer",
                    "**welcome:** ${if (ctx.storedGuild.welcome) "enabled" else "disabled"}\n" +
                            "**welcomeChannel:** ${ctx.guild.getTextChannelById(ctx.storedGuild.welcomeChannel ?: 0L)?.asMention ?: "none"}\n" +
                            "**welcomeMessage:** ${ctx.storedGuild.welcomeMessage}\n" +
                            "**leaveMessage:** ${ctx.storedGuild.leaveMessage}",
                    true
            )
            addField(
                    "Auto Role",
                    "**Autorole:** ${if (ctx.storedGuild.userRole) "enabled" else "disabled"}\n" +
                            "**AutoRole:** ${ctx.guild.getRoleById(ctx.storedGuild.autoRole ?: 0L)?.asMention ?: "none"}",
                    true
            )
        }

        ctx.send(embed.build())
    }
}
