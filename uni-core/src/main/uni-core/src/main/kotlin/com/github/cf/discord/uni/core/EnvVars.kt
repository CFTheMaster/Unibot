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
package com.github.cf.discord.uni.core

object EnvVars {
    @JvmStatic
    val BOT_TOKEN = readEnvVars("bot_token")
    @JvmStatic
    val LAVAPLAYER_NICONICO_EMAIL = readEnvVars("lavaplayer_niconico_email")
    @JvmStatic
    val LAVAPLAYER_NICONICO_PASSWORD = readEnvVars("lavaplayer_niconico_password")
    @JvmStatic
    val GOOGLE_API_KEY = readEnvVars("google_api_key")
    @JvmStatic
    val GOOGLE_SEARCH_ENGINE = readEnvVars("google_search_engine_id")

    // Redis env vars
    val REDIS_HOST = readEnvVars("redis_host")

    // PostgreSQL env vars
    @JvmStatic
    val DATABASE_DRIVER = readEnvVars("database_driver")
    val DATABASE_HOST = readEnvVars("database_host")
    val DATABASE_PORT = readEnvVars("database_port")
    val DATABASE_SCHEMA = readEnvVars("database_schema")
    val DATABASE_USERNAME = readEnvVars("database_username")
    val DATABASE_PASSWORD = readEnvVars("database_password")

    @JvmStatic
    private fun readEnvVars(envVar: String): String {
        return System.getenv(envVar) ?: throw RuntimeException("Could not read environment variable \"$envVar\", make sure it is supplied and not empty!")
    }
}
