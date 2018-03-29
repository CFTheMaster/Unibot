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

import com.github.cf.discord.uni.maxByList
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.TextChannel

data class Poll(
        val channel: TextChannel,
        val author: Member,
        val title: String,
        val options: List<PollOption>
) {

    private var isClosed: Boolean = false
    lateinit var embed: PollEmbed

    /**
     * Closes the poll to prevent any more voting.
     */
    fun close() {
        isClosed = true
    }

    /**
     * Returns a boolean denoting whether the poll can still accept votes or not.
     */
    fun isClosed(): Boolean = isClosed

    /**
     * Adds a user's vote to the corresponding options based on the selected option's index number.
     */
    fun addVote(choiceNum: Int, user: Member) {
        if (!isClosed) options[choiceNum - 1].add(user)
    }

    /**
     * Retracts a user's vote from the corresponding options based on the selected option's index number.
     */
    fun retractVote(choiceNum: Int, user: Member) {
        if (!isClosed) options[choiceNum - 1].retract(user)
    }

    /**
     * Returns a list of winning poll options based on the highest number of votes (ties are possible). For a poll
     * option to be considered, it has to have greater than 0 votes.
     */
    fun getWinningOptions(): List<PollOption> = options.maxByList({ it.count() }, { 0 })

    /**
     * Gets a user-friendly String representation of the poll options in a list, with each poll option prefixed by
     * its choice number (where choice number = list's index + 1)
     */
    fun getNumeratedOptions(): List<String> = options.mapIndexed { index, option -> "${index + 1}. $option" }.toList()
}
