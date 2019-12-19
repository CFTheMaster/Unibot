/*
 *   Copyright (C) 2017-2020 computerfreaker
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

import com.github.cf.discord.uni.reactions.ReactionChangeListener
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.EventListener
import reactor.core.Disposable
import reactor.core.scheduler.Schedulers
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

abstract class ReactableEmbed<T>(
        /**
         * Channel where the embed is to be sent
         */
        protected val channel: TextChannel,
        /**
         * User who issued the command, resulting in this reactable embed message being sent.
         */
        protected val requester: User,
        /**
         * Generic object for the embed operate on (i.e. state changes)
         */
        protected val obj: T,
        protected val removeListenerDelaySeconds: Long = 60
) : EventListener, ReactionChangeListener {

    val embedBuilder = EmbedBuilder()

    companion object {
        @JvmStatic
        protected val schedulers = Schedulers.newElastic(ReactableEmbed::class.java.simpleName)
        @JvmStatic
        protected val LOGGER = KotlinLogging.logger { }
    }

    // Scheduler for removing event listener from the client after queue() is invoked
    private lateinit var scheduler: Disposable

    // Get the JDA client for this reactable embed to add / remove listeners
    private val client: JDA = channel.jda

    // The message id after the embed is sent
    lateinit var message: Message

    fun onEvent(event: Event) {
        when (event) {
            is GuildMessageReactionAddEvent -> if (event.messageIdLong == message.idLong && event.guild.idLong == channel.guild.idLong) this.onReactionAdd(event)
            is GuildMessageReactionRemoveEvent -> if (event.messageIdLong == message.idLong && event.guild.idLong == channel.guild.idLong) this.onReactionRemove(event)
            is GuildMessageReactionRemoveAllEvent -> if (event.messageIdLong == message.idLong && event.guild.idLong == channel.guild.idLong) this.onReactionRemoveAll(event)
        }
    }

    /**
     * Sends the reactable embed message to the specified channel, and updates the message id property after the embed is sent.
     * Then adds the specified reactions to the message and registers itself as an event listener for the JDA client.
     */
    open fun queue() {
        channel.sendMessage(this.embedBuilder.build()).queue(queueConsumer())
    }

    protected open fun queueConsumer(): Consumer<Message> = Consumer {
        // Set message id of this message
        message = it
        // Add reactions to embed message
        this.addReactions(it)
        // Register reaction listeners for this embed message, for when a user selects a reaction
        this.registerListener()
    }

    open fun registerListener() {
        // Register this instance as an event listener to propagate reaction-specific events to the ReactionChangeListener implementation methods
        // Schedule to remove the ReactableEmbed event listener in X seconds (defaults to 60 seconds)
        LOGGER.debug { "Creating a scheduler to remove the reaction listener for ReactableEmbed message: ${message.idLong} in ${channel.guild.idLong}, in $removeListenerDelaySeconds seconds." }
        scheduler = schedulers.schedule(this::removeListener, removeListenerDelaySeconds, TimeUnit.SECONDS)
        LOGGER.debug { "Registered reaction listener for ReactableEmbed message: ${message.idLong} in ${channel.guild.idLong}" }
        client.addEventListener(this)
    }

    open fun removeListener() {
        // Remove this instance from the client event listener pool
        client.removeEventListener(this)
        // Clear all reactions
        message.clearReactions().queue()
        // Dispose the scheduler if it has not already been disposed
        scheduler.dispose()
        LOGGER.debug { "Removed reaction listener for ReactableEmbed message: ${message.idLong} in ${channel.guild.idLong}" }
    }

    /**
     * Adds reactions to the provided message (called by queue() method after the message is sent successfully).
     * @see ReactableEmbed.queue
     */
    abstract fun addReactions(message: Message)
}
