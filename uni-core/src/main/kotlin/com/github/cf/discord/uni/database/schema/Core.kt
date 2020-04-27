/*
 *   Copyright (C) 2017-2021 computerfreaker
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

object Core : Table(){
    val discordToken = varchar("discordToken", 200)
            .uniqueIndex()
    val nicoNicoEmail = varchar("nicoNicoEmail", 200)
    val nicoNicoPassword = varchar("nicoNicoPassword", 900)
    val googleApiKey = varchar("googleApiKey", 200)
    val googleSearchEngineID = varchar("googleSearchEngineID", 200)
    val dblToken = varchar("dblToken", 900)
            .nullable()
    val sauceNaoToken = varchar("sauceNaoToken", 900)
    val osuToken = varchar("osuToken", 200)
    val discordBoatsToken = varchar("discordBoatsToken", 900)
            .nullable()
    val discordServicesToken = varchar("discordServicesToken", 900)
            .nullable()

}
