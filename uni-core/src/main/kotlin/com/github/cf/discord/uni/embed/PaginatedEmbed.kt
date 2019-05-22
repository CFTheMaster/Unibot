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
package com.github.cf.discord.uni.embed

import com.github.cf.discord.uni.Reactions
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import reactor.core.Disposable
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

abstract class PaginatedEmbed<T>(
        channel: TextChannel,
        requester: User,
        obj: T,
        protected val maxPerPage: Int,
        protected var page: Int = 1,
        protected var maxPages: Int,
        private val updateIntervalMs: Long = 1000
) : ReactableEmbed<T>(channel, requester, obj) {

    private lateinit var updateScheduler: Disposable
    var lastPage = page

    override fun addReactions(message: Message) {
        // Add reaction emojis for left and right
        message.addReaction(Reactions.LEFT_ARROW).complete()
        message.addReaction(Reactions.RIGHT_ARROW).complete()
        // Add crossmark for cancelling
        message.addReaction(Reactions.REGIONAL_CROSSMARK).complete()
    }

    override fun queueConsumer(): Consumer<Message> {
        return super.queueConsumer().andThen {
            updateScheduler = schedulers.schedulePeriodically({
                if (lastPage != page) {
                    this.updateMessage()
                }
            }, 0, updateIntervalMs, TimeUnit.MILLISECONDS)
        }
    }

    override fun removeListener() {
        super.removeListener()
        updateScheduler.dispose()
    }

    override fun onReactionAdd(event: GuildMessageReactionAddEvent) {
        // Only accept the original user's search
        onReaction(event)
    }

    override fun onReactionRemove(event: GuildMessageReactionRemoveEvent) {
        onReaction(event)
    }

    private fun onReaction(event: GenericGuildMessageReactionEvent) {
        // Only accept reactions from user who issued the embed command
        if (event.member.user.idLong == requester.idLong) {
            when (event.reactionEmote.name) {
                Reactions.LEFT_ARROW -> this.onLeft()
                Reactions.RIGHT_ARROW -> this.onRight()
                Reactions.REGIONAL_CROSSMARK -> this.onCrossmark()
            }
        }
    }

    private fun onLeft() {
        if (page > 1) {
            page--
        }
    }
    private fun onRight() {
        if (page < maxPages) {
            page++
        }
    }
    private fun onCrossmark() {
        this.removeListener()
    }

    /**
     * Updates the message based on a change in pages
     */
    abstract fun updateMessage()
}
