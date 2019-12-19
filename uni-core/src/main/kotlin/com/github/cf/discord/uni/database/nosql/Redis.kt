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
package com.github.cf.discord.uni.database.nosql

import com.github.cf.discord.uni.core.EnvVars
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

object Redis {

    private val config = Config().apply {
        this.useSingleServer()
                .setAddress("redis://127.0.0.1:6379").connectionPoolSize = 200
    }

    val client: RedissonClient = Redisson.create(config)
}
