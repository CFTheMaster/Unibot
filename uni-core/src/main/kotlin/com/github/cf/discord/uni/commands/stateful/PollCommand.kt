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
package com.github.cf.discord.uni.commands.stateful

import com.github.kvnxiao.discord.meirei.annotations.Command
import com.github.kvnxiao.discord.meirei.annotations.CommandGroup
import com.github.kvnxiao.discord.meirei.command.CommandContext
import com.github.cf.discord.uni.Lib.LINE_SEPARATOR
import com.github.cf.discord.uni.commands.stateful.polls.Poll
import com.github.cf.discord.uni.commands.stateful.polls.PollEmbed
import com.github.cf.discord.uni.commands.stateful.polls.PollManager
import com.github.cf.discord.uni.commands.stateful.polls.PollOption
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.getFromQuotes
import com.github.cf.discord.uni.splitByLines
import com.github.cf.discord.uni.stateful.GuildStateManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import reactor.core.publisher.toMono

@CommandGroup("stateful")
class PollCommand {

    companion object {
        const val MIN_LINES = 3
        const val MAX_LINES = 10
    }

    private val guildPollManager: GuildStateManager<PollManager> = GuildStateManager()

    private fun GuildStateManager<PollManager>.getOrPut(guildId: Long): PollManager = this.getOrPut(guildId, { PollManager() })

    private fun validatePollLines(lines: Int, event: MessageReceivedEvent): Boolean = when {
        lines < MIN_LINES -> {
            event.channel.sendMessage("To create a poll, you must specify a title and at least two options on a line-by-line basis.").queue()
            false
        }
        lines > MAX_LINES -> {
            event.channel.sendMessage("To create a poll, you are limited to specifying a maximum of 9 poll options.").queue()
            false
        }
        else -> true
    }

    private fun validatePollTitle(title: String, event: MessageReceivedEvent): Boolean {
        if (title.isBlank()) {
            event.channel.sendMessage("A poll may not have an empty title, please try creating a poll with an actual title").queue()
            return false
        }
        return if (guildPollManager.getOrPut(event.guild.idLong).hasPoll(title)) {
            event.channel.sendMessage("A poll with this title already exists, please try creating a poll using a different title.").queue()
            false
        } else {
            true
        }
    }

    /// @@@@@@@@@@@@@@@@@@@@@@@@@@
    /// Command declarations below
    /// @@@@@@@@@@@@@@@@@@@@@@@@@@

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "poll",
            aliases = ["poll"],
            description = "Fetches poll information based on provided name of poll.",
            usage = "<name of poll to fetch information for>"
    )
    fun poll(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val args = context.args ?: return
            val pollManager = guildPollManager.getOrPut(event.guild.idLong)
            val title = args.getFromQuotes()

            // Retrieve poll from manager and print info into an embed (options -> votes & who voted)
            title.toMono()
                    .filter { pollManager.hasPoll(it) }
                    .map { pollManager.getPoll(it)!! }
                    .subscribe {
                        val options = it.options
                        val embed = EmbedBuilder()
                                .setTitle(title)
                        embed.setFooter("Status: ${if (it.isClosed()) "CLOSED" else "OPEN"}", it.author.user.effectiveAvatarUrl)
                        options.forEachIndexed { i, option ->
                            embed.addField("${i + 1}. ${option.optionName}",
                                    if (option.count() > 0) option.votes().joinToString(separator = " ", prefix = "`${option.count()} votes` ") { it.asMention } else "`0 votes` *nobody has voted for this option*",
                                    false)
                        }
                        embed.addField("", "Poll created by: ${it.author.asMention}", false)
                        event.channel.sendMessage(embed.build()).queue()
                    }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "poll_create",
            aliases = ["makepoll"],
            description = "Creates a poll with the provided name, and options to vote for on subsequent lines.",
            usage = "<poll name to create, can be in quotes>\r\noption 1\noption 2\noption 3..."
    )
    fun createPoll(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val args = context.args?.splitByLines() ?: return
            args.toMono()
                    .filter { validatePollLines(it.size, event) }
                    .filter { validatePollTitle(it.first().getFromQuotes(), event) }
                    .subscribe {
                        val pollManager = guildPollManager.getOrPut(event.guild.idLong)
                        val title = it.first().getFromQuotes()
                        val options = it.subList(1, it.size).map { PollOption(it) }
                        val member = event.member
                        val poll = Poll(event.textChannel, event.member, title, options)

                        // Output poll to users as an embed
                        val pollEmbed = PollEmbed(event.textChannel, member, poll, pollManager)
                        pollEmbed.embedBuilder
                                .setTitle("Poll: $title")
                                .setDescription(poll.getNumeratedOptions().joinToString(separator = LINE_SEPARATOR, prefix = "`", postfix = "`"))
                                .addField("", "Poll created by: ${event.author.asMention}", false)
                                .setFooter("Status: OPEN", event.author.avatarUrl)
                        pollEmbed.queue()
                    }
        }
    }

    @Command(
            prefix = "${EnvVars.PREFIX}",
            id = "poll_close",
            aliases = ["closepoll"],
            description = "Closes a poll with the provided name and outputs the winning vote.",
            usage = "<poll name to close>"
    )
    fun closePoll(context: CommandContext, event: MessageReceivedEvent) {
        val author = event.author
        if(author!!.isBot) {
            return
        } else {
            val args = context.args ?: return
            val pollManager = guildPollManager.getOrPut(event.guild.idLong)

            args.getFromQuotes()
                    .toMono()
                    .filter { pollManager.hasPoll(it) }
                    .map { pollManager.getPoll(it)!! }
                    .subscribe {
                        // Remove poll from manager and clean it up
                        pollManager.removePoll(it)
                        it.close()

                        // Get winning votes from poll
                        val winners = it.getWinningOptions()

                        // Output poll results to users in an embed
                        val embed = EmbedBuilder()
                                .setTitle("Poll results for: ${it.title}")
                                .setDescription(when {
                                    winners.size == 1 -> "The winning vote is:"
                                    winners.size > 1 -> "A tie between the following votes:"
                                    else -> "Nobody has voted in the poll."
                                })
                                .setFooter("Status: CLOSED", it.author.user.effectiveAvatarUrl)
                        winners.forEachIndexed { i, option ->
                            embed.addField("${i + 1}. ${option.optionName}", "${option.votes().joinToString(separator = " ", prefix = "`${option.count()} votes`") { it.asMention }} ", false)
                        }
                        embed.addField("", "Poll created by: ${it.author.asMention}>", false)
                        event.channel.sendMessage(
                                embed.build()
                        ).queue()

                        // Remove reaction listeners from JDA client
                        it.embed.removeListener()
                        // Add check-mark reaction
                        it.embed.message.addReaction("\u2705").queue()
                        // TODO: edit message to change status to closed and have more information
                    }
        }
    }
}
