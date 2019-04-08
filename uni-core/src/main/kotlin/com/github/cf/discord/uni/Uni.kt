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
import com.github.cf.discord.uni.commands.`fun`.CatgirlCommand
import com.github.cf.discord.uni.commands.`fun`.DuckCommand
import com.github.cf.discord.uni.commands.`fun`.LewdCatgirlCommand
import com.github.cf.discord.uni.commands.admin.AdminCommand
import com.github.cf.discord.uni.commands.audio.AudioPlayerCommand
import com.github.cf.discord.uni.commands.audio.AudioPlayerPanel
import com.github.cf.discord.uni.commands.audio.VoiceChannelCommand
import com.github.cf.discord.uni.commands.info.UserInfoCommand
import com.github.cf.discord.uni.commands.info.BotInfoCommand
import com.github.cf.discord.uni.commands.info.HelpCommand
import com.github.cf.discord.uni.commands.info.InviteCommand
import com.github.cf.discord.uni.commands.info.PingCommand
import com.github.cf.discord.uni.commands.info.ServerInfoCommand
import com.github.cf.discord.uni.commands.info.UptimeCommand
import com.github.cf.discord.uni.commands.info.VoteCommand
import com.github.cf.discord.uni.commands.owner.TestApiCommand
import com.github.cf.discord.uni.commands.owner.StatusCommand
import com.github.cf.discord.uni.commands.owner.SayCommand
import com.github.cf.discord.uni.commands.owner.ChangeNickNameCommand
import com.github.cf.discord.uni.commands.owner.ChangeNameCommand
import com.github.cf.discord.uni.commands.owner.AmIOwnerCommand
import com.github.cf.discord.uni.commands.owner.ShellCommand
import com.github.cf.discord.uni.commands.query.*
import com.github.cf.discord.uni.commands.stateful.*
import com.github.cf.discord.uni.commands.system.*
import com.github.cf.discord.uni.commands.userColors.*
import com.github.cf.discord.uni.jsr223.*
import com.github.cf.discord.uni.listeners.*
import com.github.kvnxiao.discord.meirei.Meirei
import com.github.kvnxiao.discord.meirei.jda.MeireiJDA
import mu.KotlinLogging
import net.dv8tion.jda.core.*

class Uni(token: String) {

    companion object {
        @JvmField
        val LOGGER = KotlinLogging.logger(Uni::class.java.name)
    }

    private val clientBuilder = JDABuilder(AccountType.BOT)
            .setToken(token)
    private val meirei: Meirei = MeireiJDA(clientBuilder)

    fun start(): Boolean {
        return try {
            // TODO: load external commands
            LOGGER.debug { "Logging in..." }
            LOGGER.info {
                "\n_   _   _  _   ___\n"+
                "| | | | | \\| | |_ _|\n"+
                "| |_| | | .` |  | |\n"+
                " \\___/  |_|\\_| |___|"

            }
            clientBuilder
                    .addEventListener(
                            ReadyEventListener(),
                            MessageLogListener(),
                            CommandListener(),
                            GuildJoinLeaveListener())
                    .registerCommands()
                    .buildAsync()
            true
        } catch (e: Exception) {
            LOGGER.error(e) { "An errorEmbed occurred in starting the bot!" }
            false
        }
    }

    private fun JDABuilder.registerCommands(): JDABuilder {
        meirei.addAnnotatedCommands(
                //Color User
                changeMyColor(),
                // System
                BotInfoCommand(),
                HelpCommand(),
                PingCommand(),
                UptimeCommand(),
                ShutdownCommand(),
                RestartCommand(),
                InviteCommand(),
                UserInfoCommand(),
                ServerInfoCommand(),
                VoteCommand(),

                // Moderation Commands
                AdminCommand(),

                // Audio
                VoiceChannelCommand(),
                AudioPlayerCommand(),

                // Queryable commands,
                GoogleCommand(),
                WikipediaCommand(),
                PcPartPickerCommand(),
                UrbanDictionaryCommand(),
                SauceNAOCommand(),

                // Stateful
                PollCommand(),

                // Fun
                EightBallCommand(),
                CatgirlCommand(),
                LewdCatgirlCommand(),
                DuckCommand(),

                // Scripting
                KotlinScriptCommand(),

                // Owner Only
                StatusCommand(),
                ChangeNameCommand(),
                ChangeNickNameCommand(),
                TestApiCommand(),
                SayCommand(),
                AmIOwnerCommand(),
                ShellCommand()
        )
        return this
    }
}
