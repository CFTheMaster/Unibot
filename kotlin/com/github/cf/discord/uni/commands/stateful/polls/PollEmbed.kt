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
package com.github.cf.discord.uni.commands.stateful.polls

import com.github.cf.discord.uni.Reactions
import com.github.cf.discord.uni.embed.NumericOptionEmbed
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import java.util.function.Consumer

class PollEmbed(
        channel: TextChannel,
        requester: Member,
        private val poll: Poll,
        private val pollManager: PollManager
) : NumericOptionEmbed<Poll>(channel, requester.user, poll, poll.options.size, removeListenerDelaySeconds = Long.MAX_VALUE) {

    override fun onReactionAdd(event: GuildMessageReactionAddEvent) {
        val choiceNum = Reactions.EMOJI_TO_INT[event.reactionEmote.name] ?: return
        val author = event.member
        if (!author.user.isBot && choiceNum <= numChoices) obj.addVote(choiceNum, author)
    }

    override fun onReactionRemove(event: GuildMessageReactionRemoveEvent) {
        val choiceNum = Reactions.EMOJI_TO_INT[event.reactionEmote.name] ?: return
        val author = event.member
        if (!author.user.isBot && choiceNum <= numChoices) obj.retractVote(choiceNum, author)
    }

    override fun queueConsumer(): Consumer<Message> {
        return super.queueConsumer().andThen {
            pollManager.addPoll(poll)
            poll.embed = this
        }
    }
}
