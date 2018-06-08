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
import com.github.cf.discord.uni.commands.audio.*
import com.github.cf.discord.uni.commands.info.*
import com.github.cf.discord.uni.commands.owner.*
import com.github.cf.discord.uni.commands.query.*
import com.github.cf.discord.uni.commands.stateful.*
import com.github.cf.discord.uni.commands.system.*
import com.github.cf.discord.uni.commands.userColors.*
import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.entities.Config
import com.github.cf.discord.uni.jsr223.*
import com.github.cf.discord.uni.listeners.*
import com.github.kvnxiao.discord.meirei.Meirei
import com.github.kvnxiao.discord.meirei.jda.MeireiJDA
import mu.KotlinLogging
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Uni(private val config: Config) {

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
        lateinit var shardManager: ShardManager
        var jda: JDA? = null
    }

    init {
        Database.connect(
                "postgresql://${EnvVars.DATABASE_HOST}:${EnvVars.DATABASE_PORT}/${EnvVars.DATABASE_SCHEMA}",
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
    fun build(){
        LOGGER.debug { "Logging in..." }
        LOGGER.info {
            "\n_   _   _  _   ___\n"+
                    "| | | | | \\| | |_ _|\n"+
                    "| |_| | | .` |  | |\n"+
                    " \\___/  |_|\\_| |___|"

        }
        jda = JDABuilder(AccountType.BOT).apply {
            setToken("${EnvVars.BOT_TOKEN}")
            addEventListener(
                    ReadyEventListener(),
                    MessageLogListener(),
                    CommandListener(),
                    GuildJoinLeaveListener())
        }.buildAsync()

        Uni.jda = jda
    }

    fun build(firstShard: Int, lastShard: Int, total: Int) {
        shardManager = DefaultShardManagerBuilder().apply {
            setToken("${EnvVars.BOT_TOKEN}")
            addEventListeners(ReadyEventListener(),
                    MessageLogListener(),
                    CommandListener(),
                    GuildJoinLeaveListener())
            setAutoReconnect(true)
            setShardsTotal(total)
            setShards(firstShard, lastShard)
        }.build()
    }
}
