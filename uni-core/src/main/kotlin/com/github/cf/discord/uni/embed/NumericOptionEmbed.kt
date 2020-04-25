/*
 *   Copyright (C) 2017-2021 computerfreaker
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
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

/**
 * Creates an embed message with up to 9 choices, which relies on reaction listeners for stateful interactions.
 */
abstract class NumericOptionEmbed<T>(
        channel: TextChannel,
        requester: User,
        obj: T,
        protected val numChoices: Int = 9,
        protected val isCancellable: Boolean = false,
        removeListenerDelaySeconds: Long = 60
) : ReactableEmbed<T>(channel, requester, obj, removeListenerDelaySeconds) {

    init {
        require(numChoices in 2..9) { "The number of available choices must be a number from 2 to 9" }
    }

    override fun addReactions(message: Message) {
        // Add reaction emojis for selection numbers
        Reactions.NUMBERS.sliceArray(1..numChoices).forEach {
            message.addReaction(it).queue()
        }
        // Add crossmark for cancelling
        if (isCancellable) {
            message.addReaction(Reactions.REGIONAL_CROSSMARK).complete()
        }
        LOGGER.debug { "Added reactions for ReactableEmbed message: ${message.idLong} in ${message.guild.idLong}" }
    }
}
