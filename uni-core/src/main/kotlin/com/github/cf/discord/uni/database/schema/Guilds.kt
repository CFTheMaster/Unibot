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
package com.github.cf.discord.uni.database.schema

import org.jetbrains.exposed.sql.Table

object Guilds : Table(){
    val id = long("id")
            .uniqueIndex()
            .primaryKey()
    val name = varchar("name", 300)
    val prefix = varchar("prefix", 20)
            .nullable()
    val starboard = bool("starboard")
    val starboardChannel = long("starboardChannel")
            .nullable()
    val logs = bool("logs")
    val modlogs = bool("modlogs")
    val modLogChannel = long("modlogChannel")
            .nullable()
    val welcome = bool("welcome")
    val welcomeChannel = long("welcomeChannel")
            .nullable()
    val welcomeMessage = varchar("welcomeMessage", 2000)
    val leaveMessage = varchar("leaveMessage", 2000)
    val levelMessages = bool("levelMessages")
    val mutedRole = long("mutedRole")
            .nullable()
    val antiInvite = bool("antiInvite")
    val userRole = bool("userRole")
    val autoRole = long("autoRole")
            .nullable()
}
