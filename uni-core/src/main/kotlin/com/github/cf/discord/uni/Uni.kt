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

import com.github.cf.discord.uni.async.asyncTransaction
import org.jetbrains.exposed.sql.*
import com.github.cf.discord.uni.db.schema.*
import com.github.cf.discord.uni.commands.`fun`.*
import com.github.cf.discord.uni.commands.admin.*
import com.github.cf.discord.uni.commands.audio.*
import com.github.cf.discord.uni.commands.info.*
import com.github.cf.discord.uni.commands.owner.*
import com.github.cf.discord.uni.commands.query.*
import com.github.cf.discord.uni.commands.stateful.*
import com.github.cf.discord.uni.commands.system.*
import com.github.cf.discord.uni.commands.userColors.*
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.jsr223.*
import com.github.cf.discord.uni.listeners.*
import com.github.kvnxiao.discord.meirei.Meirei
import com.github.kvnxiao.discord.meirei.jda.MeireiJDA
import mu.KotlinLogging
import net.dv8tion.jda.core.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Uni(token: String) {

    companion object {
        @JvmField
        val LOGGER = KotlinLogging.logger(Uni::class.java.name)
        val pool: ExecutorService by lazy {
            Executors.newCachedThreadPool {
                Thread(it, "Uni-Thread").apply {
                    isDaemon = true
                }
            }
        }
    }

    init {
        Database.connect(
                "jdbc:postgresql://${EnvVars.DATABASE_HOST}:${EnvVars.DATABASE_PORT}/${EnvVars.DATABASE_SCHEMA}",
                "org.postgresql.Driver",
                "${EnvVars.DATABASE_USERNAME}"
        )

        asyncTransaction(pool) {
            SchemaUtils.create(
                    Guilds,
                    Users,
                    Starboard,
                    Logs,
                    Modlogs,
                    Contracts,
                    Tags,
                    Reminders,
                    Scripts,
                    Items,
                    Restrictions,
                    Roles
            )
        }.execute()
    }

    private val clientBuilder = JDABuilder(AccountType.BOT)
            .setToken(token)
    private val meirei: Meirei = MeireiJDA(clientBuilder)

    fun start(): Boolean {
        return try {
            // TODO: load external commands
            LOGGER.debug { "Logging in..." }
            LOGGER.info {
                "_   _   _  _   ___\n"+
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
                AmIOwnerCommand()
        )
        return this
    }
}
