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

import java.util.concurrent.ConcurrentHashMap

/**
 * Per-guild poll manager
 */
class PollManager {

    // Map of poll title to polls
    private val polls: MutableMap<String, Poll> = ConcurrentHashMap()
    // Map of message IDs to polls
    private val messageIdToPolls: MutableMap<Long, Poll> = ConcurrentHashMap()
    // Map of message IDs to poll embeds

    /**
     * Gets the poll with the specified title from the poll-title map.
     */
    fun getPoll(pollTitle: String): Poll? = polls[pollTitle]

    /**
     * Gets the poll by message id from the message-id-poll map.
     */
    fun getPollByMessageId(messageId: Long): Poll? = messageIdToPolls[messageId]

    /**
     * Adds a poll to the poll-title and message-id-poll maps.
     */
    fun addPoll(poll: Poll) {
        polls[poll.title] = poll
        messageIdToPolls[poll.embed.message.idLong] = poll
    }

    /**
     * Closes a specific poll based on its message id.
     */
    fun closePoll(messageId: Long) = messageIdToPolls[messageId]?.close()

    /**
     * Removes a poll from the guild based on its message id.
     */
    fun removePoll(poll: Poll) {
        polls.remove(poll.title)
        messageIdToPolls.remove(poll.embed.message.idLong)
    }

    /**
     * Checks whether a poll with the specified title exists in the guild.
     */
    fun hasPoll(pollTitle: String): Boolean = polls.containsKey(pollTitle)
}
