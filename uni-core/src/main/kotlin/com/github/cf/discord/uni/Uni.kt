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

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.database.schema.*
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.listeners.*
import com.github.cf.discord.uni.stateful.CoroutineDispatcher
import mu.KotlinLogging
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Uni(token: String) {

    init {
        Database.connect(
                "jdbc:postgresql://${EnvVars.DATABASE_HOST}:${EnvVars.DATABASE_PORT}/${EnvVars.DATABASE_SCHEMA}",
                "org.postgresql.Driver",
                EnvVars.DATABASE_USERNAME!!,
                EnvVars.DATABASE_PASSWORD!!
        )

        asyncTransaction(pool){
            SchemaUtils.create(
                    Guilds,
                    Logs,
                    ModLogs,
                    Roles,
                    Starboard,
                    Users
            )
        }.execute()
    }

    companion object {
        @JvmField
        val LOGGER = KotlinLogging.logger(Uni::class.java.name)
        val pool: ExecutorService by lazy {
            Executors.newCachedThreadPool {
                Thread(it, "Uni-Main-Pool-Thread").apply {
                    isDaemon = true
                }
            }
        }
        val coroutineDispatcher by lazy {
            CoroutineDispatcher(pool)
        }
        var jda: JDA? = null

        lateinit var shardManager: ShardManager
        val prefix: List<String> = EnvVars.PREFIX!!.split("::")
        val prefixes = prefix.toList()

        val MINIMUM_FOR_LEVEL_1 = 900
    }

    fun build(){
        jda = JDABuilder(AccountType.BOT).apply {
            setToken(EnvVars.BOT_TOKEN)
            addEventListener(EventListener())
        }.buildAsync()

        Uni.jda = jda
    }

    fun build(firstShard: Int, lastShard: Int, total: Int){
        shardManager = DefaultShardManagerBuilder().apply {
            setToken(EnvVars.BOT_TOKEN)
            addEventListeners(EventListener())
            setAutoReconnect(true)
            setShardsTotal(total)
            setShards(firstShard, lastShard)
        }.build()
    }

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
            build()
            true
        } catch (e: Exception) {
            LOGGER.error(e) { "An errorEmbed occurred in starting the bot!" }
            false
        }
    }
}
