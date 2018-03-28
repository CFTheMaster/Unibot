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
package com.github.cf.discord.uni

import com.github.cf.discord.uni.commands.`fun`.EightBallCommand
import com.github.cf.discord.uni.commands.audio.AudioPlayerCommand
import com.github.cf.discord.uni.commands.audio.VoiceChannelCommand
import com.github.cf.discord.uni.commands.info.HelpCommand
import com.github.cf.discord.uni.commands.info.PingCommand
import com.github.cf.discord.uni.commands.info.UptimeCommand
import com.github.cf.discord.uni.commands.info.InviteCommand
import com.github.cf.discord.uni.commands.query.GoogleCommand
import com.github.cf.discord.uni.commands.query.PcPartPickerCommand
import com.github.cf.discord.uni.commands.query.UrbanDictionaryCommand
import com.github.cf.discord.uni.commands.query.WikipediaCommand
import com.github.cf.discord.uni.commands.stateful.PollCommand
import com.github.cf.discord.uni.commands.system.RestartCommand
import com.github.cf.discord.uni.commands.system.ShutdownCommand
import com.github.cf.discord.uni.jsr223.KotlinScriptCommand
import com.github.cf.discord.uni.listeners.MessageLogListener
import com.github.cf.discord.uni.listeners.ReadyEventListener
import com.github.kvnxiao.discord.meirei.Meirei
import com.github.kvnxiao.discord.meirei.jda.MeireiJDA
import mu.KotlinLogging
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder

class Uni(token: String) {

    companion object {
        @JvmField
        val LOGGER = KotlinLogging.logger(Uni::class.java.name)
    }

    private val clientBuilder = JDABuilder(AccountType.BOT).setToken(token)
    private val meirei: Meirei = MeireiJDA(clientBuilder)

    fun start(): Boolean {
        return try {
            // TODO: load external commands
            LOGGER.debug { "Logging in..." }
            clientBuilder
                    .registerCommands()
                    .addEventListener(
                            ReadyEventListener(),
                            MessageLogListener()
                    )
                    .buildAsync()
            true
        } catch (e: Exception) {
            LOGGER.error(e) { "An errorEmbed occurred in starting the bot!" }
            false
        }
    }

    private fun JDABuilder.registerCommands(): JDABuilder {
        meirei.addAnnotatedCommands(
                // System
                HelpCommand(),
                PingCommand(),
                UptimeCommand(),
                ShutdownCommand(),
                RestartCommand(),
                InviteCommand(),

                // Audio
                VoiceChannelCommand(),
                AudioPlayerCommand(),

                // Queryable commands,
                GoogleCommand(),
                WikipediaCommand(),
                PcPartPickerCommand(),
                UrbanDictionaryCommand(),

                // Stateful
                PollCommand(),

                // Fun
                EightBallCommand(),

                // Scripting
                KotlinScriptCommand()
        )
        meirei.registerEventListeners(this)
        return this
    }
}
