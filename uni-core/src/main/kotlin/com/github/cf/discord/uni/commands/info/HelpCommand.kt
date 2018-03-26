/*
 *   Copyright (C) 2017-2018 computerfreaker
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
package com.github.cf.discord.uni.commands.info

import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.annotations.Permissions
import com.github.kvnxiao.discord.meirei.annotations.RegistryAware
import com.github.kvnxiao.discord.meirei.command.CommandContext
import com.github.kvnxiao.discord.meirei.command.CommandDefaults
import com.github.kvnxiao.discord.meirei.command.CommandProperties
import com.github.kvnxiao.discord.meirei.command.DiscordCommand
import com.github.kvnxiao.discord.meirei.command.database.CommandRegistryRead
import com.github.kvnxiao.discord.meirei.utility.SplitString.Companion.splitString
import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction
import java.awt.Color
import java.util.StringJoiner

@CommandGroup("system.help")
class HelpCommand {

    companion object {
        const val EMBED_TITLE = "Uni Help Page"
        const val WEBSITE_URL = "https://bots.computerfreaker.cf/uni/"
        const val COMMANDS_PER_PAGE = 10
        const val VERSION_NUMBER = "0.0.2"
        @JvmStatic
        val EMBED_COLOUR = Color(125, 165, 222)
    }

    @Command(
            prefix = "uni!",
            aliases = ["help"],
            id = "main",
            description = "The help page for Uni.",
            usage = "<command alias> | {page number} | all"
    )
    @Permissions(
            allowDm = true
    )
    @RegistryAware
    fun help(context: CommandContext, event: MessageReceivedEvent) {
        val args = context.args
        if (args != null) {
            val page = args.toIntOrNull()
            if (page != null) {
                // Send help book
                event.sendHelpBook(context, page).queue()
            }
            // Send command info
            event.sendCommandInfo(context, args)?.queue()
        } else {
            // Send help book
            event.sendHelpBook(context).queue()
        }
    }

    @Command(
            aliases = ["all"],
            id = "all",
            parentId = "main",
            description = "lists all available command aliases"
    )
    @Permissions(
            allowDm = true
    )
    @RegistryAware
    fun helpAll(context: CommandContext, event: MessageReceivedEvent) {
        val registry = context.readOnlyCommandRegistry!!
        val totalCommandsCount = registry.getAllCommandAliases().size
        val embed = EmbedBuilder()
                .setColor(EMBED_COLOUR)
                .setAuthor(EMBED_TITLE, WEBSITE_URL, event.jda.selfUser.avatarUrl)
                .setDescription("Uni v$VERSION_NUMBER | Displaying all available commands.")
                .addField("List of all commands", "`${registry.getAllCommandAliases().joinToString()}`", false)
                .setFooter("$totalCommandsCount commands, ${maxPageSize(totalCommandsCount)} pages available", null)
                .build()
        event.textChannel.sendMessage(embed).queue()
    }

    private fun maxPageSize(size: Int): Int {
        if (size < 0) return 0
        // Quick ceiling function assuming that size >= 0 always
        return (size + COMMANDS_PER_PAGE - 1) / COMMANDS_PER_PAGE
    }

    private fun MessageReceivedEvent.sendSubCommandInfo(context: CommandContext, parentCommand: DiscordCommand, prefixedAlias: String): RestAction<Message>? {
        val (alias, subAlias) = splitString(prefixedAlias)
        val registry = context.readOnlyCommandRegistry!!
        alias.let {
            val command = registry.getSubCommandByAlias(alias, parentCommand.id)
            command?.let {
                if (subAlias != null) {
                    return this.sendSubCommandInfo(context, it, subAlias)
                }
                return this.textChannel.sendMessage(createHelpEmbed(context, registry, registry.getPropertiesById(it.id)!!, this.jda.selfUser.avatarUrl))
            }
        }
        return null
    }

    private fun createHelpEmbed(context: CommandContext, registry: CommandRegistryRead, info: CommandProperties, avatarUrl: String): MessageEmbed {
        val builder = EmbedBuilder()
                .setColor(EMBED_COLOUR)
                .setAuthor(EMBED_TITLE, WEBSITE_URL, avatarUrl)
                .setDescription("Uni v$VERSION_NUMBER | Displaying information for command: `${context.args}`")

        if (info.parentId == CommandDefaults.PARENT_ID) {
            builder.addField("Activation Prefix", "`${info.prefix}`", true)
        }
        return builder.addField("Aliases", "`${info.aliases.joinToString()}`", true)
                .addField("Description", info.description, false)
                .addField("Usage", "`${context.args} ${info.usage}`", false)
                .addField("Child Commands", registry.getSubCommandRegistry(info.id)?.getAllSubCommandAliases()?.joinToString(prefix = "`", postfix = "`") ?: "none", false)
                .build()
    }

    private fun MessageReceivedEvent.sendCommandInfo(context: CommandContext, prefixedAlias: String): RestAction<Message>? {
        val (alias, subAlias) = splitString(prefixedAlias)
        val registry = context.readOnlyCommandRegistry!!
        alias.let {
            val command = registry.getCommandByAlias(it)

            command?.let {
                if (subAlias != null) {
                    return this.sendSubCommandInfo(context, it, subAlias)
                }
                return this.textChannel.sendMessage(createHelpEmbed(context, registry, registry.getPropertiesById(it.id)!!, this.jda.selfUser.avatarUrl))
            }
        }
        return null
    }

    private fun MessageReceivedEvent.sendHelpBook(context: CommandContext): RestAction<Message> {
        val registry = context.readOnlyCommandRegistry!!
        val prefixedAlias = context.alias
        val totalCommandsCount = registry.getAllCommandAliases().size
        val embed = EmbedBuilder()
                .setColor(EMBED_COLOUR)
                .setAuthor(EMBED_TITLE, WEBSITE_URL, this.jda.selfUser.avatarUrl)
                .setDescription("Welcome to the help page for Uni v$VERSION_NUMBER")
                .addField("List of all commands", "`$prefixedAlias all`: lists all available command aliases.", false)
                .addField("View commands per page", "`$prefixedAlias {number}`: lists commands on the specified page (e.g. `$prefixedAlias 1`).", false)
                .addField("View a specific command", "`$prefixedAlias <command alias>`: view a specific command's details (e.g. `$prefixedAlias ${context.properties.prefix}ping`).", false)
                .setFooter("$totalCommandsCount commands, ${maxPageSize(totalCommandsCount)} pages available", null)
                .build()
        return this.textChannel.sendMessage(embed)
    }

    private fun MessageReceivedEvent.sendHelpBook(context: CommandContext, page: Int): RestAction<Message> {
        val registry = context.readOnlyCommandRegistry!!
        val allAliases = registry.getAllCommandAliases()
        return this.textChannel.sendMessage(allAliases.toEmbed(page, registry.getAllCommandAliases().size, this.jda.selfUser.avatarUrl))
    }

    private fun List<String>.toEmbed(p: Int, totalCommandCount: Int, avatarUrl: String): MessageEmbed {
        val page = if (p <= 0) 1 else p

        val min = (page - 1) * COMMANDS_PER_PAGE
        val max = Math.min(page * COMMANDS_PER_PAGE, this.size)

        if (min >= this.size) {
            return this.toEmbed(maxPageSize(this.size), totalCommandCount, avatarUrl)
        }

        val joiner = StringJoiner(LINE_SEPARATOR)
        for (i in (min until max)) {
            joiner.add("`${i + 1}. ${this[i]}`")
        }

        return EmbedBuilder()
                .setColor(EMBED_COLOUR)
                .setAuthor(EMBED_TITLE, WEBSITE_URL, avatarUrl)
                .setDescription("Listing commands on page $page")
                .addField("Commands - Page $page", joiner.toString(), false)
                .setFooter("Page $page/${maxPageSize(totalCommandCount)}", null)
                .build()
    }
}
