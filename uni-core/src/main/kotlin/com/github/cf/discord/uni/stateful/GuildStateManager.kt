/*
 *   Copyright (C) 2017-2019 computerfreaker
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
package com.github.cf.discord.uni.stateful

import java.util.concurrent.ConcurrentHashMap

/**
 * A generified state manager on a per-guild basis which represents a mapping of guild to type T.
 */
class GuildStateManager<T> {

    private val guildManager: MutableMap<Long, T> = ConcurrentHashMap()

    fun getOrPut(guildId: Long, defaultValue: () -> T): T = guildManager.getOrPut(guildId, defaultValue)
    fun putIfAbsent(guildId: Long, value: T) = guildManager.putIfAbsent(guildId, value)
    fun put(guildId: Long, value: T) = guildManager.put(guildId, value)
    fun delete(guildId: Long) = guildManager.remove(guildId)
    fun contains(guildId: Long): Boolean = guildManager.containsKey(guildId)
}
