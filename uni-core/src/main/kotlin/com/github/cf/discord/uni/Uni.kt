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
package com.github.cf.discord.uni

import com.github.cf.discord.uni.core.EnvVars
import com.github.cf.discord.uni.database.DatabaseWrapper
import com.github.cf.discord.uni.database.schema.*
import com.github.cf.discord.uni.extensions.asyncTransaction
import com.github.cf.discord.uni.listeners.*
import com.github.cf.discord.uni.stateful.CoroutineDispatcher
import com.github.natanbc.weeb4j.TokenType
import com.github.natanbc.weeb4j.Weeb4J
import mu.KotlinLogging
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Uni {

    init {
        Database.connect(
                "jdbc:postgresql://${EnvVars.DATABASE_HOST}:${EnvVars.DATABASE_PORT}/${EnvVars.DATABASE_SCHEMA}",
                "org.postgresql.Driver",
                EnvVars.DATABASE_USERNAME!!,
                EnvVars.DATABASE_PASSWORD!!
        )

        asyncTransaction(pool){
            SchemaUtils.createMissingTablesAndColumns(
                    Guilds,
                    Logs,
                    ModLogs,
                    Roles,
                    Starboard,
                    Users,
                    WewCounter,
                    Core
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
        lateinit var wolkeApi: Weeb4J
        val prefix: List<String> = EnvVars.PREFIX!!.split("::")
        val prefixes = prefix.toList()

        const val MINIMUM_FOR_LEVEL_1 = 900
    }

    fun build(){
        jda = JDABuilder(AccountType.BOT).apply {
            setToken(DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).discordToken)
            setAutoReconnect(true)
            addEventListeners(EventListener())
        }.build()

        Uni.jda = jda
    }

    fun build(firstShard: Int, lastShard: Int, total: Int){
        shardManager = DefaultShardManagerBuilder().apply {
            setToken(DatabaseWrapper.getCore().get(1, TimeUnit.SECONDS).discordToken)
            addEventListeners(EventListener())
            setAutoReconnect(true)
            setShardsTotal(-1)
            setBulkDeleteSplittingEnabled(false)
        }.build()
    }

    fun start(): Boolean {
        return try {
            LOGGER.debug { "Logging in..." }
            LOGGER.info {
                "\n_   _   _  _   ___\n"+
                "| | | | | \\| | |_ _|\n"+
                "| |_| | | .` |  | |\n"+
                " \\___/  |_|\\_| |___|"

            }
            build(0, (EnvVars.TOTAL_SHARDS!!.toInt() - 1), EnvVars.TOTAL_SHARDS.toInt())

            if(EnvVars.WEEB_SH_TOKEN != null){
                LOGGER.info { "Wolke API Key present, enabling Weeb4J" }
                wolkeApi = Weeb4J.Builder()
                        .setToken(TokenType.BEARER, EnvVars.WEEB_SH_TOKEN)
                        .build()
            }
            true
        } catch (e: Exception) {
            LOGGER.error(e) { "An error has occurred in starting the bot!" }
            false
        }
    }
}
