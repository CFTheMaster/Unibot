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
package com.github.cf.discord.uni.core
import io.github.cdimascio.dotenv.dotenv

object EnvVars {

    private val dotenv = dotenv{
        ignoreIfMalformed = true
        directory = "./"
        filename = ".env"
    }

    // Redis env vars
    val REDIS_HOST = dotenv["redis_host"]

    // PostgreSQL env vars
    @JvmStatic
    val DATABASE_DRIVER = dotenv["database_driver"]
    val DATABASE_HOST = dotenv["database_host"]
    val DATABASE_PORT = dotenv["database_port"]
    val DATABASE_SCHEMA = dotenv["database_schema"]
    val DATABASE_USERNAME = dotenv["database_username"]
    val DATABASE_PASSWORD = dotenv["database_password"]

    @JvmStatic
    val PREFIX = dotenv["prefix"]

    @JvmStatic
    val TOTAL_SHARDS = dotenv["total_shards"]

    @JvmStatic
    val RANDOM_TEXT = dotenv["random_text"]

    @JvmStatic
    val WEEB_SH_TOKEN = dotenv["weeb_token"]

    @JvmStatic
    private fun dotenv(envVar: String): String? {
        return dotenv(envVar) ?: throw RuntimeException("Could not read environment variable \"$envVar\", make sure it is supplied and not empty!")
    }
}
